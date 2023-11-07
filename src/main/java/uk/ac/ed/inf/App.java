package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class App
{
    public static void main(String[] args){

//        String dateInput = args[0];
//        String urlInput = args[1];

        String dateInput = "2023-09-01";
        String urlInput = "https://ilp-rest.azurewebsites.net";

        if (!validDate(dateInput)){
            System.out.println("Invalid date format. Please use the format 'yyyy-MM-dd'.");
            System.exit(0);
        }

        if(!validURL(urlInput)){
            System.out.println("Invalid URL. Use https://ilp-rest.azurewebsites.net.");
            System.exit(0);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateInput, formatter);
        GetDataFromRest.setBaseUrl(urlInput);

        Order[] allOrdersDate = GetDataFromRest.getOrdersOnDay(date);
        Restaurant[] restaurantData = GetDataFromRest.getRestaurantsData();

        for (Order order: allOrdersDate){
            new OrderValidator().validateOrder(order, restaurantData);
        }

        Order[] validOrdersDate = OrderValidator.getValidOrdersOnDay(date, allOrdersDate);

        for(Order order: validOrdersDate){
            PathCharter.totalMovesPerOrder(order);
        }

        CreateJsonDocuments.createDeliveries(date, allOrdersDate);

    }

    //Check if input date is valid
    private static boolean validDate (String date){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
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
                    return false;
                }
            }
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
