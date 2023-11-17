package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.*;
import java.time.LocalDate;
import java.util.Arrays;

import static uk.ac.ed.inf.GetDataFromRest.getOrdersOnDay;

public class PathCharterTest {
    public static void main (String[] args) {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        //Test to get Moves
        LocalDate date = LocalDate.of(2023,9,1);
        Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
        Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();
        for (Order order: allOrdersDate){
            new OrderValidator().validateOrder(order, restaurantData);
        }
        Order[] validOrdersDate = OrderValidator.filterValidOrders(allOrdersDate);
        Move[] path = PathCharter.totalMoves(validOrdersDate);

        double[] distances = new double[path.length];
        int angleCount = 0;
        for (int i = 0; i < path.length; i++) {
            double fromLng1 = path[i].fromLng();
            double fromLat1 = path[i].fromLat();

            double toLng = path[i].toLng();
            double toLat = path[i].toLat();

            double distance = new LngLatHandler().distanceTo(new LngLat(fromLng1, fromLat1), new LngLat(toLng, toLat));
            distances[i] = distance;
            System.out.println("[" + fromLng1 + "," + fromLat1 + "] to [" + toLng + "," + toLat + "], Distance: " + distance + ", Angle: " + path[i].angle());

            if(path[i].angle() == 999){
                angleCount += 1;
            }
        }

        boolean allValuesAreValid = true;

        for (double distance : distances) {
            if (Math.abs(distance - 0.00015) > 1e-10 && Math.abs(distance) > 1e-10) {
                allValuesAreValid = false;
                break;
            }
        }

        boolean allAnglesAreValid = true;
        for (Move move : path) {
            double angle = move.angle();
            if (Math.abs(angle) > 1e-6 && (angle % 22.5 > 1e-9 && angle % 999 > 1e-9)) {
                allAnglesAreValid = false;
                break;
            }
        }
        System.out.println("All angles are either multiples of 22.5 or 999: " + allAnglesAreValid);
        System.out.println("All values are either 0.00015 or 0: " + allValuesAreValid);
        System.out.println("Number of angles 999: " + angleCount);
        System.out.println("Valid number of hovers: " + (validOrdersDate.length == angleCount/2));

//        LocalDate date = LocalDate.of(2023,9,1);
//        LngLat appleton = new LngLat(-3.1870,55.9445);
//        LngLat rest = new LngLat(-3.1913, 55.9455);
//        Order[] ordersOnDate = getOrdersOnDay(date);
//        Order order = ordersOnDate[1];
//
//        double step = new LngLatHandler().distanceTo(appleton, rest)/6;
//        PathCharter.PathPoint[] un = PathCharter.AstarAlg(appleton,rest, step);
//        PathCharter.PathPoint[] refine = PathCharter.fullPath(order, appleton);
//
//        System.out.println(Arrays.toString(refine));
//        System.out.println(refine.length);
//        for (int i = 1; i < refine .length;i++){
//            System.out.println(PathCharter.calculateBearing(refine[i -1].location(), refine[i].location()));
//        }
//
//        for (PathCharter.PathPoint point: refine){
//            System.out.println("[" + point.location().lng() + "," + point.location().lat() + "],");
//        }
    }
}
