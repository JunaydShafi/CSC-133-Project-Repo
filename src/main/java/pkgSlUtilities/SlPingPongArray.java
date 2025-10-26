package pkgSlUtilities;

import java.util.Random;

public class SlPingPongArray extends SlIntArray {
    public SlIntArray liveCellArray;
    public SlIntArray nextCellArray;
    private Random rand;

    public SlPingPongArray(int rows, int cols) {
        this(rows, cols, 0, 0, (int) System.currentTimeMillis());
    }

    public SlPingPongArray(int rows, int cols, int x, int y, int seed) {
        super(rows, cols);
        rand = new Random(seed);
        liveCellArray = new SlIntArray(rows, cols);
        nextCellArray = new SlIntArray(rows, cols);
    }

    public void swapLiveAndNext() {
        SlIntArray temp = liveCellArray;
        liveCellArray = nextCellArray;
        nextCellArray = temp;
    }

    @Override
    public int[][] loadFile(String dataFile) {
        // Load data into nextCellArray (write array)
        int[][] loaded = nextCellArray.loadFile(dataFile);

        // Keep dimensions consistent
        NUM_ROWS = nextCellArray.NUM_ROWS;
        NUM_COLS = nextCellArray.NUM_COLS;

        // Return a clone for test validation
        return nextCellArray.getClone();
    }




    public void printArray() {
        for (int r = 0; r < NUM_ROWS; r++) {
            System.out.printf("%4d|--> ", r);
            for (int c = 0; c < NUM_COLS; c++) {
                System.out.printf("%4d ", liveCellArray.arrayData[r][c]);
            }
            System.out.println();
        }
    }

    public void randomizeViaFisherYatesKnuth() {
        int total = NUM_ROWS * NUM_COLS;

        for (int r = 0; r < NUM_ROWS; r++) {
            for (int c = 0; c < NUM_COLS; c++) {
                nextCellArray.arrayData[r][c] = liveCellArray.arrayData[r][c];
            }
        }

        for (int i = total - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int r1 = i / NUM_COLS, c1 = i % NUM_COLS;
            int r2 = j / NUM_COLS, c2 = j % NUM_COLS;

            int temp = nextCellArray.arrayData[r1][c1];
            nextCellArray.arrayData[r1][c1] = nextCellArray.arrayData[r2][c2];
            nextCellArray.arrayData[r2][c2] = temp;
        }

        swapLiveAndNext();
    }

    public void setCell(int row, int col, int val) {
        nextCellArray.arrayData[row][col] = val;
    }

    public int getNNSum(int row, int col) {
        int sum = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = (row + dr + NUM_ROWS) % NUM_ROWS;
                int c = (col + dc + NUM_COLS) % NUM_COLS;
                sum += liveCellArray.arrayData[r][c];
            }
        }
        return sum;
    }
}
