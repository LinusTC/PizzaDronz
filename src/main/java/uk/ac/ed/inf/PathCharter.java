package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public class PathCharter {
    public static LngLat closestEdge (LngLat startingPoint){

        //Check if starting point is already in central
        if(new LngLatHandler().isInCentralArea(startingPoint,GetDataFromRest.getCentralAreaData())){
            return startingPoint;
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

        //Finds the closest point on central box
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
