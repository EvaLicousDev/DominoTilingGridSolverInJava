package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.Comparator;

public class NodeConnectionComparator implements Comparator<ConnectionNode> {

@Override
public int compare(ConnectionNode o1, ConnectionNode o2) {
    if (o1.edges.size() > o2.edges.size()) return 1;
    else if (o1.edges.size() < o2.edges.size()) return -1;
    else return 0;
}
}
