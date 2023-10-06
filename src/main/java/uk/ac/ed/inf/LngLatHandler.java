package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

public class LngLatHandler implements LngLatHandling{
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        double x1 = startPosition.lng();
        double y1 = startPosition.lat();

        double x2 = endPosition.lng();
        double y2 = endPosition.lat();

        double x = (x1-x2);
        double y = (y1-y2);

        return Math.sqrt(x*x + y*y);
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return (distanceTo(startPosition, otherPosition) < 0.00015);
    }

    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        return false;
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        return null;
    }

    @Override
    public boolean isInCentralArea(LngLat point, NamedRegion centralArea) {
        return false;
    }
}
