package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.PriorityQueue;

public class ConnectionNode {
    /**
     * For a 2x2 matrix if
     *      A   B
     * a    0   0
     * b    0   0
     *
     * firstX and firstY represent the X and Y coordinates for one 0 in the grid
     *
     * A node representing the connection aB would therefor have the following values
     * firstX = 1, firstY = 0
     */
    int priority = 999; // how many valid edges this node still has
    int colour = -1; // 0 or 1
    int firstX;
    int firstY;

    int flow = 0;

    public boolean isSourceOrSink = false;
    public boolean rightCon = false;
    public boolean leftCon = false;
    public boolean upCon = false;
    public boolean downCon = false;

    public boolean seen = false;


    PriorityQueue<ConnectionNode> implyingNodes = new PriorityQueue<ConnectionNode>(4, new NodeConnectionComparator());
    PriorityQueue<GraphEdge> edges = new PriorityQueue<>(4, new ConnectionNodeComparetor());

    public Optional<GraphEdge> colour1 = Optional.empty();
    public Optional<GraphEdge> colour2 = Optional.empty();
    public Optional<GraphEdge> colour10 = Optional.empty();
    public Optional<GraphEdge> colour20 = Optional.empty();

    public void addEdge(GraphEdge edge){
        this.edges.add(edge);
    }
    public void addEdge(int colour, GraphEdge edge){
        if(colour == 1){
            this.colour1 = Optional.of(edge);
        } else if( colour == 2){
            this.colour2 = Optional.of(edge);
        } else if( colour == 10){
            this.colour10 = Optional.of(edge);
        } else if( colour == 20){
            this.colour20 = Optional.of(edge);
        }
    }
    public ConnectionNode(int firstX, int firstY){
        this.firstX = firstX;
        this.firstY = firstY;

    }

    public void addConnection(ConnectionNode connected) {
        this.implyingNodes.add(connected);
    }

    public String toString() {
        return "(" + firstX + ", " + firstY + ")";
    }
}
