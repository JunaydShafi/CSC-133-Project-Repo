package pkgSlUtilities;

import java.io.*;
import java.util.Arrays;

public class SlIntArray {
    public int[][] arrayData;
    public int NUM_ROWS;
    public int NUM_COLS;

    public SlIntArray(int rows, int cols) {
        NUM_ROWS = rows;
        NUM_COLS = cols;
        arrayData = new int[rows][cols];
    }

    public int[][] loadFile(String dataFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            int DEFAULT_VALUE = Integer.parseInt(reader.readLine());

            String[] dims = reader.readLine().trim().split("\\s+");
            NUM_ROWS = Integer.parseInt(dims[0]);
            NUM_COLS = Integer.parseInt(dims[1]);

            arrayData = new int[NUM_ROWS][NUM_COLS];
            for (int r = 0; r < NUM_ROWS; r++) {
                Arrays.fill(arrayData[r], DEFAULT_VALUE);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                int[] rowData = Arrays.stream(line.trim().split("\\s+"))
                        .mapToInt(Integer::parseInt)
                        .toArray();

                int row = rowData[0];
                int col = rowData[1];
                for (int i = 2; i < rowData.length && col < NUM_COLS; i++) {
                    arrayData[row][col++] = rowData[i];
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
        for (int r = 0; r < NUM_ROWS; r++)
            System.arraycopy(arrayData[r], 0, clone[r], 0, NUM_COLS);
        return clone;
    }
}
