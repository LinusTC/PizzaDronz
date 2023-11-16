package uk.ac.ed.inf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import uk.ac.ed.inf.ilp.data.Order;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class CreateJsonDocuments {
    public static void createDeliveries (LocalDate date, Order[] ordersOnDate) {
        JsonArray orders = new JsonArray();

        for (Order order : ordersOnDate) {
            JsonObject object = new JsonObject();

            object.addProperty("orderNo", order.getOrderNo());
            object.addProperty("orderStatus", order.getOrderStatus().toString());
            object.addProperty("orderValidationCode", order.getOrderValidationCode().toString());
            object.addProperty("costInPence", order.getPriceTotalInPence());

            orders.add(object);
        }

        try {
            FileWriter file = new FileWriter(new File("resultfiles", "deliveries-" + date + ".json"));
            file.write(orders.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFlightPath (LocalDate date, Move[] path) {
        JsonArray movesOfOrders = new JsonArray();

        for(Move move: path){
            JsonObject object = new JsonObject();

            object.addProperty("orderNo", move.orderNo());
            object.addProperty("fromLongitude", move.fromLng());
            object.addProperty("fromLatitude", move.fromLat());
            object.addProperty("angle", move.angle());
            object.addProperty("toLongitude", move.toLng());
            object.addProperty("toLatitude", move.toLat());

            movesOfOrders.add(object);
        }

        try {
            FileWriter file = new FileWriter(new File("resultfiles", "flightpath-" + date + ".json"));
            file.write(movesOfOrders.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDrone(LocalDate date, Move[] path) {

        try {
            FileWriter geojson = new FileWriter("resultfiles/drone-" + date + ".geojson");

            geojson.write("{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": { \"coordinates\": [");

            // Write the coordinates from the 'path' array
            for (int i = 0; i < path.length; i++) {
                Move move = path[i];
                String formattedCoordinates = String.format("[%.16f,%.16f]", move.fromLng(), move.fromLat());
                geojson.write(formattedCoordinates);

                if (i < path.length - 1) {
                    geojson.write(",");
                }
            }

            geojson.write("], \"type\": \"LineString\" } }");

            geojson.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
