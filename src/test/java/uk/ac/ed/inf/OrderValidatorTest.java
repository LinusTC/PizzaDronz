package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.*;
import uk.ac.ed.inf.ilp.data.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Random;

public class OrderValidatorTest {

    @Test
    public void testOrderStatus() {
        testOrders(0);
    }

    @Test
    public void testValidationCode(){
        testOrders(1);
    }

    private void testOrders(int number){
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");

        //Pick 4 random dates
        LocalDate[] randomDates = new LocalDate[4];
        LocalDate startDate = LocalDate.of(2023, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 28);
        for (int i = 0; i < 4; i++) {
            LocalDate date = generateRandomDate(startDate, endDate);
            randomDates[i] = date;
        }

        for(LocalDate date: randomDates){
            Restaurant[] restaurants = GetDataFromRest.getRestaurantsData();
            Order[] ordersOnDay = GetDataFromRest.getOrdersOnDay(date);

            for(Order order: ordersOnDay){
                new OrderValidator().validateOrder(order,restaurants);

                if(number == 0){
                    assertNotNull("Order status should not be null", order.getOrderStatus());
                    assertNotEquals("Order status should not be undefined", OrderStatus.UNDEFINED, order.getOrderStatus());
                }

                else{
                    assertNotNull("Order validation code should not be null", order.getOrderValidationCode());
                    assertNotEquals("Order validation code should not be undefined", OrderValidationCode.UNDEFINED, order.getOrderValidationCode());
                }
            }
        }
    }

    private LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        Random random = new Random();
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomEpochDay);
    }
}
