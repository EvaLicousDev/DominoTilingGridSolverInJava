package uk.ac.aber.cs31920.assignment;
import uk.ac.aber.cs31920.assignment.gridProcessor.*;
import uk.ac.aber.cs31920.assignment.testInputs.ConnectionsForTestinputs;
import uk.ac.aber.cs31920.assignment.testInputs.InputReaderTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The GridSolverMain class is the class used to combine all functionality of the remaining code
 * to solve the task outlined in the assignment for CS31920 Advanced Algorithms - Aberystwyth University 2025
 *
 * @author student ID 210103410
*/
public class GridSolverMain {

    /**
     * Some tests used during development to verify the working of the basic components
     * @param checkInputReading outputs input matrices in "testInputs" folder
     * @param printConnections outputs the GraphEdges
     */
    static public void runTests(boolean checkInputReading, boolean printConnections)
    {
        //Tests GridTestInputFileReader class with all input files stored in specific path
        if(checkInputReading){ new InputReaderTest(); }
        if(printConnections){ new ConnectionsForTestinputs(); };
    }

    /*
    Solves input01-09 correctly, as well as custom inputs
     */
    public static void main(String[] args) {
        //String tempInputName = "uk/ac/aber/cs31920/assignment/testInputs/TestInputTxtFiles/input10.txt";
        //GridTextInputFileReader reader = new GridTextInputFileReader(tempInputName);

        //Read input path of command line
        Scanner scanner = new Scanner(System.in);
        String inputPath = "";
        while(inputPath.isEmpty()){
            System.out.println("Please type in the input .txt filepath you would like to process:");
            inputPath = scanner.nextLine();
        }

        GridTextInputFileReader reader = new GridTextInputFileReader(inputPath);
        // read file and get matrix from file
        char[][] inputMatrix = reader.readInputFile();
        assert(inputMatrix.length != 0) : "Input File Reader did not return valid matrix as matrix is empty";

        //process input matrix
        if(inputMatrix != null) {
            //read the data from input and create graph
            MatrixArrayConverter graphData = new MatrixArrayConverter(inputMatrix);
            ArrayList<ConnectionNode> allConnections = graphData.getAllZeroes();
            ArrayList<GraphEdge> chosen = graphData.getEdgeChoices();

            //colour graph and process tiling if possible
            GridGraphColouringComponent solver = new GridGraphColouringComponent(chosen, allConnections, graphData.getAllEdges());
            for(GraphEdge g : solver.finalChoices){
                System.out.print(g.toString());
            }

            //used in development to verify solution:
            //new SolutionVerifier(inputMatrix, solver.finalChoices);
        } else {
            System.out.println("Something went wrong with reading in the data. ");
        }

        //test Functions for development
        //runTests(false, false);
    }
}

