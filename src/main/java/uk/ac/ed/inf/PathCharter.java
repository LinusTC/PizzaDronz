package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.Arrays;
import java.util.Objects;

public class PathCharter {
    public static LngLat closestEdge (LngLat startingPoint) throws JsonProcessingException {

        //Check if starting point is in Central
        if(new LngLatHandler().isInCentralArea(startingPoint,GetDataFromRest.getCentralAreaData())){
            return null;
        }

        double closestX, closestY;

        double x = startingPoint.lng();
        double y = startingPoint.lat();

        //Using the following code to find max/min lng-lat values.
        LngLat[] vertices = GetDataFromRest.getCentralAreaData().vertices();
        double xMin = vertices[0].lng();
        double xMax = vertices[2].lng();
        double yMin = vertices[2].lat();
        double yMax = vertices[0].lat();

        if (x < xMin){
            closestX = xMin;
            if(y < yMin){
                closestY = yMin;
            }
            else closestY = Math.min(y, yMax);
        }
        else if (x <= xMax) {
            closestX = x;
            if(y < yMin){
                closestY = yMin;
            }
            else{
                closestY = yMax;
            }
        }
        else{
            closestX = xMax;
            if(y < yMin){
                closestY = yMin;
            }
            else closestY = Math.min(y, yMax);

        }
        return new LngLat(closestX,closestY);
    }

}
