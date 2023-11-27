package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class App
{
    public static void main(String[] args){
        final long startTime = System.nanoTime();

        //Test if number of input is valid, should only have 2: Date and URL
        if(!validInput(args)){
            System.exit(0);
        }

        String dateInput = args[0];
        String urlInput = args[1];

        //Test if the inputs are valid
        if (!validDate(dateInput) || !validURL(urlInput)){
            System.exit(0);
        }

        //Set date and url as the current parameters
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateInput, formatter);
        GetDataFromRest.setBaseUrl(urlInput);

        //Get data from REST
        Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
        Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();

        //Validate Orders
        for (Order order: allOrdersDate){
            new OrderValidator().validateOrder(order, restaurantData);
        }

        //Get all valid orders
        Order[] validOrdersDate = OrderValidator.filterValidOrders(allOrdersDate);

        //Create the drone path
        Move[] path = PathCharter.totalMoves(validOrdersDate);

        CreateJsonDocuments.createFlightPath(date, path);
        CreateJsonDocuments.createDrone(date,path);
        CreateJsonDocuments.createDeliveries(date, allOrdersDate);

        //RunTime
        final long duration = System.nanoTime() - startTime;
        System.out.println("Runtime for " + date + ": " + duration/1000000000 + " seconds");
    }

    private static boolean validInput(String[] input){
        boolean validInput = true;

        if (input.length > 2){
            System.out.println("Please input date and url only");
            validInput = false;
        }

        if (input.length < 2){
            System.out.println("Please input date and url");
            validInput = false;
        }

        return validInput;
    }

    //Check if input date is valid
    private static boolean validDate (String date){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use the format 'yyyy-MM-dd'.");
            return false;
        }
    }

    //Check if we can get all the data we need from the provided URL
    private static boolean validURL (String baseURL){
        try {
            String[] paths = new String[]{"/restaurants", "/centralArea", "/noFlyZones", "/orders"};

            for (String path: paths){
                URL url = new URL(baseURL + path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Error accessing URL: " + url + ". Status code: " + connection.getResponseCode());
                    return false;
                }
            }
            return true;

        } catch (IOException e) {
            System.out.println("Invalid URL");
            return false;
        }
    }
}
