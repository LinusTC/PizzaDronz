package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.time.LocalDate;
import java.util.Arrays;

public class GetDataFromRest {
    private static final String BASE_URL = "https://ilp-rest.azurewebsites.net";
    public static String getData (String path){
        WebClient.Builder builder = WebClient.builder();

        return builder
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build()
                .get()
                .uri(BASE_URL + path)
                .retrieve()
                .bodyToMono(String.class)
                .block();
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

    public  static Order[] getOrdersOnDay (LocalDate date){

        Order[] orders = getOrderData();

        return Arrays.stream(orders)
                .filter(order -> order.getOrderDate().equals(date))
                .toArray(Order[]::new);
    }

    public  static Order[] getValidOrdersOnDay (LocalDate date){

        Order[] orders = getOrdersOnDay(date);

        for (Order order: orders){
            new OrderValidator().validateOrder(order, getRestaurantsData());
        }

        return Arrays.stream(orders)
                .filter(order -> order.getOrderStatus().equals(OrderStatus.VALID_BUT_NOT_DELIVERED))
                .toArray(Order[]::new);
    }
}
