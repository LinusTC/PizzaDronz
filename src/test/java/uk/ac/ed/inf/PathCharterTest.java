package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.*;
import java.time.LocalDate;
import java.util.Arrays;

public class PathCharterTest {
    public static void main (String[] args) {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        //Test to get Moves
        LocalDate date = LocalDate.of(2023,9,3);
        Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
        Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();
        for (Order order: allOrdersDate){
            new OrderValidator().validateOrder(order, restaurantData);
        }
        Order[] validOrdersDate = OrderValidator.filterValidOrders(allOrdersDate);
        Move[] path = PathCharter.totalMoves(validOrdersDate);

        System.out.println(Arrays.toString(path));
        double[] distances = new double[path.length - 1];

        for (int i = 1; i < path.length; i++) {
            double fromLng1 = path[i - 1].fromLng();
            double fromLat1 = path[i - 1].fromLat();

            double fromLng2 = path[i].fromLng();
            double fromLat2 = path[i].fromLat();

            double distance = new LngLatHandler().distanceTo(new LngLat(fromLng1, fromLat1), new LngLat(fromLng2, fromLat2));
            distances[i - 1] = distance;
            System.out.println("[" + fromLng1 + "," + fromLat1 + "] to [" + fromLng2 + "," + fromLat2 + "], Distance: " + distance);
        }

        boolean allValuesAreValid = true;

        for (double distance : distances) {
            if (Math.abs(distance - 0.00015) > 1e-6 && Math.abs(distance) > 1e-6) {
                allValuesAreValid = false;
                break;
            }
        }

        boolean allAnglesAreValid = true;
        for (Move move : path) {
            double angle = move.angle();
            // Check if the angle is either a multiple of 22.5 or 999
            if (Math.abs(angle) > 1e-6 && (Math.abs(angle % 22.5) > 1e-6 && Math.abs(angle % 999) > 1e-6)) {
                allAnglesAreValid = false;
                break;
            }
        }
        System.out.println("All angles are either multiples of 22.5 or 999: " + allAnglesAreValid);
        System.out.println("All values are either 0.00015 or 0: " + allValuesAreValid);
        System.out.println(validOrdersDate.length);
        System.out.println(path.length);

//        LocalDate date = LocalDate.of(2023,9,1);
//        LngLat appleton = new LngLat(-3.1870,55.9445);
//        LngLat rest = new LngLat(-3.1913, 55.9455);
//        Order[] ordersOnDate = getOrdersOnDay(date);
//        Order order = ordersOnDate[0];
//
//        double step = new LngLatHandler().distanceTo(appleton, rest)/6;
//        PathCharter.PathPoint[] un = PathCharter.AstarAlg(appleton,rest, step);
//        PathCharter.PathPoint[] refine = PathCharter.fullPath(order, appleton);
//
//        System.out.println(Arrays.toString(refine));
//        System.out.println(refine.length);
//        for (int i = 1; i < refine .length;i++){
//            System.out.println(new LngLatHandler().distanceTo(refine[i -1].location(), refine[i].location()));
//        }
//
//        for (PathCharter.PathPoint point: refine){
//            System.out.println("[" + point.location().lng() + "," + point.location().lat() + "],");
//        }
    }
}
