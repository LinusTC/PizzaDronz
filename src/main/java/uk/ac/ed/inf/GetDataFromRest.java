package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;

public class GetDataFromRest {
    private static String baseURL;

    public static void setBaseUrl(String baseUrl) {
        baseURL = baseUrl;
    }

    //Get data as string, use ObjectMapper later to map the strings.
    private static String getData(String path) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(baseURL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    char[] buffer = new char[1024];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, bytesRead);
                    }
                }
            } else {
                // Handle HTTP error, if necessary
                throw new IOException("HTTP error: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public static Restaurant[] getRestaurantsData (){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(getData("/restaurants"), Restaurant[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public static NamedRegion getCentralAreaData (){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(getData("/centralArea"), NamedRegion.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static NamedRegion[] getNoFlyZones (){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(getData("/noFlyZones"), NamedRegion[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Order[] getOrderData (){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            return objectMapper.readValue(getData("/orders"), Order[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Order[] getOrdersOnDay (LocalDate date){

        Order[] orders = getOrderData();

        return Arrays.stream(orders)
                .filter(order -> order.getOrderDate().equals(date))
                .toArray(Order[]::new);
    }
}
