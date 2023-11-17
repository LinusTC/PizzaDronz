package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.data.*;
import static org.junit.Assert.*;

public class GetDataTest {

    @Test
    public void getRestaurantData() {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");
        Restaurant[] restaurants = GetDataFromRest.getRestaurantsData();
        assertNotNull("The array of restaurants should not be null", restaurants);

        for (Restaurant restaurant : restaurants) {
            assertNotNull("Restaurant name should not be null", restaurant.name());
            assertNotNull("Restaurant location should not be null", restaurant.location());
            assertNotNull("Restaurant opening days should not be null", restaurant.openingDays());
            assertNotNull("Restaurant menu should not be null", restaurant.menu());
        }
    }

    @Test
    public void getCentralArea() {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");
        NamedRegion central = GetDataFromRest.getCentralAreaData();

        assertNotNull("Region should not be null", central);
        assertNotNull("Region name should not be null", central.name());
        assertNotNull("Region vertices should not be null", central.vertices());

    }

    @Test
    public void getNoFlyZones() {
        GetDataFromRest.setBaseUrl("https://ilp-rest.azurewebsites.net");
        NamedRegion[] noFlyZones = GetDataFromRest.getNoFlyZones();

        assertNotNull("The array of noFlyZones should not be null", noFlyZones);

        for(NamedRegion zone: noFlyZones){
            assertNotNull("Region should not be null", zone);
            assertNotNull("Region name should not be null", zone.name());
            assertNotNull("Region vertices should not be null", zone.vertices());
        }
    }
}
