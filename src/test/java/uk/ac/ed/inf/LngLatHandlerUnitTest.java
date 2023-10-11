package uk.ac.ed.inf;


import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

public class LngLatHandlerUnitTest {
    public static void main( String[] args ) {
        LngLat startPosition = new LngLat(0.0000, 0.0000);
        LngLat endPosition = new LngLat(0.0001, 0.0000);

        NamedRegion region = new NamedRegion("exampleRegion",
                new LngLat[]{
                        new LngLat(0.0000,0.0000)
                        ,new LngLat(0.0001, 0.0000)
                        ,new LngLat(0.0001,0.0001)
                        ,new LngLat(0.0000,0.0001)});

        LngLatHandler lngLatHandler = new LngLatHandler();
        double distance = lngLatHandler.distanceTo(startPosition,endPosition);
        boolean isCloseTo = lngLatHandler.isCloseTo(startPosition,endPosition);
        LngLat nextPosition = lngLatHandler.nextPosition(startPosition, 999);
        boolean isInRegion = lngLatHandler.isInRegion(new LngLat(0.00005,0.00005), region);

        System.out.println(distance);
        System.out.println(isCloseTo);
        System.out.println(nextPosition);
        System.out.println(isInRegion);
    }
}
