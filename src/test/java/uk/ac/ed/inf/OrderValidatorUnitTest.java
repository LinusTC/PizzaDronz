package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;

import java.time.DayOfWeek;
import java.time.LocalDate;

import uk.ac.ed.inf.ilp.data.Restaurant;

public class OrderValidatorUnitTest
{
    public static void main( String[] args )
    {
        CreditCardInformation creditCardSample = new CreditCardInformation();

        creditCardSample.setCvv("952");
        creditCardSample.setCreditCardNumber("378282246310005");
        creditCardSample.setCreditCardExpiry("06/28");


        LocalDate date = LocalDate.of(2023,9,1);

        Pizza[] pizzas = { new Pizza("Super Cheese", 1400), new Pizza("All Shrooms", 900)};

        Restaurant[] restaurants = new Restaurant[4];
        restaurants[0] = new Restaurant("Civerinos Slice"
                ,new LngLat(-3.1912869215011597,55.945535152517735)
                ,new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}
                ,new Pizza[]{new Pizza("Margarita",1000), new Pizza("Calzone", 1400)});

        restaurants[1] = new Restaurant("Sora Lella Vegan Restaurant"
                ,new LngLat(-3.202541470527649,55.943284737579376)
                ,new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}
                ,new Pizza[]{new Pizza("Meat Lover",1400), new Pizza("Vegan Delight", 1100)});

        restaurants[2] = new Restaurant("Domino's Pizza - Edinburgh - Southside"
                ,new LngLat(-3.1838572025299072,55.94449876875712)
                ,new DayOfWeek[]{DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}
                ,new Pizza[]{new Pizza("Super Cheese",1400), new Pizza("All Shrooms", 900)});

        restaurants[3] = new Restaurant("Sodeberg Pavillion"
                ,new LngLat(-3.1940174102783203,55.94390696616939)
                ,new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}
                ,new Pizza[]{new Pizza("Proper Pizza",1400), new Pizza("Pineapple & Ham & Cheese", 900)});


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
}
