package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.Order;
import java.util.regex.*;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.OrderStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderValidator implements OrderValidation {
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {

        String creditCardNumber = orderToValidate.getCreditCardInformation().getCreditCardNumber();
        if(!checkLuhn(creditCardNumber)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
        }

        String expiryDate = orderToValidate.getCreditCardInformation().getCreditCardExpiry();
        if (isExpired(expiryDate)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
        }

        String CVV = orderToValidate.getCreditCardInformation().getCvv();
        if (!isCVVValid(CVV)){
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
        }



        else{
            orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        }


        return orderToValidate;
    }

    //This is Luhn's algorithm to check credit card number validity
    static boolean checkLuhn(String cardNum){
        int digits = cardNum.length();

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
    static boolean isExpired (String expDate) {

        SimpleDateFormat SDF = new SimpleDateFormat("MM/yy");
        SDF.setLenient(false);

        Date expiry;

        try {
            expiry = SDF.parse(expDate);
        } catch (ParseException e) {
            return false;
        }

        return expiry.before(new Date());
    }

    //Check CVV validity
    static boolean isCVVValid (String cvv){
        String regex = "^[0-9]{3}$";

        Pattern p = Pattern.compile(regex);

        if (cvv == null){
            return false;
        }

        Matcher m = p.matcher(cvv);

        return m.matches();
    }
}