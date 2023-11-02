package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.time.DayOfWeek;
import java.util.Arrays;
import uk.ac.ed.inf.ilp.data.Restaurant;


public class App
{
    public static void main( String[] args ) throws JsonProcessingException {

        Restaurant[] restaurants = restaurantsData();

        System.out.println(getCentralAreaData().name());
        for (LngLat vertex: getCentralAreaData().vertices()){
            System.out.print(vertex);
        }
        System.out.println();

        for(NamedRegion regions: getNoFlyZones()) {
            System.out.println();
            System.out.println(regions.name());
            for (LngLat vertex : regions.vertices()) {
                System.out.print(vertex);
            }
            System.out.println();
        }
        System.out.println();

        for (Restaurant restaurant : restaurants) {
            System.out.println("Restaurant Name: " + restaurant.name());
            System.out.println("Location: " + restaurant.location());
            System.out.print("Opening Days: ");
            for (DayOfWeek day : restaurant.openingDays()) {
                System.out.print(day + " ");
            }
            System.out.println("\nMenu Items:");
            for (Pizza pizza : restaurant.menu()) {
                System.out.println("  - " + pizza.name() + " - Price: " + pizza.priceInPence() + " pence");
            }
            System.out.println();
        }
        System.out.println();


        Order order = getOrderData()[2];
        for (int i = 0; i < 2; i++) {
            System.out.println("Pizzas Number:" + order.getOrderNo());
            System.out.println("Pizzas Date:" + order.getOrderDate());
            System.out.println("Pizzas Status:" + order.getOrderStatus());
            System.out.println("Pizzas Validation Code:" + order.getOrderValidationCode());
            System.out.println("Pizzas Price:" + order.getPriceTotalInPence());
            System.out.println("Pizzas include:" + Arrays.toString(order.getPizzasInOrder()));
            System.out.println("Credit Card Number:" + order.getCreditCardInformation().getCreditCardNumber());
            System.out.println("Credit Card CVV:" + order.getCreditCardInformation().getCvv());
            System.out.println("Credit Card Expiry Date:" + order.getCreditCardInformation().getCreditCardExpiry());
            System.out.println();

            OrderValidator validator = new OrderValidator();
            validator.validateOrder(order, restaurants);
        }
        System.out.println();

    }

    public static String getData (String url){
        WebClient.Builder builder = WebClient.builder();

        return builder
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public static Restaurant[] restaurantsData () throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(getData("https://ilp-rest.azurewebsites.net/restaurants"), Restaurant[].class);
    }

    public static NamedRegion getCentralAreaData () throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getData("https://ilp-rest.azurewebsites.net/centralArea"), NamedRegion.class);
    }

    public static NamedRegion[] getNoFlyZones () throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getData("https://ilp-rest.azurewebsites.net/noFlyZones"), NamedRegion[].class);
    }

    public static Order[] getOrderData () throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(getData("https://ilp-rest.azurewebsites.net/orders"), Order[].class);
    }
}
