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

//        //Test to get Central Area
//        System.out.println(getCentralAreaData().name());
//        for (LngLat vertex: getCentralAreaData().vertices()){
//            System.out.print(vertex);
//        }
//        System.out.println();
//
//        //Test to get NoFlyZones
//        for(NamedRegion regions: getNoFlyZones()) {
//            System.out.println();
//            System.out.println(regions.name());
//            for (LngLat vertex : regions.vertices()) {
//                System.out.print(vertex);
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        //Test to get all Restaurant info
//        for (Restaurant restaurant : restaurants) {
//            System.out.println("Restaurant Name: " + restaurant.name());
//            System.out.println("Location: " + restaurant.location());
//            System.out.print("Opening Days: ");
//            for (DayOfWeek day : restaurant.openingDays()) {
//                System.out.print(day + " ");
//            }
//            System.out.println("\nMenu Items:");
//            for (Pizza pizza : restaurant.menu()) {
//                System.out.println("  - " + pizza.name() + " - Price: " + pizza.priceInPence() + " pence");
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        //Test to Validate order number i
//        Order order = getOrderData()[3];
//        for (int i = 0; i < 2; i++) {
//            System.out.println("Pizzas Number:" + order.getOrderNo());
//            System.out.println("Pizzas Date:" + order.getOrderDate());
//            System.out.println("Pizzas Status:" + order.getOrderStatus());
//            System.out.println("Pizzas Validation Code:" + order.getOrderValidationCode());
//            System.out.println("Pizzas Price:" + order.getPriceTotalInPence());
//            System.out.println("Pizzas include:" + Arrays.toString(order.getPizzasInOrder()));
//            System.out.println("Credit Card Number:" + order.getCreditCardInformation().getCreditCardNumber());
//            System.out.println("Credit Card CVV:" + order.getCreditCardInformation().getCvv());
//            System.out.println("Credit Card Expiry Date:" + order.getCreditCardInformation().getCreditCardExpiry());
//            System.out.println();
//
//            OrderValidator validator = new OrderValidator();
//            validator.validateOrder(order, restaurants);
//        }
//        System.out.println();
//
//        //Test to get all orders/validOrders on specific date
//        LocalDate date = LocalDate.of(2023,9,1);
//        Order[] ordersOnDate = getOrdersOnDay(date);
//        for (Order orders: ordersOnDate){
//            System.out.println("Pizzas Number:" + orders.getOrderNo());
//            System.out.println("Pizzas Date:" + orders.getOrderDate());
//            System.out.println("Pizzas Status:" + orders.getOrderStatus());
//            System.out.println("Pizzas Validation Code:" + orders.getOrderValidationCode());
//            System.out.println("Pizzas Price:" + orders.getPriceTotalInPence());
//            System.out.println("Pizzas include:" + Arrays.toString(orders.getPizzasInOrder()));
//            System.out.println("Credit Card Number:" + orders.getCreditCardInformation().getCreditCardNumber());
//            System.out.println("Credit Card CVV:" + orders.getCreditCardInformation().getCvv());
//            System.out.println("Credit Card Expiry Date:" + orders.getCreditCardInformation().getCreditCardExpiry());
//            System.out.println();
//        }
//        System.out.println("Number of Orders on " + date + " is: " + ordersOnDate.length);
//        System.out.println();
//
//        //LngLatHandler Tests
//        LngLat sampleInCenter = new LngLat(-3.1913256901223406, 55.94571464973686);
//        LngLat sampleNotInCenter = new LngLat(-3.1933472253281536, 55.94576526828331);
//        LngLatHandler handler = new LngLatHandler();
//        System.out.println(handler.isCloseTo(sampleInCenter,new LngLat(sampleInCenter.lng() + 0.0002, sampleInCenter.lat())));
//        System.out.println(handler.isInCentralArea(sampleInCenter, getCentralAreaData()));
//        System.out.println(handler.isInCentralArea(sampleNotInCenter, getCentralAreaData()));
//
        //test to get moves
        LocalDate date = LocalDate.of(2023,9,1);
        Order[] ordersOnDate = getOrdersOnDay(date);

        for (Order order: ordersOnDate){
            new OrderValidator().validateOrder(order, restaurants);
        }
        Order[] validOrdersDate = OrderValidator.filterValidOrders(ordersOnDate);

        Move[] path = PathCharter.totalMoves(validOrdersDate);
        for(Move move: path){
            System.out.println("[" + move.fromLng() + "," + move.fromLat() + "],");
        }
        System.out.println(path.length);

        for (int i = 1; i < path.length; i++) {
            double fromLng1 = path[i - 1].fromLng();
            double fromLat1 = path[i - 1].fromLat();

            double fromLng2 = path[i].fromLng();
            double fromLat2 = path[i].fromLat();

            double distance = new LngLatHandler().distanceTo(new LngLat(fromLng1, fromLat1), new LngLat(fromLng2, fromLat2));

            System.out.println("[" + fromLng1 + "," + fromLat1 + "] to [" + fromLng2 + "," + fromLat2 + "], Distance: " + distance);
        }

//        LngLat appleton = new LngLat(-3.1869, 	55.9445);
//        LngLat rest = new LngLat(-3.1913, 55.9455);
//
//        double step = new LngLatHandler().distanceTo(appleton, rest)/3;
//        PathCharter.PathPoint[] un = PathCharter.modAStarAlg(appleton,rest, step);
//
//        PathCharter.PathPoint[] refined2 = PathCharter.fullyRefine(un, step, rest);
//
//        System.out.println(Arrays.toString(refined2));
//        System.out.println(refined2.length);
//
//        for (int i = 1; i < refined2.length - 1;i++){
//            LngLat prev = refined2[i - 1].location();
//            LngLat curr = refined2[i].location();
//            double dist = new LngLatHandler().distanceTo(prev, curr);
//            System.out.println(dist);
//        }
//
//        for (PathCharter.PathPoint point: refined2){
//            System.out.println("[" + point.location().lng() + "," + point.location().lat() + "],");
//        }
//
//        LngLat temp = new LngLat(-3.1912955655638755,55.945375300759984);
//        System.out.println(new LngLatHandler().isCloseTo(temp, rest));
    }
}

