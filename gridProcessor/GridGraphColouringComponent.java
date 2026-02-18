package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.*;

public class GridGraphColouringComponent {
    /**
     * This class takes the output graph from MatrixArrayConverter and first uses graph colouring to determine if the grid is solvable
     * and if it is it then passes the grid to MaxFlowBipartyColourGraph matcher to find a valid tiling
     *
     * ArrayLists are used for fast access, and priority queues where we want to access data in a specific order.
     * The member variables use NodeConnectionComparator for this, which sorts ConnectionNodes by number of edges / possible tiles.
     *
     * An ArrayList of PriorityQueues of ConnectionNodes is used to get subGraphs from the matrix
     * This is because some matrices will have several seperate groups of zeroes, so we take advantage of that
     * and break those grids into smaller tiling tasks
     *
     * - for any subgraphs if colour 1 and colour 2 of the ConnectionNodes are uneven we determine the grid as not solvable
     */
    PriorityQueue<ConnectionNode> allConnections = new PriorityQueue<ConnectionNode>(100, new NodeConnectionComparator());
    ArrayList<GraphEdge> connections;
    ArrayList<GraphEdge> preProcessedChoices;
    //ArrayList<ArrayList<GraphEdge>> connectedEdges = new ArrayList<>();
    ArrayList<PriorityQueue<ConnectionNode>> connectedNodes = new ArrayList<>();

    public ArrayList<GraphEdge> finalChoices = new ArrayList<>();

    int colour0 = 0;
    int colour1 = 0;

    public ArrayList<GraphEdge> getEdges() {
        return connections;
    }
    public GridGraphColouringComponent(ArrayList<GraphEdge> choices, ArrayList<ConnectionNode> connections, ArrayList<GraphEdge> edges){
        this.connections = edges;
        this.allConnections.addAll(connections);
        this.preProcessedChoices = choices;
        if(!this.colour())
        {
            System.out.print("No solution");
        }
    }

    /**
     * Recursive function that uses dfs on connection node to colour them
     * @param connectedNodesInternal
     * @param connectedEdgesInternal
     * @param currentNode
     * @param previousColour
     * @param calldepth
     */
    private void makeChoices(PriorityQueue<ConnectionNode> connectedNodesInternal, ArrayList<GraphEdge> connectedEdgesInternal, ConnectionNode currentNode, int previousColour, int calldepth) {
        allConnections.remove(currentNode);
        if(currentNode.colour == -1){
            if(previousColour == 1){
                currentNode.colour = 0;
                colour0++;
            } else if(previousColour == 0){
                currentNode.colour = 1;
                colour1++;
            }
            for(GraphEdge nodeEdge : currentNode.edges){
                if(nodeEdge.seen == false){
                    nodeEdge.seen = true;
                    connectedEdgesInternal.add(nodeEdge);
                }
            }
            connectedNodesInternal.add(currentNode);
            for(ConnectionNode n : currentNode.implyingNodes){
                makeChoices(connectedNodesInternal, connectedEdgesInternal, n, currentNode.colour,calldepth +1);
            }
        } else if(currentNode.colour == previousColour) {
            System.out.println("No solution");
            System.exit(1);
        }
    }

    /**
     * function which calls makeChoices for colouring and asses the result to see if the grid is solvable
     * @return true if solvable
     */
    private boolean colour(){
        boolean hasSolution = true;
        int colour1Mem = 0;
        int colour2Mem = 0;
        while(!allConnections.isEmpty())
        {
            ArrayList<GraphEdge> connectedEdgesInternal = new ArrayList<>();
            PriorityQueue<ConnectionNode> connectionNodesInternal = new PriorityQueue<>(new NodeConnectionComparator());
            makeChoices(connectionNodesInternal, connectedEdgesInternal, allConnections.peek(), 1, 0);
            if(colour0 != colour1){
                //this section of the graph does not contain an evenly matched set of zeroes
                System.out.println("No solution");
                System.exit(0);
            }
            //this.connectedEdges.add(connectedEdgesInternal);
            this.connectedNodes.add(connectionNodesInternal);
            colour1Mem += colour0;
            colour2Mem += colour1;
            colour0 = 0;
            colour1 = 0;
        }

        //sanity check
        if(colour1Mem != colour2Mem){
            System.out.println("No solution");
            System.exit(0);
        }

        //if we have not terminated the program the grid is solvable, so we now process the tiling for the sub graphs
        for(int i = 0; i < this.connectedNodes.size(); i++){
            MaxFlowBipartieColourGraphMatcher m = new MaxFlowBipartieColourGraphMatcher(this.preProcessedChoices, this.connectedNodes.get(i));
            finalChoices.addAll(m.getChoices());
        }
        finalChoices.addAll(this.preProcessedChoices);

        return hasSolution;
    }
}
