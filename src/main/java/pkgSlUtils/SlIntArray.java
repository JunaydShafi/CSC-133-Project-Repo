package pkgSlUtils;

import java.io.*;
import java.util.Arrays;

public class SlIntArray {
    public int[][] arrayData;

    public SlIntArray(int rows, int cols) {
        arrayData = new int[rows][cols];
    }

    public void loadFile(String dataFilePath) {
        try (BufferedReader myReader = new BufferedReader(new FileReader(dataFilePath))) {
            String inputLine;

            // Read the default value
            int DEFAULT_VALUE = Integer.parseInt(myReader.readLine());
            int MIN_VALUE = DEFAULT_VALUE;
            int MAX_VALUE = DEFAULT_VALUE;

            // Read number of rows and columns
            inputLine = myReader.readLine();
            int[] rowCol = Arrays.stream(inputLine.trim().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            final int NUM_ROWS = rowCol[0];
            final int NUM_COLS = rowCol[1];

            // Resize array if needed
            if (arrayData == null || NUM_ROWS != arrayData.length || NUM_COLS != arrayData[0].length) {
                arrayData = new int[NUM_ROWS][NUM_COLS];
            }

            // Fill the entire array with the default value first
            for (int row = 0; row < NUM_ROWS; row++) {
                for (int col = 0; col < NUM_COLS; col++) {
                    arrayData[row][col] = DEFAULT_VALUE;
                }
            }

            // Read remaining rows of data
            while ((inputLine = myReader.readLine()) != null) {
                if (inputLine.trim().isEmpty()) continue; // skip blank lines

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

        } catch (IOException e) {
            e.printStackTrace();
            arrayData = null;
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in file: " + e.getMessage());
            arrayData = null;
        }
    }
}
