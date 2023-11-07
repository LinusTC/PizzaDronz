package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.util.*;
import java.util.stream.IntStream;
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
        return null;
    }

    public static PathPoint[] pathFromAT (Order validOrder){

        Restaurant orderRestaurant = OrderValidator.findPizzaRestaurant(validOrder.getPizzasInOrder()[0], GetDataFromRest.getRestaurantsData());

        if (handler.isInCentralArea(Objects.requireNonNull(orderRestaurant).location(), central)){
            return modAStarAlg(appleton, orderRestaurant.location());
        }

        else{
            LngLat edge = closestEdge(orderRestaurant.location());
            PathPoint[] pt1 = modAStarAlg(appleton,edge);
            PathPoint[] pt2 = modAStarAlg(edge, orderRestaurant.location());

            if (pt1 != null) {
                if (pt2 != null) {
                    return Stream.concat(Arrays.stream(pt1), Arrays.stream(pt2)).toArray(PathPoint[]::new);
                }
            }
            return null;
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
            double dx = round(Math.abs(currentNode.location().lng() - endNode.location().lng()));
            double dy = round(Math.abs(currentNode.location().lat() - endNode.location().lat()));
            if (dx < 0.00015 && dy < 0.00015){
                List<PathPoint> path = new ArrayList<>();
                Node temp = currentNode;
                while (temp != null){
                    PathPoint newPP = new PathPoint(temp.location,temp.angle);
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

                //Angle
                double angle = 22.5*i;

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

                Node tempChildNode = new Node(childLocation, currentNode, heuristics, angle);
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
        double closestX = round(Math.min(Math.max(x, xMin), xMax));
        double closestY = round(Math.min(Math.max(y, yMin), yMax));

        return new LngLat(closestX,closestY);
    }
    private record Node(LngLat location, Node parent, double heuristics, double angle) {
    }
    public record PathPoint(LngLat location, double angle) {
    }
    public record Move(String orderNo, float fromLng, float fromLat, float angle, float toLng, float toLat) {
    }
}
