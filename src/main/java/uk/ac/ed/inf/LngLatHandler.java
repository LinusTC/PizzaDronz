package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
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
        return (distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE);
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

        double nextX = startPosition.lng() + (SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(Math.toRadians(angle)));
        double nextY = startPosition.lat() + (SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(Math.toRadians(angle)));

        return new LngLat(nextX, nextY);
    }

    @Override
    public boolean isInCentralArea(LngLat point, NamedRegion centralArea) {
        if (centralArea == null) {
            throw new IllegalArgumentException("the named region is null");
        } else if (!centralArea.name().equals("central")) {
            throw new IllegalArgumentException("the named region: " + centralArea.name() + " is not valid - must be: central");
        } else {
            return this.isInRegion(point, centralArea);
        }
    }


    //Point in polygon algorithm found online
    private static boolean isPointInRegion(LngLat[] vertices, LngLat position){
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
