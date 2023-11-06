package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public record Node(LngLat location, Node parent, double heuristics) {
    public Node(LngLat location, Node parent, double heuristics){
        this.location = location;
        this.parent = parent;
        this.heuristics = heuristics;
    }

    public LngLat location(){
        return this.location;
    }

    public Node parent(){
        return this.parent;
    }
}
