// pkgSlUtilities/SlGoLArray.java
package pkgSlUtilities;

public class SlGoLArray {
    public final int NUM_ROWS, NUM_COLS;
    private final SlPingPongArray ppa;

    public SlGoLArray(int rows, int cols) {
        this.NUM_ROWS = rows;
        this.NUM_COLS = cols;
        this.ppa = new SlPingPongArray(rows, cols);
    }

    // keep prof’s expected name
    public void onTickUpdate() {
        // standard GoL rule using the ping-pong arrays inside ppa
        for (int r = 0; r < NUM_ROWS; r++)
            for (int c = 0; c < NUM_COLS; c++)
                ppa.nextCellArray.arrayData[r][c] = 0;

        for (int r = 0; r < NUM_ROWS; r++) {
            for (int c = 0; c < NUM_COLS; c++) {
                int cur = ppa.liveCellArray.arrayData[r][c];
                int nn  = ppa.getNNSum(r, c); // wraps toroidally
                ppa.nextCellArray.arrayData[r][c] = (nn == 3 || (nn == 2 && cur == 1)) ? 1 : 0;
            }
        }
        ppa.swapLiveAndNext();
    }

    // expected by the diagram
    public int[] getNumRowsCols() { return new int[]{NUM_ROWS, NUM_COLS}; }

    // helpers so your renderer code doesn’t change much
    public SlIntArray live() { return ppa.liveCellArray; }
    public SlIntArray next() { return ppa.nextCellArray; }
    public int getNNSum(int r, int c) { return ppa.getNNSum(r, c); }
}
