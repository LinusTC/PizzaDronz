package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static uk.ac.ed.inf.LngLatHandler.round;

public class PathCharter {
    public static LngLat appleton = new LngLat(-3.186874, 55.944494);
    //Get Central
    public static NamedRegion central = GetDataFromRest.getCentralAreaData();
    //Get noFlyZones from rest
    public static NamedRegion[] noFlyZones = GetDataFromRest.getNoFlyZones();

    public static Move[] totalMovesPerOrder(Order order){
        return null;
    }

    public static LngLat[] shortestValidPath (Order validOrder){

        Restaurant orderRestaurant = OrderValidator.findPizzaRestaurant(validOrder.getPizzasInOrder()[0], GetDataFromRest.getRestaurantsData());

        //Only valid orders will call this method
        if (new LngLatHandler().isInCentralArea(Objects.requireNonNull(orderRestaurant).location(), central)){

        }

        else{
            LngLat edge = closestEdge(orderRestaurant.location());
        }
        return null;
    }

    public static LngLat[] aStarAlg (LngLat start, LngLat end){

        LngLatHandler handler = new LngLatHandler();

        double startEnd = handler.distanceTo(start, end);
        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();

        Node startNode = new Node(start, null, 0);
        Node endNode = new Node(end, null, 0);

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
                System.out.println(i);
            }
            openList.remove(currentIndex);
            closedList.add(currentNode);

            //If endNode was found, build a path back to the start.
            double dx = round(Math.abs(currentNode.location().lng() - endNode.location().lng()));
            double dy = round(Math.abs(currentNode.location().lat() - endNode.location().lat()));
            if (dx < 0.00015 && dy < 0.00015){
                List<LngLat> path = new ArrayList<>();
                Node temp = currentNode;

                while (temp != null){
                    path.add(temp.location());
                    temp = temp.parent();
                }

                Collections.reverse(path);
                return path.toArray(new LngLat[0]);
            }

            //Create a childNodes list
            List<Node> childNodes = new ArrayList<>();
            for (int i = 0; i < 16; i++){
                //Location of child
                LngLat parentLocation = currentNode.location();
                LngLat childLocation = handler.nextPosition(parentLocation, 22.5*i);

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
                if (!isPathClear(parentLocation, childLocation, noFlyZones)){
                    inNoFlyZone = true;
                }
                if (inNoFlyZone) {
                    continue;
                }

                Node tempChildNode = new Node(childLocation, currentNode, heuristics);
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

    public static boolean isPathClear(LngLat start, LngLat end, NamedRegion[] noFlyZones) {
        LngLatHandler handler = new LngLatHandler();
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


    public static LngLat closestEdge (LngLat restaurantLocation){

        //Check if starting point is already in central
        if(new LngLatHandler().isInCentralArea(restaurantLocation,central)){
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

}
