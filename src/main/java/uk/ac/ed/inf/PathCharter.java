package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.util.*;
import java.util.stream.Stream;

public class PathCharter {
    private static final LngLat appleton = new LngLat(-3.186874, 55.944494);
    private static final NamedRegion central = GetDataFromRest.getCentralAreaData();
    private static final NamedRegion[] noFlyZones = GetDataFromRest.getNoFlyZones();
    private static final LngLatHandler handler = new LngLatHandler();
    private static final double maxMoveDistance = SystemConstants.DRONE_MOVE_DISTANCE;
    private static final double hoverAngle = 999;

    //totalMoves is a method that gets all the moves the drone will make on a given day
    public static Move[] totalMoves(Order[] ordersToChart) {
        List<Move> orderMovesList = new ArrayList<>();

        LngLat startPoint = appleton;
        for(Order order: ordersToChart){

            PathPoint[] orderPath = fullPath(order, startPoint);
            for(int i = 0; i < orderPath.length;i++) {
                PathPoint current = orderPath[i];
                PathPoint next = (i < orderPath.length - 1) ? orderPath[i + 1] : current;
                double angle = next.angleFromParent;

                if(next.equals(current)){
                    angle = hoverAngle;
                }
                orderMovesList.add(pptoMove(order, current.location, angle, next.location));
            }

            startPoint = orderPath[orderPath.length - 1 ].location;

        }
        return orderMovesList.toArray(new Move[0]);
    }

    private static Move pptoMove (Order order, LngLat first, double angle, LngLat second){
        //Order Number
        String orderNumber = order.getOrderNo();

        //fromLng and fromLat
        double firstLng = first.lng();
        double firstLat = first.lat();

        //toLng and toLat
        double secondLng = second.lng();
        double secondLat = second.lat();

        return new Move(orderNumber
                , firstLng
                , firstLat
                , angle
                , secondLng
                , secondLat);
    }

    private static PathPoint[] fullPath(Order validOrder, LngLat startPoint){

        Restaurant orderRestaurant = OrderValidator.findPizzaRestaurant(validOrder.getPizzasInOrder()[0], GetDataFromRest.getRestaurantsData());
        LngLat restLocation = Objects.requireNonNull(orderRestaurant).location();

        //If restaurant is in central, find path
        if (handler.isInCentralArea(restLocation, central)){

            //Using a large stepSize to reduce search space
            double stepSize = handler.distanceTo(startPoint, restLocation)/6;

            //Appleton to Restaurant
            PathPoint[] pathToRest = twoStep(startPoint, restLocation, stepSize);

            //Restaurant to Appleton
            PathPoint[] restToAT = twoStep(pathToRest[pathToRest.length-1].location, appleton, stepSize);

            //Combine Paths
            PathPoint[] fullPath = Stream.of(pathToRest, restToAT)
                    .flatMap(Arrays::stream)
                    .toArray(PathPoint[]::new);

            for(int i = 1; i < fullPath.length;i++){
                LngLat start = fullPath[i - 1].location;
                LngLat end = fullPath[i].location;

                double angle = calculateBearing(start, end);

                fullPath[i] = new PathPoint(fullPath[i].location, angle);
            }
            validOrder.setOrderStatus(OrderStatus.DELIVERED);
            return fullPath;
        }

        //Go to edge restaurant is closest then go directly to restaurant from edge
        else{
            LngLat edge = closestEdge(restLocation);
            //Using a large stepSize to reduce search space
            double stepSizeToEdge = handler.distanceTo(startPoint, edge)/6;
            double stepSizeToRest = handler.distanceTo(edge, restLocation)/6;

            //Appleton to edge
            PathPoint[] pt1 = twoStep(startPoint, edge, stepSizeToEdge);

            //Edge to restaurant
            PathPoint[] pt2 = twoStep(pt1[pt1.length - 1].location, restLocation, stepSizeToRest);

            //Restaurant back to edge
            PathPoint[] pt3 = twoStep(pt2[pt2.length - 1].location, edge, stepSizeToRest);

            //Edge to appleton
            PathPoint[] pt4 = twoStep(pt3[pt3.length - 1].location, appleton, stepSizeToEdge);

            //Remove the hover move when the drone reaches an edge
            if (pt2.length > 1) {
                pt2 = Arrays.copyOfRange(pt2, 1, pt2.length);
            }
            if (pt4.length > 1) {
                pt4 = Arrays.copyOfRange(pt4, 1, pt4.length);
            }

            //Combine paths
            PathPoint[] fullPath = Stream.of(pt1, pt2, pt3, pt4)
                    .flatMap(Arrays::stream)
                    .toArray(PathPoint[]::new);

            for(int i = 1; i < fullPath.length;i++){
                LngLat start = fullPath[i - 1].location;
                LngLat end = fullPath[i].location;

                double angle = calculateBearing(start, end);

                fullPath[i] = new PathPoint(fullPath[i].location, angle);
            }

            validOrder.setOrderStatus(OrderStatus.DELIVERED);
            return fullPath;
        }
    }

