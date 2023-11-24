package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.Order;
import java.util.regex.*;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import java.text.*;
import java.util.*;


public class OrderValidator implements OrderValidation {
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {

        String creditCardNumber = orderToValidate.getCreditCardInformation().getCreditCardNumber();
        if(!checkLuhn(creditCardNumber)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        String expiryDate = orderToValidate.getCreditCardInformation().getCreditCardExpiry();
        if (!validExpirationDate(expiryDate)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        String CVV = orderToValidate.getCreditCardInformation().getCvv();
        if (!isCVVValid(CVV)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        if (!totalAccurate(orderToValidate)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        if (!validPizzas(orderToValidate, definedRestaurants)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        if (!validPizzaCount(orderToValidate)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        if (!fromSameRestaurant(orderToValidate,definedRestaurants)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        if (!restaurantIsOpen(orderToValidate, definedRestaurants)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        return orderToValidate;

    }

    //This is Luhn's algorithm to check credit card number validity
    private static boolean checkLuhn(String cardNum){
        int digits = cardNum.length();

        if (digits != 16 || !cardNum.matches("\\d+")) {
            return false;
        }
        int numSum = 0;
        boolean temp = false;

        for (int i = digits - 1; i >= 0; i--) {
            int d = cardNum.charAt(i) - '0';

            if (temp) {
                d = d * 2;
            }

            numSum += d / 10;
            numSum += d % 10;

            temp = !temp;

        }
        return (numSum % 10 == 0);
    }

    //check expiration date validity
    private static boolean validExpirationDate (String expDate) {

        if (expDate == null){
            return false;
        }

        String regex = "^(0[1-9]|1[0-2])/([0-9]{2})$";

        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(expDate);

        if (!m.matches()){
            return false;
        }
        else{
            SimpleDateFormat SDF = new SimpleDateFormat("MM/yy");
            SDF.setLenient(false);

            Date expiry;

            try {
                expiry = SDF.parse(expDate);
            } catch (ParseException e) {
                return false;
            }

            return expiry.after(new Date());
        }
    }

    //Check CVV validity
    private static boolean isCVVValid (String cvv){
        String regex = "^[0-9]{3}$";

        Pattern p = Pattern.compile(regex);

        if (cvv == null){
            return false;
        }

        Matcher m = p.matcher(cvv);

        return m.matches();
    }

    //Check if price total is accurate
    private static boolean totalAccurate (Order order){

        int customerTotal = order.getPriceTotalInPence();
        int deliveryFee = SystemConstants.ORDER_CHARGE_IN_PENCE;
        int menuTotal = 0;
        for (Pizza pizza: order.getPizzasInOrder()){
            menuTotal += pizza.priceInPence();
        }


        return customerTotal==(menuTotal + deliveryFee);
    }

    //Check number of pizzas is valid
    private static boolean validPizzaCount (Order order){
        return order.getPizzasInOrder().length >= 1 && order.getPizzasInOrder().length <= SystemConstants.MAX_PIZZAS_PER_ORDER;
    }

    //Find which restaurant a pizza is from
    public static Restaurant findPizzaRestaurant(Pizza pizza, Restaurant[] definedRestaurants){
        for (Restaurant restaurant: definedRestaurants){
            for(Pizza menuPizza: restaurant.menu()){
                if(menuPizza.name().equals(pizza.name())){
                    return restaurant;
                }
            }
        }
        return null;
    }

    //Check if all pizzas are defined
    private static boolean validPizzas (Order order, Restaurant[] definedRestaurants){

        Pizza[] pizzas  = order.getPizzasInOrder();

        for (Pizza pizza: pizzas){

            boolean pizzaFound = findPizzaRestaurant(pizza, definedRestaurants) != null;

            if (!pizzaFound){
                return false;
            }
        }
        return true;
    }

    //Check if all pizzas are from the same restaurant
    private static boolean fromSameRestaurant (Order order, Restaurant[] definedRestaurants){

        if (order.getPizzasInOrder().length == 1){
            return true;
        }

        Pizza[] pizzas  = order.getPizzasInOrder();

        Pizza firstPizza = pizzas[0];

        Restaurant firstPizzaRestaurant = findPizzaRestaurant(firstPizza, definedRestaurants);

        for (int i = 1; i< pizzas.length  ;i++){
            Pizza currentPizza = pizzas[i];
            if (firstPizzaRestaurant != findPizzaRestaurant(currentPizza,definedRestaurants)){
                return false;
            }
        }

        return true;
    }

    //Check if restaurant is open on order day
    private static boolean restaurantIsOpen (Order order, Restaurant[] definedRestaurants){

        Pizza[] pizzas  = order.getPizzasInOrder();

        Pizza firstPizza = pizzas[0];

        Restaurant firstPizzaRestaurant = findPizzaRestaurant(firstPizza, definedRestaurants);

        assert firstPizzaRestaurant != null;

        return Arrays.stream(firstPizzaRestaurant.openingDays())
                .anyMatch(day -> day == order.getOrderDate().getDayOfWeek());
    }

    public static Order[] filterValidOrders (Order[] orders){

        return Arrays.stream(orders)
                .filter(order -> order.getOrderStatus().equals(OrderStatus.VALID_BUT_NOT_DELIVERED))
                .toArray(Order[]::new);
    }
}