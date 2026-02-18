package uk.ac.aber.cs31920.assignment.gridProcessor;

public class GraphEdge {

    public ConnectionNode first;
    public ConnectionNode second;
    int flow = 0 ;

    int id = -1; // determines "colour" of edge
    boolean seen = false;

    public GraphEdge(ConnectionNode f, ConnectionNode s){
        this.first = f;
        this.second = s;
    }

    public ConnectionNode getOther(ConnectionNode in){
        if(in == first) return second;
        else if(in == second) return first;

        System.out.println("returning null on graph edge node");
        return null;
    }

    public String toString() {
       return "(" + first.firstX + ", " + first.firstY + ")-(" + second.firstX + ", " + second.firstY + ") ";
    }
}
