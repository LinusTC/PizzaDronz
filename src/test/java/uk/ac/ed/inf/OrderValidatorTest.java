package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.*;
import uk.ac.ed.inf.ilp.data.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class OrderValidatorTest {
    @Test
    public void testOrders(){
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        //Pick 4 random dates
        LocalDate startDate = LocalDate.of(2023, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 28);
        Set<LocalDate> randomDateSet = new HashSet<>();

        while (randomDateSet.size() < 8) {
            LocalDate date = generateRandomDate(startDate, endDate);
            randomDateSet.add(date);
        }

        for(LocalDate date : randomDateSet){
            Restaurant[] restaurants = GetDataFromRest.getRestaurantsData();
            Order[] ordersOnDay = GetDataFromRest.getOrdersOnDay(date);

            for(Order order: ordersOnDay){
                new OrderValidator().validateOrder(order,restaurants);

                assertNotNull("Order status should not be null", order.getOrderStatus());
                assertNotEquals("Order status should not be undefined", OrderStatus.UNDEFINED, order.getOrderStatus());

                assertNotNull("Order validation code should not be null", order.getOrderValidationCode());
                assertNotEquals("Order validation code should not be undefined", OrderValidationCode.UNDEFINED, order.getOrderValidationCode());

            }
            testAllValidationCodesPresent(ordersOnDay, date);
        }
    }

    private void testAllValidationCodesPresent(Order[] ordersOnDay, LocalDate date) {
        boolean isNoError = false;
        boolean isCardNumberInvalid = false;
        boolean isExpiryDateInvalid = false;
        boolean isCvvInvalid = false;
        boolean isTotalIncorrect = false;
        boolean isPizzaNotDefined = false;
        boolean isMaxPizzaCountExceeded = false;
        boolean isPizzaFromMultipleRestaurants = false;
        boolean isRestaurantClosed = false;

        for (Order order : ordersOnDay) {
            switch (order.getOrderValidationCode()) {
                case NO_ERROR:
                    isNoError = true;
                    break;
                case CARD_NUMBER_INVALID:
                    isCardNumberInvalid = true;
                    break;
                case EXPIRY_DATE_INVALID:
                    isExpiryDateInvalid = true;
                    break;
                case CVV_INVALID:
                    isCvvInvalid = true;
                    break;
                case TOTAL_INCORRECT:
                    isTotalIncorrect = true;
                    break;
                case PIZZA_NOT_DEFINED:
                    isPizzaNotDefined = true;
                    break;
                case MAX_PIZZA_COUNT_EXCEEDED:
                    isMaxPizzaCountExceeded = true;
                    break;
                case PIZZA_FROM_MULTIPLE_RESTAURANTS:
                    isPizzaFromMultipleRestaurants = true;
                    break;
                case RESTAURANT_CLOSED:
                    isRestaurantClosed = true;
                    break;
            }
        }

        assertTrue("OrderValidationCode NO_ERROR should be present for date " + date, isNoError);
        assertTrue("OrderValidationCode CARD_NUMBER_INVALID should be present for date " + date, isCardNumberInvalid);
        assertTrue("OrderValidationCode EXPIRY_DATE_INVALID should be present for date " + date, isExpiryDateInvalid);
        assertTrue("OrderValidationCode CVV_INVALID should be present for date " + date, isCvvInvalid);
        assertTrue("OrderValidationCode TOTAL_INCORRECT should be present for date " + date, isTotalIncorrect);
        assertTrue("OrderValidationCode PIZZA_NOT_DEFINED should be present for date " + date, isPizzaNotDefined);
        assertTrue("OrderValidationCode MAX_PIZZA_COUNT_EXCEEDED should be present for date " + date, isMaxPizzaCountExceeded);
        assertTrue("OrderValidationCode PIZZA_FROM_MULTIPLE_RESTAURANTS should be present for date " + date, isPizzaFromMultipleRestaurants);
        assertTrue("OrderValidationCode RESTAURANT_CLOSED should be present for date " + date, isRestaurantClosed);
    }

    private LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        Random random = new Random();
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomEpochDay);
    }
}
