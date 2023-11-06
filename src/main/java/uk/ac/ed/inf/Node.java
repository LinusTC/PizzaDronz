package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public record Node(LngLat location, Node parent, double distanceFromStart, double heuristics, double totalCost) {
    public Node(LngLat location, Node parent, double distanceFromStart, double heuristics, double totalCost){
        this.location = location;
        this.parent = parent;
        this.distanceFromStart = 0;
        this.heuristics = 0;
        this.totalCost = 0;
    }

    public LngLat location(){
        return this.location;
    }

    public Node parent(){
        return this.parent;
    }
}
