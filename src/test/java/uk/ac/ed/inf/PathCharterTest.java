package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Random;

public class PathCharterTest {

    private static final double maxMoveDistance = SystemConstants.DRONE_MOVE_DISTANCE;
    private static final double hoverAngle = 999;

    @Test
    public void validatePathCharacteristics() {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        //Pick 4 random dates
        LocalDate[] randomDates = new LocalDate[4];
        LocalDate startDate = LocalDate.of(2023, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 28);
        for (int i = 0; i < 4; i++) {
            LocalDate date = generateRandomDate(startDate, endDate);
            randomDates[i] = date;
        }

        //Generate path for orders on a random Date and see if the path is valid
        for (LocalDate date : randomDates) {

            Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
            Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();
            for (Order order : allOrdersDate) {
                new OrderValidator().validateOrder(order, restaurantData);
            }
            Order[] validOrdersDate = OrderValidator.filterValidOrders(allOrdersDate);

            //Generate Path
            Move[] path = PathCharter.totalMoves(validOrdersDate);

            double[] distances = new double[path.length];

            for (int i = 0; i < path.length; i++) {
                double fromLng1 = path[i].fromLng();
                double fromLat1 = path[i].fromLat();

                double toLng = path[i].toLng();
                double toLat = path[i].toLat();

                double distance = new LngLatHandler().distanceTo(new LngLat(fromLng1, fromLat1), new LngLat(toLng, toLat));
                distances[i] = distance;
                System.out.println("[" + fromLng1 + "," + fromLat1 + "] to [" + toLng + "," + toLat + "], Distance: " + distance + ", Angle: " + path[i].angle());
            }

            boolean allValuesAreValid = validateDistances(distances);
            assertTrue("All distance between nodes are either 0.00015 or 0", allValuesAreValid);

            boolean allAnglesAreValid = validateAngles(path);
            assertTrue("All angles are either multiples of 22.5 or 999", allAnglesAreValid);

            boolean correctNumberHovers = validateHovers(path, validOrdersDate);
            assertTrue("Correct number of hovers", correctNumberHovers);
        }
    }

    private boolean validateDistances(double[] distances) {
        for (double distance : distances) {
            if (Math.abs(distance - maxMoveDistance) > 1e-10 && Math.abs(distance) > 1e-10) {
                return false;
            }
        }
        return true;
    }

    private boolean validateAngles(Move[] path) {
        for (Move move : path) {
            double angle = move.angle();
            if (Math.abs(angle) > 1e-10 && (angle % 22.5 > 1e-10 && angle % hoverAngle > 1e-10)) {
                return false;
            }

            double fromLng = move.fromLng();
            double fromLat = move.fromLat();
            double toLng = move.toLng();
            double toLat = move.toLat();

            LngLat fromPoint = new LngLat(fromLng, fromLat);
            LngLat toPoint = new LngLat(toLng,toLat);
            LngLat predictedPoint = new LngLatHandler().nextPosition(fromPoint,angle);

            if(!predictedPoint.equals(toPoint)){
                return false;
            }

        }
        return true;
    }

    private boolean validateHovers (Move[] path, Order[] validOrders){
        int numberOfValidOrders = validOrders.length;

        int angleCount = 0;
        for (Move move : path) {
            if (move.angle() == hoverAngle) {
                angleCount += 1;
            }
        }

        return angleCount == numberOfValidOrders * 2;
    }

    private LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        Random random = new Random();
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomEpochDay);
    }
}
