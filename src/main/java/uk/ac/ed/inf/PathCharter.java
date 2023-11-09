package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

public class PathCharter {
    public static final LngLat appleton = new LngLat(-3.186874, 55.944494);
    //Get Central
    public static final NamedRegion central = GetDataFromRest.getCentralAreaData();
    //Get noFlyZones from rest
    public static final NamedRegion[] noFlyZones = GetDataFromRest.getNoFlyZones();

    public static final LngLatHandler handler = new LngLatHandler();

    public static final double maxMoveDistance = 0.00015;

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
                    angle = 999;
                }
                orderMovesList.add(pptoMove(order, roundLngLat(current.location), angle, roundLngLat(next.location)));
            }

            startPoint = orderPath[orderPath.length - 1 ].location;

        }
        return orderMovesList.toArray(new Move[0]);
    }

    public static Move pptoMove (Order order, LngLat first, double angle, LngLat second){
        //Order Number
        String orderNumber = order.getOrderNo();

        //fromLng and fromLat
        double firstLng = first.lng();
        double firstLat = first.lat();

        //toLng and toLat
        double secondLng = second.lng();
        double secondLat = second.lat();

        return new Move(orderNumber
                , (float)firstLng
                , (float)firstLat
                , (float)angle
                , (float)secondLng
                , (float)secondLat);
    }

    public static PathPoint[] fullPath (Order validOrder, LngLat startPoint){

        Restaurant orderRestaurant = OrderValidator.findPizzaRestaurant(validOrder.getPizzasInOrder()[0], GetDataFromRest.getRestaurantsData());
        LngLat restLocation = Objects.requireNonNull(orderRestaurant).location();

        //If restaurant is in central, find path
        if (handler.isInCentralArea(restLocation, central)){

            double stepSize = handler.distanceTo(startPoint, restLocation)/6;

            PathPoint[] unrefinedPathToRest = AstarAlg(startPoint, restLocation, stepSize);
            PathPoint[] pathToRest = fullyRefine(unrefinedPathToRest, restLocation);

            PathPoint[] unrefinedPathToAT = AstarAlg(pathToRest[pathToRest.length-1].location, appleton, stepSize);
            PathPoint[] restToAT = fullyRefine(unrefinedPathToAT, appleton);

            validOrder.setOrderStatus(OrderStatus.DELIVERED);
            return Stream.of(pathToRest, restToAT).filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .toArray(PathPoint[]::new);
        }

        else{
            //Go to edge restaurant is closest then go directly to restaurant from edge
            LngLat edge = closestEdge(restLocation);
            double stepSizeToEdge = handler.distanceTo(startPoint, edge)/6;
            double stepSizeToRest = handler.distanceTo(edge, restLocation)/3;

            PathPoint[] unrefinedPt1 = AstarAlg(startPoint,edge, stepSizeToEdge);
            PathPoint[] pt1 = fullyRefine(unrefinedPt1, edge);

            PathPoint[] unrefinedPt2 = AstarAlg(pt1[pt1.length - 1].location, restLocation, stepSizeToEdge);
            PathPoint[] pt2 = fullyRefine(unrefinedPt2, restLocation);

            PathPoint[] unrefinedPt3 = AstarAlg(pt2[pt2.length - 1].location, edge, stepSizeToEdge);
            PathPoint[] pt3 = fullyRefine(unrefinedPt3, edge);

            PathPoint[] unrefinedPt4 = AstarAlg(pt3[pt3.length - 1].location, appleton, stepSizeToRest);
            PathPoint[] pt4 = fullyRefine(unrefinedPt4, appleton);

            assert pt2 != null;
            if (pt2.length > 1) {
                pt2 = Arrays.copyOfRange(pt2, 1, pt2.length);
            }
            assert pt4 != null;
            if (pt4.length > 1) {
                pt4 = Arrays.copyOfRange(pt4, 1, pt4.length);
            }

            validOrder.setOrderStatus(OrderStatus.DELIVERED);
            return Stream.of(pt1, pt2, pt3, pt4)
                    .flatMap(Arrays::stream)
                    .toArray(PathPoint[]::new);

        }
    }

    public static PathPoint[] fullyRefine(PathPoint[] unrefinedPath, LngLat end) {
        List<PathPoint> refinedPath = new ArrayList<>();
        refinedPath.add(unrefinedPath[0]);
        for(int i = 1; i < unrefinedPath.length; i++){
            LngLat next = unrefinedPath[i].location;
            LngLat curr = unrefinedPath[i-1].location;

            PathPoint[] subPath = AstarAlg(curr, next, maxMoveDistance);

            for (int j = 1; j < subPath.length ;j++){
                LngLat subCurr = subPath[j].location;
                LngLat subPrev = subPath[j-1].location;

                if(handler.distanceTo(subPrev, subCurr) >= 0.00015){
                    refinedPath.add(subPath[j]);
                }

            }
        }

        LngLat unrefinedPathEnd = unrefinedPath[unrefinedPath.length - 1].location;
        PathPoint[] subPathLast = AstarAlg(unrefinedPathEnd, end, maxMoveDistance);
        refinedPath.addAll(Arrays.asList(subPathLast));

        return refinedPath.toArray(new PathPoint[0]);
    }


    public static PathPoint[] AstarAlg (LngLat start, LngLat end, double stepSize){

        stepSize = Math.ceil(stepSize/ 0.00015) * 0.00015;

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
                    PathPoint newPP = new PathPoint(temp.location,999);
                    path.add(newPP);
                    temp = temp.parent();
                }
                Collections.reverse(path);
                return path.toArray(new PathPoint[0]);
            }

            //Create a childNodes list
            List<Node> childNodes = new ArrayList<>();
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
    public static boolean isPathClear(LngLat start, LngLat end) {
        double stepSize = 0.00001;

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

    //Get the central edge closest to restaurant
    public static LngLat closestEdge (LngLat restaurantLocation){

        //Check if starting point is already in central
        if(handler.isInCentralArea(restaurantLocation,central)){
            return restaurantLocation;
        }

        double x = restaurantLocation.lng();
        double y = restaurantLocation.lat();

        //Using the following code to find max/min lng-lat values.
        LngLat[] vertices = central.vertices();
        double xMin = vertices[0].lng();
        double xMax = vertices[2].lng();
        double yMin = vertices[2].lat();
        double yMax = vertices[0].lat();

        //Finds the closest point on central box
        double closestX = Math.min(Math.max(x, xMin), xMax);
        double closestY = Math.min(Math.max(y, yMin), yMax);

        return new LngLat(closestX,closestY);
    }

    public static LngLat nextPositionAStar(LngLat startPosition, double angle, double stepSize) {

        if (angle == 999){
            return startPosition;
        }

        double nextX = (startPosition.lng() + (stepSize * Math.cos(Math.toRadians(angle))));
        double nextY = (startPosition.lat() + (stepSize * Math.sin(Math.toRadians(angle))));

        return new LngLat(nextX, nextY);
    }

    public static double calculateBearing(LngLat start, LngLat end) {
        if(new LngLatHandler().distanceTo(start,end) < 0.00015){
            return 999;
        }
        double lat1 = Math.toRadians(start.lat());
        double lat2 = Math.toRadians(end.lat());
        double deltaLng = Math.toRadians(end.lng() - start.lng());

        double y = Math.sin(deltaLng) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        double roundedBearing = Math.round(bearing / 22.5) * 22.5;

        return (roundedBearing - 90 + 360) % 360;
    }

    private static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(5, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static LngLat roundLngLat(LngLat value) {
        double lat = value.lat();
        double lng = value.lng();

        return new LngLat(round(lng), round(lat));
    }

    public record Node(LngLat location
            , Node parent
            , double heuristics
            , double distanceFromStart
            , double totalCost) {
    }
    public record PathPoint(LngLat location, double angleFromParent) {
    }
}