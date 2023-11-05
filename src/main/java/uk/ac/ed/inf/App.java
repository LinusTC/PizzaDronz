package uk.ac.ed.inf;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class App
{
    public static void main(String[] args){
        System.out.println("Hello World");

        String date = args[0];
        String url = args[1];

        if (!validDate(date)){
            System.out.println("Invalid date format. Please use the format 'yyyy-MM-dd'.");
            System.exit(0);
        }

        if(!validURL(url)){
            System.out.println("Invalid URL. Use https://ilp-rest.azurewebsites.net.");
            System.exit(0);
        }

        else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            GetDataFromRest.setBaseUrl(url);
        }

    }

    //Check if input date is valid
    public static boolean validDate (String date){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Check if we can get all the data we need from the provided URL
    public static boolean validURL (String baseURL){
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
