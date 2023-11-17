package uk.ac.ed.inf;

import org.junit.After;
import org.junit.Test;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest{
    LocalDate startDate = LocalDate.of(2023, 9, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 28);
    LocalDate[] randomDates = new LocalDate[4];
    String projectDir = System.getProperty("user.dir") + "/resultfiles/";

    @Test
    public void testFileGeneration(){

        String urlInput = "https://ilp-rest.azurewebsites.net";
        GetDataFromRest.setBaseUrl(urlInput);

        for (int i = 0; i < 4; i++) {
            LocalDate date = generateRandomDate(startDate, endDate);
            randomDates[i] = date;
        }

        for (LocalDate date: randomDates){//Get data from REST
            Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
            Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();

            //Validate Orders
            for (Order order : allOrdersDate) {
                new OrderValidator().validateOrder(order, restaurantData);
            }

            //Get all valid orders
            Order[] validOrdersDate = OrderValidator.filterValidOrders(allOrdersDate);

            //Create the drone path
            Move[] path = PathCharter.totalMoves(validOrdersDate);

            CreateJsonDocuments.createFlightPath(date, path);
            CreateJsonDocuments.createDrone(date, path);
            CreateJsonDocuments.createDeliveries(date, allOrdersDate);

            String flightPathFileName = projectDir + "flightpath-" + date + ".json";
            String droneFileName = projectDir + "drone-" + date + ".geojson";
            String deliveriesFileName = projectDir + "deliveries-" + date + ".json";

            // Assert that the files exist
            assertTrue("Flight path file for " + date + " not created", Files.exists(Paths.get(flightPathFileName)));
            assertTrue("Drone file for " + date + " not created", Files.exists(Paths.get(droneFileName)));
            assertTrue("Deliveries file for " + date + " not created", Files.exists(Paths.get(deliveriesFileName)));}
    }
    @After
    public void cleanup() throws IOException {
        for(LocalDate date: randomDates){

            String flightPathFileName = projectDir + "flightpath-" + date + ".json";
            String droneFileName = projectDir + "drone-" + date + ".geojson";
            String deliveriesFileName = projectDir + "deliveries-" + date + ".json";

            Files.deleteIfExists(Paths.get(flightPathFileName));
            Files.deleteIfExists(Paths.get(droneFileName));
            Files.deleteIfExists(Paths.get(deliveriesFileName));

            System.out.println("Files for "+ date + " were successfully created and deleted after testing.");
        }
    }

    private LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        Random random = new Random();
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomEpochDay);
    }
}
