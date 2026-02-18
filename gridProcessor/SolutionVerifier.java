package uk.ac.aber.cs31920.assignment.gridProcessor;

import java.util.ArrayList;
public class SolutionVerifier {
    /**
     * A simple class to output the grid and validate the correctness of the algorithms tile choices
     *
     * If the input grid was
     *
     * 00
     * 00
     *
     * Then a correct solution displays as
     *
     * 11
     * 11
     *
     * And a solution with too many edges displays as
     *
     * 12
     * 12
     *
     * In the example above we chose 3 edges for a 2x2 grid, resulting in 0 zeroes receiveing double coverage
     * If any zeroes display then they do not have and tile coverage.
     */
    char[][] inputMatrix;
    ArrayList<GraphEdge> solution;

    public SolutionVerifier(char[][] inputMatrix, ArrayList<GraphEdge> solution){
        this.inputMatrix = inputMatrix;
        this.solution = solution;
        int[][] outputGrid = new int[inputMatrix.length][inputMatrix.length];
        for(int y = 0; y < inputMatrix.length; y++) {
            for(int x = 0; x < inputMatrix.length; x++) {
                outputGrid[y][x] = Character.getNumericValue(inputMatrix[y][x]);
            }
        }
        for(GraphEdge n : solution) {
            outputGrid[n.first.firstY][n.first.firstX]++;
            outputGrid[n.second.firstY][n.second.firstX]++;
        }
        for(int y = 0; y < inputMatrix.length; y++) {
            for (int x = 0; x < inputMatrix.length; x++) {
                System.out.print(" " + outputGrid[y][x]);
            }
            System.out.println();
        }
        for(int y = 0; y < inputMatrix.length; y++) {
            for(int x = 0; x < inputMatrix.length; x++) {
                if(outputGrid[y][x] != 1)
                {
                    System.out.println("Invalid solution");
                    System.exit(0);
                }
            }
        }
        System.out.println("All good!");
        System.exit(1);
    }
}
