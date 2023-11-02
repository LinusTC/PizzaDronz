package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Scanner;

import uk.ac.ed.inf.ilp.data.Restaurant;

import javax.swing.*;

public class OrderValidatorUnitTest
{
    public static void main( String[] args ) throws JsonProcessingException {
        CreditCardInformation creditCardSample = new CreditCardInformation();

        creditCardSample.setCvv("952");
        creditCardSample.setCreditCardNumber("378282246310005");
        creditCardSample.setCreditCardExpiry("06/28");

        Restaurant[] restaurants = restaurantsData();

        LocalDate date = LocalDate.of(2023,9,1);

        Pizza[] pizzas = { new Pizza("Super Cheese", 1400), new Pizza("All Shrooms", 900)};

        Order sampleOrder = new Order();
        sampleOrder.setOrderStatus(OrderStatus.UNDEFINED);
        sampleOrder.setOrderValidationCode(OrderValidationCode.UNDEFINED);
        sampleOrder.setOrderNo("19514FE0");
        sampleOrder.setPriceTotalInPence(2400);
        sampleOrder.setCreditCardInformation(creditCardSample);
        sampleOrder.setOrderDate(date);
        sampleOrder.setPizzasInOrder(pizzas);

        OrderValidator validator = new OrderValidator();

        validator.validateOrder(sampleOrder,restaurants);

        System.out.println(sampleOrder.getOrderStatus());
        System.out.println(sampleOrder.getOrderValidationCode());
    }

    public static String getData (String url){
        WebClient.Builder builder = WebClient.builder();

        return builder
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(4 * 1024 * 1024))
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
}
