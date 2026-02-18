package uk.ac.aber.cs31920.assignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class takes an input source file of type .txt
 * We use a simple scanner for this with the assumption all files provided will always be of type .txt
 * It checks if the first row is a positive numeric value which is used to describe the remaining
 * NxN matrix of 1s and 0s
 * If the file format violates the input constraints for the solving of the matrix as
 * outlined in the assignment for CS319020 we will get hit an assert to indicate the issue
 */
public class GridTextInputFileReader {
    //class attributes
    String fileName;
    int    inputMatrixSize = 0;

    public GridTextInputFileReader(String inputFileName){
        this.fileName = inputFileName;
    }

    public boolean isValid() {
        return (inputMatrixSize >= 2);
    }
    //private methodes
    /**
     * Methode which determines if the file contains a valid input
     * Info: For the input matrix to be valid we need at least a 2x2 matrix
     */
    private void isFirstNumValid(int firstInputNum) {
        assert (firstInputNum > 1) : "Value of the first line in the text file is " + firstInputNum + " and is therefor invalid. We need at least a 2x2 matrix to provide an appropriate problem space";
    }

    /**
     * Methode that takes the scanner and value from the first line of the input file and
     * creates an output matrix of strings of size "matrixSize x matrixSize"
     * This methode also asserts if the input read does not match the expected criteria
     *
     * @param reader a scanner used to open the simple txt file
     * @param matrixSize the size of square matrix row x columns, which determines how large the output string array needs to be
     * @return a string array which contains the square matrix that needs solving
     */
    private char[][] readInputMatrixOfTxtFile(Scanner reader, int matrixSize){
        //skip number
        reader.nextLine();

        //create string array with size of expected matrix rows, reading and verifying data from file into the array line by line
        String[] readData = new String[matrixSize];
        for(int matrixLineIndex = 0; matrixLineIndex < matrixSize; matrixLineIndex++) {
            assert(reader.hasNextLine()) : "Failed to read line index " + matrixLineIndex + " due to no more lines existing in the file " + fileName;
            readData[matrixLineIndex] = reader.nextLine();
            assert(readData[matrixLineIndex].length() == matrixSize) : "Input line in file " + fileName + " for index " + matrixLineIndex + " is of length " + readData[matrixLineIndex].length() + ", not " + matrixSize + " as expected.";
        }

        char[][] data = new char[matrixSize][matrixSize];
        int rowIndex = 0;
        for(String s : readData) {
            data[rowIndex] = readData[rowIndex].toCharArray();
            rowIndex++;
        }
        return data;
    }

    /**
     * Methode which uses the class attributes to read out the input file using a scanner
     * It first validates the first line as positive integer input and then reads all consecutive lines
     * validating the length to be equal to the value of the first line as well as number of lines
     * @return string array with equal capacity and length of individual strings determined by the integer in the first line of the file
     */
    public char[][] readInputFile()
    {
        File myFile = new File(fileName);
        try (Scanner reader = new Scanner(myFile)) {
            //read the first line as integer and check it is valid
            int firstLineVal = -1;
            if(reader.hasNextInt())
            {
                 firstLineVal = reader.nextInt();
                 isFirstNumValid(firstLineVal);
                 this.inputMatrixSize = firstLineVal;
            }
            assert(firstLineVal != -1) : "Could not accurately read the first input line of the .txt file with name " + this.fileName;

            //create return matrix
            char[][] outputMatrix = readInputMatrixOfTxtFile(reader, this.inputMatrixSize);
            reader.close();
            return outputMatrix;
        } catch (FileNotFoundException e) {
            System.out.println("Could not use scanner to read input file with name: ");
            System.out.println(fileName);
            e.printStackTrace();
        }
        return new char[0][0];
    }
}
