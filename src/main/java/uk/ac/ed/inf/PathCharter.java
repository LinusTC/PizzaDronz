package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.util.Objects;

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
        return null;
    }

    public static Move nextMove(int orderNo,LngLat start, LngLat end){
        double lngDiff = end.lng() - start.lng();
        double latDiff = end.lat() - start.lat();
        double angle = Math.atan2(latDiff, lngDiff);

        double moveAngle = Math.toRadians(Math.round(Math.toDegrees(angle) / 16) * 16);

        LngLat nextPosition = new LngLatHandler().nextPosition(start, moveAngle);

        return new Move(orderNo, start, nextPosition);
    }

    public static LngLat closestEdge (LngLat restaurantLocation){

        //Check if starting point is already in central
        if(new LngLatHandler().isInCentralArea(restaurantLocation,GetDataFromRest.getCentralAreaData())){
            return restaurantLocation;
        }

        double x = restaurantLocation.lng();
        double y = restaurantLocation.lat();

        //Using the following code to find max/min lng-lat values.
        LngLat[] vertices = GetDataFromRest.getCentralAreaData().vertices();
        double xMin = vertices[0].lng();
        double xMax = vertices[2].lng();
        double yMin = vertices[2].lat();
        double yMax = vertices[0].lat();

        //Finds the closest point on central box
        double closestX = Math.min(Math.max(x, xMin), xMax);
        double closestY = Math.min(Math.max(y, yMin), yMax);

        return new LngLat(closestX,closestY);
    }
}
