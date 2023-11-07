package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public record Move(int orderNo, LngLat startingPoint, LngLat endingPoint) {

    public Move(int orderNo, LngLat startingPoint, LngLat endingPoint){
        this.orderNo = orderNo;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
    }
}
