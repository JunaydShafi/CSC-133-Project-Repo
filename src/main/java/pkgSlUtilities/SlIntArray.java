package pkgSlUtilities;

import java.io.*;
import java.util.Arrays;

public class SlIntArray {
    public int[][] arrayData;
    public int NUM_ROWS;
    public int NUM_COLS;

    public SlIntArray(int rows, int cols) {
        arrayData = new int[rows][cols];
        NUM_ROWS = rows;
        NUM_COLS = cols;
    }

    public int[][] loadFile(String dataFilePath) {
        try (BufferedReader myReader = new BufferedReader(new FileReader(dataFilePath))) {
            String inputLine;

            int DEFAULT_VALUE = Integer.parseInt(myReader.readLine());
            int MIN_VALUE = DEFAULT_VALUE;
            int MAX_VALUE = DEFAULT_VALUE;

            inputLine = myReader.readLine();
            int[] rowCol = Arrays.stream(inputLine.trim().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            NUM_ROWS = rowCol[0];
            NUM_COLS = rowCol[1];

            arrayData = new int[NUM_ROWS][NUM_COLS];

            // Fill the array with default value
            for (int r = 0; r < NUM_ROWS; r++) {
                for (int c = 0; c < NUM_COLS; c++) {
                    arrayData[r][c] = DEFAULT_VALUE;
                }
            }

            // Fill specific data
            while ((inputLine = myReader.readLine()) != null) {
                if (inputLine.trim().isEmpty()) continue;
                int[] readRow = Arrays.stream(inputLine.trim().split("\\s+"))
                        .mapToInt(Integer::parseInt)
                        .toArray();

                int curRow = readRow[0];
                int colOffset = readRow[1];
                int readColOffset = 2;
                int curWriteCol = colOffset;

                while (curWriteCol < NUM_COLS && readColOffset < readRow.length) {
                    arrayData[curRow][curWriteCol++] = readRow[readColOffset++];
                }
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            arrayData = null;
        }
        return arrayData;
    }

    public int[][] getClone() {
        int[][] clone = new int[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            System.arraycopy(arrayData[r], 0, clone[r], 0, NUM_COLS);
        }
        return clone;
    }
}
