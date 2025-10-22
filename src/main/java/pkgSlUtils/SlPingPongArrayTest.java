package pkgSlUtils;

import static pkgDriver.ULTResult.*;

public class SlPingPongArrayTest {
    final static boolean ULT_DEBUG = true;

    // Fisher-Yates test - 1
    public static boolean ULT_200() {
        boolean retVal = false;
        int myRows = 20, myCols = 20;
        SlPingPongArray myPPA = new SlPingPongArray(myRows, myCols, 0, 0, 77);
        for (int row = 0; row < myRows; ++row) {
            for (int col = 0; col < myCols; ++col) {
                myPPA.nextCellArray.arrayData[row][col] = row * myCols + col;
            }  //  for(int col = 0; col < myPPA.NUM_COLS; ++col)
        }  //  for(int row = 0; row < myPPA.NUM_ROWS; ++row)
        myPPA.swapLiveAndNext();

        if (ULT_DEBUG) {
            System.out.println("Before randomization: ");
            myPPA.printArray();
        }  //  if (ULT_DEBUG)
        final int maxTrials = 10;
        for (int trialNum = 0; trialNum < maxTrials; ++trialNum) {
            myPPA.randomizeViaFisherYatesKnuth();
            myPPA.swapLiveAndNext();
            if (ULT_DEBUG) {
                System.out.println("\nAfter randomization : " + trialNum);
                myPPA.printArray();
            }  //  if (ULT_DEBUG)

        }  //  for(int trialNum = 0; trialNum < maxTrials; ++trialNum)
        retVal = true;
        return returnTestResult("ULT_200", retVal);
    }  //  private static boolean ULT_1(...)

    // Reading data from file
    public static boolean ULT_220() {
        boolean retVal = true;
        SlPingPongArray myPPA = new SlPingPongArray(7, 7);
        myPPA.nextCellArray.loadFile("C:\\Users\\jeoju\\Documents\\Second Semester Fall 2025\\CSC 133 Object Oriented Graph\\ASSIGNMENT_4\\ult_input_1.txt");

        myPPA.swapLiveAndNext();
        if (ULT_DEBUG) {
            System.out.println("Input array:");
            myPPA.printArray();
        }  //  if (ULT_DEBUG)

        int[][] validationData = {
                {1, 1, 0, 1, 0, 1, 9},
                {9, 1, 1, 0, 0, 1, 1},
                {1, 2, 3, 4, 5, 6, 7},
                {9, 9, 9, 0, 1, 9, 9},
                {9, 9, 9, 9, 1, 1, 1},
                {9, 9, 9, 9, 9, 1, 0},
                {9, 9, 9, 9, 9, 9, 0} };

        for (int row = 0; row < validationData.length && retVal; ++row) {
            for (int col = 0; col < validationData[0].length; ++col) {
                if (validationData[row][col] != myPPA.liveCellArray.arrayData[row][col]) {
                    retVal = false;
                    break;
                }  //  if (validationData[row][col] != myPPA.liveCellArray.arrayData[row][col])
            }  //  for(int col = 0; col < validationData[0].length; ++col)
        }  //  for(int row = 0; row < validationData.length; ++row)

        return returnTestResult("ULT_220", retVal);
    }  //  private static boolean ULT_201(...)

    // NearestNeighbor array computation
    public static boolean ULT_240() {
        boolean retVal = false;
        String testLabel = "ULT_240";
        SlPingPongArray myPPA = new SlPingPongArray(7, 7);
        myPPA.loadFile("ult_input_2.txt");
        System.out.println("Input array:");
        myPPA.printArray();

        for (int row = 0; row < myPPA.NUM_ROWS; ++row) {
            for (int col = 0; col < myPPA.NUM_COLS; ++col) {
                myPPA.setCell(row, col, myPPA.getNNSum(row, col));
            }  //  for(int col = 0; col < myPPA.NUM_COLS; ++col)
        }  //  for(int row = 0; row < myPPA.NUM_ROWS; ++row)
        myPPA.swapLiveAndNext();
        System.out.println("NN Count:");
        myPPA.printArray();

        retVal = true;
        return returnTestResult(testLabel, retVal);
    }  //  private static boolean ULT_202(...)

    public static void main(String[] args) {
        System.out.println("Running SlPingPongArray Tests...\n");

        ULT_200();
        ULT_220();
        ULT_240();

        System.out.println("\nAll tests complete.");
    }
}  //  class SlPingPongArrayTest