    private static PathPoint[] twoStep(LngLat startPoint, LngLat endPoint, double intialStepSize){
        PathPoint[] unrefined = AstarAlg(startPoint,endPoint,intialStepSize);
        assert unrefined != null;
        return fullyRefine(unrefined, endPoint);
    }

    //Refine the path so that the distance between each node is 0.00015
    private static PathPoint[] fullyRefine(PathPoint[] unrefinedPath, LngLat end) {

        List<PathPoint> refinedPath = new ArrayList<>();

        // Add the first point of the unrefined path to the refined path
        refinedPath.add(unrefinedPath[0]);
        LngLat curr = refinedPath.get(0).location;

        //Iterate through each node within unrefinedPath
        for(int i = 1; i < unrefinedPath.length; i++){
            LngLat next = unrefinedPath[i].location;

            PathPoint[] subPath = AstarAlg(curr, next, maxMoveDistance);
            assert subPath != null;

            refinedPath.addAll(Arrays.asList(subPath).subList(1, subPath.length));

            curr = refinedPath.get(refinedPath.size() - 1).location;
        }

        // Use an A* algorithm to find a subPath from the end of the unrefined path to the final destination (end)
        LngLat unrefinedPathEnd = refinedPath.get(refinedPath.size() - 1).location;
        PathPoint[] subPathLast = AstarAlg(unrefinedPathEnd, end, maxMoveDistance);

        for (int j = 1; j < Objects.requireNonNull(subPathLast).length; j++) {
            LngLat subCurr = subPathLast[j].location;

            if (!subCurr.equals(refinedPath.get(refinedPath.size() - 1).location)) {
                refinedPath.add(subPathLast[j]);
            }

        }

        return refinedPath.toArray(new PathPoint[0]);
    }

    //A star algorithm used to evaluate the fastest path.
    private static PathPoint[] AstarAlg(LngLat start, LngLat end, double stepSize){

        //Distance between two nodes must always be a multiple of 0.00015
        stepSize = Math.ceil(stepSize/ maxMoveDistance) * maxMoveDistance;

        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();

        Node startNode = new Node(start, null, handler.distanceTo(start,end), 0, 0);
        Node endNode = new Node(end, null, 0, 0, 0);

        openList.add(startNode);

        for (NamedRegion noFlyZone : noFlyZones) {
            if (handler.isInRegion(end, noFlyZone)) {
                return null;
            }
        }

        while (!openList.isEmpty()){

            //Find the node in openList with the least cost
            Node currentNode = openList.get(0);
            int currentIndex = 0;
            for (int i = 0; i < openList.size(); i++) {
                Node item = openList.get(i);

                if (item.totalCost < currentNode.totalCost) {
                    currentNode = item;
                    currentIndex = i;
                }
            }
            openList.remove(currentIndex);
            closedList.add(currentNode);

            //If endNode was found, build a path back to the start.
            double distance = handler.distanceTo(currentNode.location, endNode.location);
            if (distance < stepSize){
                List<PathPoint> path = new ArrayList<>();
                Node temp = currentNode;
                while (temp != null){
                    PathPoint newPP = new PathPoint(temp.location,hoverAngle);
                    path.add(newPP);
                    temp = temp.parent();
                }
                Collections.reverse(path);
                return path.toArray(new PathPoint[0]);
            }

            //Create a childNodes list
            List<Node> childNodes = new ArrayList<>();
            //Loop through 16 nodes, each being a multiple of 22.5 until 360 degrees
            for (int i = 0; i < 16; i++){
                //Location of child
                LngLat parentLocation = currentNode.location;
                LngLat childLocation = nextPositionAStar(parentLocation, 22.5*i, stepSize);

                //Heuristics
                double heuristics = handler.distanceTo(childLocation, end);

                //Distance From start
                double distanceFromStart = currentNode.distanceFromStart + stepSize;

                //Total Cost
                double totalCost = heuristics + distanceFromStart;

                //Check if inNoFlyZone
                boolean inNoFlyZone = false;
                for (NamedRegion noFlyZone : noFlyZones) {
                    if (handler.isInRegion(childLocation, noFlyZone)) {
                        inNoFlyZone = true;
                        break;
                    }
                }
                if (!isPathClear(parentLocation, childLocation)){
                    inNoFlyZone = true;
                }
                if (inNoFlyZone) {
                    continue;
                }

                Node tempChildNode = new Node(childLocation, currentNode, heuristics, distanceFromStart, totalCost);
                childNodes.add(tempChildNode);
            }

            for (Node childNode: childNodes){
                //Check if childNode is already in closedList
                boolean inClosedList = false;
                for(Node closedNode: closedList){
                    if (childNode.equals(closedNode)){
                        inClosedList = true;
                        break;
                    }
                }
                if(inClosedList){
                    continue;
                }

                //Put childNode in openList if it isn't already in there
                boolean inOpenList = false;
                for(Node openNode: openList){
                    if (childNode.equals(openNode)){
                        inOpenList = true;
                        break;
                    }
                }
                if(inOpenList){
                    continue;
                }
                openList.add(childNode);
            }
        }
        return null;
    }

