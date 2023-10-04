package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

public class LngLatHandler implements LngLatHandling{
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return 0;
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return false;
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
