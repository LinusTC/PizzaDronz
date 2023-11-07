package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.util.*;
import java.util.stream.Stream;

import static uk.ac.ed.inf.LngLatHandler.round;

public class PathCharter {
    private static final LngLat appleton = new LngLat(-3.186874, 55.944494);
    //Get Central
    private static final NamedRegion central = GetDataFromRest.getCentralAreaData();
    //Get noFlyZones from rest
    private static final NamedRegion[] noFlyZones = GetDataFromRest.getNoFlyZones();

    private static final LngLatHandler handler = new LngLatHandler();

    public static Move[] totalMovesPerOrder(Order order) {

        PathPoint[] orderPath = fullPath(order);

        List<Move> orderMovesList = new ArrayList<>();
        for(int i = 0; i < orderPath.length;i++){

            PathPoint current = orderPath[i];
            PathPoint next = (i < orderPath.length - 1) ? orderPath[i + 1] : current;
            double angle = next.angleFromParent;

            if(next.equals(current)){
                angle = 999;
            }
            orderMovesList.add(pptoMove(order, current.location, angle, next.location));

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
                , (float)firstLng
                , (float)firstLat
                , (float)angle
                , (float)secondLng
                , (float)secondLat);
    }

    private static PathPoint[] fullPath (Order validOrder){

        Restaurant orderRestaurant = OrderValidator.findPizzaRestaurant(validOrder.getPizzasInOrder()[0], GetDataFromRest.getRestaurantsData());

        //If restaurant is in central, find path
        if (handler.isInCentralArea(Objects.requireNonNull(orderRestaurant).location(), central)){
            PathPoint[] pathToRest = modAStarAlg(appleton, orderRestaurant.location());
            PathPoint[] restToAT = modAStarAlg(pathToRest[pathToRest.length-1].location, appleton);

            validOrder.setOrderStatus(OrderStatus.DELIVERED);
            return Stream.of(pathToRest, restToAT).filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .toArray(PathPoint[]::new);
        }

        else{
            //Go to edge restaurant is closest then go directly to restaurant from edge
            LngLat edge = closestEdge(orderRestaurant.location());
            PathPoint[] pt1 = modAStarAlg(appleton,edge);
            PathPoint[] pt2 = modAStarAlg(edge, orderRestaurant.location());
            PathPoint[] pt3 = modAStarAlg(orderRestaurant.location(), edge);
            PathPoint[] pt4 = modAStarAlg(edge, appleton);

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

    private static PathPoint[] modAStarAlg (LngLat start, LngLat end){

        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();

        Node startNode = new Node(start, null, 0, 999);
        Node endNode = new Node(end, null, 0, 999);

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

                if (item.heuristics() < currentNode.heuristics()) {
                    currentNode = item;
                    currentIndex = i;
                }
            }
            openList.remove(currentIndex);
            closedList.add(currentNode);

            //If endNode was found, build a path back to the start.
            double distance = handler.distanceTo(currentNode.location, endNode.location);
            if (distance < 0.00015){
                List<PathPoint> path = new ArrayList<>();
                Node temp = currentNode;
                while (temp != null){
                    PathPoint newPP = new PathPoint(temp.location,temp.angleFromParent);
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
                LngLat parentLocation = currentNode.location();
                LngLat childLocation = handler.nextPosition(parentLocation, 22.5*i);

                //Angle from parent
                double angleFromParent = 22.5*i;

                //Heuristics
                double heuristics = handler.distanceTo(childLocation, end);

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

                Node tempChildNode = new Node(childLocation, currentNode, heuristics, angleFromParent);
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
    private static LngLat closestEdge (LngLat restaurantLocation){

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

    private record Node(LngLat location, Node parent, double heuristics, double angleFromParent) {
    }
    private record PathPoint(LngLat location, double angleFromParent) {
    }
}
