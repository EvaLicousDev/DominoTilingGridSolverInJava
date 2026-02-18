package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.Comparator;

public class ConnectionNodeComparetor implements Comparator<GraphEdge> {

    @Override
    public int compare(GraphEdge o1, GraphEdge o2) {
        if (o1.flow > o2.flow) return 1;
        else if (o1.flow < o2.flow) return -1;
        else return 0;
    }
}
