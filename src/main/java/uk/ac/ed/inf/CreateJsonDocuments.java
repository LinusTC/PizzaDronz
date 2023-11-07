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
    public static void createDeliveries (LocalDate date, Order[] ordersOnDate) {
        JSONArray orders = new JSONArray();

        for (Order order : ordersOnDate) {
            JSONObject object = new JSONObject();

            object.put("orderNo", order.getOrderNo());
            object.put("orderStatus", order.getOrderStatus());
            object.put("orderValidationCode", order.getOrderValidationCode());
            object.put("costInPence", order.getPriceTotalInPence());

            orders.put(object);
        }

        try {
            FileWriter file = new FileWriter(new File("resultfiles", "deliveries-" + date + ".json"));
            file.write(orders.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFlightPath (LocalDate date, Order[] validOrdersOnDate) {
        JSONArray movesOfOrders = new JSONArray();
        Move[] path = PathCharter.totalMoves(validOrdersOnDate);

        for(Move move: path){
            JSONObject object = new JSONObject();

            object.put("orderNo", move.orderNo());
            object.put("fromLongitude", move.fromLng());
            object.put("fromLatitude", move.fromLat());
            object.put("angle", move.angle());
            object.put("toLongitude", move.toLng());
            object.put("toLatitude", move.toLat());

            movesOfOrders.put(object);
        }

        try {
            FileWriter file = new FileWriter(new File("resultfiles", "flightpath-" + date + ".json"));
            file.write(movesOfOrders.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDrone (LocalDate date) {

    }
}
