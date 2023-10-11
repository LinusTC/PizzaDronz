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
        LngLat[] vertices = region.vertices();

        return isPointInRegion(vertices, position);
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {

        if (angle == 999){
            return startPosition;
        }

        double nextX = startPosition.lng() + (.00015 * Math.cos(Math.toRadians(angle)));
        double nextY = startPosition.lat() + (.00015 * Math.sin(Math.toRadians(angle)));

        if (Math.abs(nextX) < 1e-15) {
            nextX = 0.0;
        }
        if (Math.abs(nextY) < 1e-15) {
            nextY = 0.0;
        }
        return new LngLat(nextX, nextY);
    }

    @Override
    public boolean isInCentralArea(LngLat point, NamedRegion centralArea) {
        return false;
    }


    //Point in polygon alg found online
    static boolean isPointInRegion(LngLat[] vertices, LngLat position ){
        boolean temp = false;

        double x = position.lng();
        double y = position.lat();

        int numVertices = vertices.length;

        double[] xVertices = new double[numVertices];
        double[] yVertices = new double[numVertices];


        for(int k = 0; k < numVertices;k++){
            xVertices[k] = vertices[k].lng();
            yVertices[k] = vertices[k].lat();
        }

        for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
            if (((yVertices[i] > y) != (yVertices[j] > y)) &&
                    (x < (xVertices[j] - xVertices[i]) * (y - yVertices[i]) / (yVertices[j] - yVertices[i]) + xVertices[i])) {
                temp = !temp;
            }
        }
        return temp;
    }
}
