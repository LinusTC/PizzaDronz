package uk.ac.ed.inf;


import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import static uk.ac.ed.inf.GetDataFromRest.*;
public class CreateJsonDocuments {
    public static void createDeliveries (LocalDate date) {
        Order[] ordersOnDate = getOrdersOnDay(date);
        JSONArray orders = new JSONArray();

        for (Order order : ordersOnDate) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("orderNo", order.getOrderNo());
            jsonObject.put("orderStatus", order.getOrderStatus());
            jsonObject.put("orderValidationCode", order.getOrderValidationCode());
            jsonObject.put("costInPence", order.getPriceTotalInPence());

            orders.put(jsonObject);
        }

        try {
            FileWriter file = new FileWriter(new File("resultfiles", "deliveries-" + date + ".json"));
            file.write(orders.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFlightPath () {

    }

    public static void createDrone () {

    }
}
