package uk.ac.ed.inf;

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
    public static boolean validDate (String date){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validURL (String URL){
        return URL.equals("https://ilp-rest.azurewebsites.net");
    }
}
