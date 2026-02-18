package uk.ac.aber.cs31920.assignment.gridProcessor;
import java.util.ArrayList;

public class MatrixArrayConverter {
    /**
     * This class takes the input read of the txt files in form of a char[][] and processes it for further evaluation
     * We convert the input matrix to a planar graph with all 0s being ConnectionNodes, and add all possible tiles in the form
     * of GraphEdges to the Connection nodes
     *
     * For fast access we store everything in ArrayLists, as we do not need to sort anything yet.
     *
     * We also "colour" the graph edges
     * @see uk.ac.aber.cs31920.assignment.GridTextInputFileReader
     */
    char[][] inputMatrix;
    ArrayList<ConnectionNode> allZeroes = new ArrayList<ConnectionNode>();
    ArrayList<GraphEdge> allEdges = new ArrayList<GraphEdge>();

    ArrayList<GraphEdge> edgeChoices = new ArrayList<GraphEdge>();
    ConnectionNode[] connectionAbove;
    ConnectionNode nodeToTheLeft = new ConnectionNode(0, 0);
//-----------------------------------------------------------------------------------------------------------------

    public MatrixArrayConverter(char[][] inputMatrix) {
        assert(inputMatrix.length != 0): "StringArrayConverter initialised with empty string array";
        this.inputMatrix = new char[inputMatrix.length][inputMatrix.length];
        for(int rowIndex = 0; rowIndex < inputMatrix.length; rowIndex++){
            this.inputMatrix[rowIndex] = inputMatrix[rowIndex];
        }
        this.connectionAbove = new ConnectionNode[inputMatrix.length];
        int nodeCount = this.createGraphData();
        if(nodeCount % 2 != 0) {
            System.out.println("No solution");
            System.exit(0);
        }
    }

    public ArrayList<ConnectionNode> getAllZeroes(){
        return allZeroes;
    }
    public ArrayList<GraphEdge> getAllEdges(){
        return allEdges;
    }

    /**
     * This method creates all nodes for matrix
     * - as we are iterating from top left to bottom right we only need to consider the
     *   0s on the right and below our 0 to get a complete collection of nodes
     * - if any lone 0s exist we exit the tile
     * - we also colour the edges
     * @param xCoordinate of current position of 0 in matrix
     * @param yCoordinate of current position of 0 in matrix
     * @see ConnectionNode for how nodes are defined
     */
    private GraphEdge createConnectionNodes(int nodeCount, int xCoordinate, int yCoordinate) {
        int numberOfXY = inputMatrix.length;
        int connections = 0;

        boolean topConnection = false;
        boolean leftConnection = false;
        boolean rightConnection = false;
        boolean bottomConnection = false;

        ConnectionNode node = new ConnectionNode(xCoordinate, yCoordinate);

        //check left
        if(xCoordinate > 0) {
            leftConnection = ((Character.getNumericValue(inputMatrix[yCoordinate][xCoordinate-1])) == 0);
            if(leftConnection) connections++;
        }

        //check above
        if(yCoordinate > 0){
            topConnection = ((Character.getNumericValue(inputMatrix[yCoordinate-1][xCoordinate])) == 0);
            if(topConnection) connections++;
        }

        //check right if possible
        if(xCoordinate < (numberOfXY - 1)) {
            int nextRight = Character.getNumericValue(inputMatrix[yCoordinate][xCoordinate+1]);
            if(nextRight == 0)
            {
                //create and emplace a node for the connection
                rightConnection = true;
                connections++;
            }
        }

        //check below if possible
        if(yCoordinate < (numberOfXY - 1)){
            int nextLower = Character.getNumericValue(inputMatrix[yCoordinate+1][xCoordinate]);
            if(nextLower == 0)
            {
                //create and emplace a node for the connection
                bottomConnection = true;
                connections++;
            }
        }

        if(connections == 0) //lone zero on grid so no solution
        {
            System.out.println("No Solution");
            System.exit(1);
        }

        if(leftConnection)
        {
            GraphEdge horizontal = new GraphEdge(nodeToTheLeft, node);
            allEdges.add(horizontal);
            nodeToTheLeft.addEdge(horizontal);
            node.addEdge(horizontal);
            if(xCoordinate%2 == 0){
                horizontal.id = 1;
                nodeToTheLeft.addEdge(1, horizontal);
                node.addEdge(1, horizontal);
            } else {
                horizontal.id = 2;
                nodeToTheLeft.addEdge(2, horizontal);
                node.addEdge(2, horizontal);
            }

            node.addConnection(nodeToTheLeft);
            nodeToTheLeft.addConnection(node);
        }

        if(topConnection)
        {
            GraphEdge vertical = new GraphEdge(connectionAbove[xCoordinate], node);
            allEdges.add(vertical);
            if(yCoordinate%2 == 0){
                vertical.id = 10;
                connectionAbove[xCoordinate].addEdge(10, vertical);
                node.addEdge(10, vertical);
            } else {
                vertical.id = 20;
                connectionAbove[xCoordinate].addEdge(20, vertical);
                node.addEdge(20, vertical);
            }
            connectionAbove[xCoordinate].addEdge(vertical);
            node.addEdge(vertical);

            node.addConnection(connectionAbove[xCoordinate]);
            connectionAbove[xCoordinate].addConnection(node);
        }

        node.priority = connections;
        node.rightCon = rightConnection;
        node.leftCon = leftConnection;
        node.upCon = topConnection;
        node.downCon = bottomConnection;
        allZeroes.add(node);

        //hold on to node if we need it for the next zeroes
        if(rightConnection)
        {
            nodeToTheLeft = allZeroes.getLast();
        }
        if(bottomConnection)
        {
            connectionAbove[xCoordinate] = allZeroes.getLast();
        }

        //if we know we have an edge that we have to choose we already choose it now
        //in this case we return it, and it gets added in "createGraphData"
        if(connections == 1 && node.edges.peek() != null){
            return node.edges.peek();
        }
        return null;
    }

    public ArrayList<GraphEdge> getEdgeChoices(){
        return edgeChoices;
    }

    /**
     * Take the input matrix and create a representation of all available spaces a where a possible connection can go
     * - we track the zeros in zeroCount as they represent available tiles
     *   Note: if the number of zeroes is uneven at the end there is no solution, so we print "no solution" as output
     */
    private int createGraphData(){
        int zeroCount = 0;
        // for every row iterate over each individual value in the matrix
        // if 0 then we process connections
        for(int yIndex = 0; yIndex < inputMatrix.length; yIndex++){
            for(int xIndex = 0; xIndex < inputMatrix.length; xIndex++){
                if((Character.getNumericValue(inputMatrix[yIndex][xIndex]) == 0)) {
                    GraphEdge choice = createConnectionNodes(zeroCount, xIndex, yIndex);
                    if(choice != null && !edgeChoices.contains(choice)){
                        edgeChoices.add(choice);
                    }
                    zeroCount++;
                }
            }
        }

        if(zeroCount%2 != 0) {
            // uneven number of available positions
            // as all tiles take 2 positions this grid is not solvable
            System.out.println("No solution");
            System.exit(0);
        }

        return zeroCount;
    }
    public void printAllConnections() {
        for(ConnectionNode node : allZeroes)
        {
            System.out.print(node.toString());
        }
    }
}
