package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.time.LocalDate;
import java.util.Arrays;

public class GetDataFromRest {
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

    public  static Order[] getOrdersOnDay (LocalDate date) throws JsonProcessingException {

        Order[] orders = getOrderData();

        return Arrays.stream(orders)
                .filter(order -> order.getOrderDate().equals(date))
                .toArray(Order[]::new);
    }
}
