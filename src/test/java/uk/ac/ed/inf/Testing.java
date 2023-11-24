package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.*;
import uk.ac.ed.inf.ilp.data.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Random;

public class Testing {
    public static void main(String[] args) {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");
        LocalDate date = LocalDate.of(2023, 9, 6);
        Restaurant[] restaurants = GetDataFromRest.getRestaurantsData();
        Order[] ordersOnDay = GetDataFromRest.getOrdersOnDay(date);

        for(Order order:ordersOnDay){
            System.out.println(order.getCreditCardInformation().getCreditCardNumber());
        }

        for(Order order: ordersOnDay){
            new OrderValidator().validateOrder(order,restaurants);
        }

        for(Order order:ordersOnDay){
            System.out.println(order.getCreditCardInformation().getCreditCardNumber());
        }
    }
}
