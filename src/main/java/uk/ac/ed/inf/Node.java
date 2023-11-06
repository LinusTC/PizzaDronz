package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public record Node(LngLat location, Node parent, double distanceFromStart, double heuristics, double totalCost) {
    public Node(LngLat location, Node parent, double distanceFromStart, double heuristics, double totalCost){
        this.location = location;
        this.parent = parent;
        this.distanceFromStart = distanceFromStart;
        this.heuristics = heuristics;
        this.totalCost = totalCost;
    }

    public LngLat location(){
        return this.location;
    }

    public Node parent(){
        return this.parent;
    }
}