    //Check if path between two nodes is clear, not within noFlyZones
    private static boolean isPathClear(LngLat start, LngLat end) {
        double stepSize = 0.000005;

        double distance = handler.distanceTo(start, end);

        double stepX = (end.lng() - start.lng()) / distance * stepSize;
        double stepY = (end.lat() - start.lat()) / distance * stepSize;

        LngLat currentPoint = new LngLat(start.lng(), start.lat());
        while (handler.distanceTo(currentPoint, end) > stepSize) {
            for (NamedRegion noFlyZone : noFlyZones) {
                if (handler.isInRegion(currentPoint, noFlyZone)) {
                    return false;
                }
            }
            currentPoint = new LngLat(currentPoint.lng() + stepX, currentPoint.lat() + stepY);
        }
        return true;
    }

    //Get the central edge closest to restaurant, this algorithm takes into account if central area is a polygon
    private static LngLat closestEdge (LngLat restaurantLocation){

        LngLat closestEdge = null;

        //Check if starting point is already in central
        if(handler.isInCentralArea(restaurantLocation,central)){
            return restaurantLocation;
        }

        LngLat[] vertices = central.vertices();
        double a, b, c, d, dot, lenSq, cosTheta, minDist = Double.MAX_VALUE, dist;
        int n = vertices.length;

        for (int i = 0, j = n - 1; i < n; j = i++) {

            a = restaurantLocation.lng() - vertices[i].lng();
            b = restaurantLocation.lat() - vertices[i].lat();
            c = vertices[j].lng() - vertices[i].lng();
            d = vertices[j].lat() - vertices[i].lat();
            dot = a * c + b * d;
            lenSq = c * c + d * d;
            cosTheta = dot / lenSq;

            double lng, lat;

            if (cosTheta < 0 || cosTheta > 1) {
                lng = (cosTheta < 0) ? vertices[i].lng() : vertices[j].lng();
                lat = (cosTheta < 0) ? vertices[i].lat() : vertices[j].lat();
            }
            else {
                lng = vertices[i].lng() + cosTheta * c;
                lat = vertices[i].lat() + cosTheta * d;
            }

            LngLat temp = new LngLat(lng, lat);
            dist = handler.distanceTo(restaurantLocation, temp);
            if (dist < minDist) {
                closestEdge = temp;
                minDist = dist;
            }
        }
        return closestEdge;
    }

    private static LngLat nextPositionAStar(LngLat startPosition, double angle, double stepSize) {

        double nextX = (startPosition.lng() + (stepSize * Math.cos(Math.toRadians(angle))));
        double nextY = (startPosition.lat() + (stepSize * Math.sin(Math.toRadians(angle))));

        return new LngLat(nextX, nextY);
    }

    private static double calculateBearing(LngLat start, LngLat end) {

        if(start.equals(end)){
            return hoverAngle;
        }

        double deltaX = end.lng() - start.lng();
        double deltaY = end.lat() - start.lat();

        double angleInRadians = Math.atan2(deltaY, deltaX);
        double angleInDegrees = Math.toDegrees(angleInRadians);

        if (angleInDegrees < 0) {
            angleInDegrees += 360;
        }

        return Math.round(angleInDegrees * 2) / 2.0;
    }

    private record Node(LngLat location
            , Node parent
            , double heuristics
            , double distanceFromStart
            , double totalCost) {
    }
    record PathPoint(LngLat location, double angleFromParent) {
    }
}