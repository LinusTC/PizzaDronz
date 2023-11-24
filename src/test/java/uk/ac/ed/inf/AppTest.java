package uk.ac.ed.inf;

import org.junit.After;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    LocalDate startDate = LocalDate.of(2023, 9, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 28);
    Set<LocalDate> randomDates = new HashSet<>();
    String projectDir = System.getProperty("user.dir") + "/resultfiles/";

    // This test that files are generated in /resultfiles/ and deletes them after testing
    @Test
    public void testFileGeneration(){
        // Generate 8 random dates
        while (randomDates.size() < 8) {
            LocalDate date = generateRandomDate(startDate, endDate);
            randomDates.add(date);
        }

        for (LocalDate date : randomDates) {
            String dateStr = date.toString();

            App.main(new String[]{dateStr, "https://ilp-rest.azurewebsites.net"});

            String flightPathFileName = projectDir + "flightpath-" + dateStr + ".json";
            String droneFileName = projectDir + "drone-" + dateStr + ".geojson";
            String deliveriesFileName = projectDir + "deliveries-" + dateStr + ".json";

            assertTrue("Flight path file for " + dateStr + " not created", Files.exists(Paths.get(flightPathFileName)));
            assertTrue("Drone file for " + dateStr + " not created", Files.exists(Paths.get(droneFileName)));
            assertTrue("Deliveries file for " + dateStr + " not created", Files.exists(Paths.get(deliveriesFileName)));
        }
    }

    @After
    public void cleanup() throws IOException {
        for (LocalDate date : randomDates) {
            String flightPathFileName = projectDir + "flightpath-" + date + ".json";
            String droneFileName = projectDir + "drone-" + date + ".geojson";
            String deliveriesFileName = projectDir + "deliveries-" + date + ".json";

            Files.deleteIfExists(Paths.get(flightPathFileName));
            Files.deleteIfExists(Paths.get(droneFileName));
            Files.deleteIfExists(Paths.get(deliveriesFileName));

            System.out.println("Files for " + date + " were successfully created and deleted after testing.");
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