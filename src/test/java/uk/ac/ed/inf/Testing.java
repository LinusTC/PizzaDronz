package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

import static uk.ac.ed.inf.GetDataFromRest.*;
import static uk.ac.ed.inf.GetDataFromRest.getCentralAreaData;

public class Testing {
    public static void main( String[] args ){
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        Restaurant[] restaurants = getRestaurantsData();

        //Test to get Central Area
        System.out.println(getCentralAreaData().name());
        for (LngLat vertex: getCentralAreaData().vertices()){
            System.out.print(vertex);
        }
        System.out.println();

        //Test to get NoFlyZones
        for(NamedRegion regions: getNoFlyZones()) {
            System.out.println();
            System.out.println(regions.name());
            for (LngLat vertex : regions.vertices()) {
                System.out.print(vertex);
            }
            System.out.println();
        }
        System.out.println();

        //Test to get all Restaurant info
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

        //Test to Validate order number i
        Order order = getOrderData()[3];
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

        //Test to get all orders/validOrders on specific date
        LocalDate date = LocalDate.of(202,9,1);
        Order[] ordersOnDate = getOrdersOnDay(date);
        Order[] validOrdersOnDate = getValidOrdersOnDay(date);
        for (Order orders: ordersOnDate){
            System.out.println("Pizzas Number:" + orders.getOrderNo());
            System.out.println("Pizzas Date:" + orders.getOrderDate());
            System.out.println("Pizzas Status:" + orders.getOrderStatus());
            System.out.println("Pizzas Validation Code:" + orders.getOrderValidationCode());
            System.out.println("Pizzas Price:" + orders.getPriceTotalInPence());
            System.out.println("Pizzas include:" + Arrays.toString(orders.getPizzasInOrder()));
            System.out.println("Credit Card Number:" + orders.getCreditCardInformation().getCreditCardNumber());
            System.out.println("Credit Card CVV:" + orders.getCreditCardInformation().getCvv());
            System.out.println("Credit Card Expiry Date:" + orders.getCreditCardInformation().getCreditCardExpiry());
            System.out.println();
        }
        System.out.println("Number of Orders on " + date + " is: " + ordersOnDate.length);
        System.out.println();

        //LngLatHandler Tests
        LngLat sampleInCenter = new LngLat(-3.1913256901223406, 55.94571464973686);
        LngLat sampleNotInCenter = new LngLat(-3.1933472253281536, 55.94576526828331);
        LngLatHandler handler = new LngLatHandler();
        System.out.println(handler.distanceTo(sampleInCenter, PathCharter.closestEdge(sampleInCenter)));
        System.out.println(handler.distanceTo(sampleNotInCenter, PathCharter.closestEdge(sampleNotInCenter)));
        System.out.println(handler.isCloseTo(sampleInCenter,new LngLat(sampleInCenter.lng() + 0.0002, sampleInCenter.lat())));
        System.out.println(handler.isInCentralArea(sampleInCenter, getCentralAreaData()));
        System.out.println(handler.isInCentralArea(sampleNotInCenter, getCentralAreaData()));

    }
}
