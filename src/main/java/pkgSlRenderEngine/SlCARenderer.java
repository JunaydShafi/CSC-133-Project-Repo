package pkgSlRenderEngine;

import pkgSlUtilities.*;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static pkgSlRenderEngine.SlSpot.SLEEP_INTERVAL;

/**
 * SlCARenderer - Cellular Automata renderer (Game of Life)
 * - Builds a sparse vertex list (one quad per alive cell)
 * - Draws quads using SlWindowManager.drawQuad(...)
 * - Advances CA rules each frame and rebuilds vertex list only when needed
 */
public class SlCARenderer extends SlRenderer {

    // board
    private SlPingPongArray gridArray;
    private String fileName;

    // cached quads (each quad is float[8] -> 4 (x,y) pairs)
    private ArrayList<float[]> quadVertices = new ArrayList<>();
    private ArrayList<float[]> quadColors = new ArrayList<>();
    private volatile boolean updateVertexArray = true;

    // background + colors (tweak as desired)
    private static final float[] BG_COLOR = new float[]{0.9f, 0.4f, 0.8f, 1.0f};
    private static final float[] COLOR_DEAD = new float[]{1.0f, 0.5f, 0.0f}; // orange
    private static final float[] COLOR_ALIVE = new float[]{0.8f, 0.8f, 0.8f}; // silver

    public SlCARenderer(SlWindowManager win, SlCamera cam, String dataFile) {
        super(win);
        this.fileName = dataFile;
        System.out.println("SlCARenderer created with data file: " + dataFile);
    }

    @Override
    public void initRendering(int numRows, int numCols) {
        System.out.println("CARenderer initializing rendering for " + numRows + "x" + numCols);
        gridArray = new SlPingPongArray(numRows, numCols);

        // Load initial grid from file (populates nextCellArray in SlPingPongArray.loadFile)
        // We'll copy values into liveCellArray so rendering starts with the loaded grid.
        loadGridFromFile(fileName, numRows, numCols);

        // Ensure renderer base init happens
        super.initRendering(numRows, numCols);

        // Mark vertex array initial build required
        updateVertexArray = true;
    }

    /**
     * Load the input file into the gridArray.liveCellArray. This keeps your earlier loading style.
     */
    private void loadGridFromFile(String filename, int numRows, int numCols) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int rowIndex = 0;

            // Skip possible header lines the input format may contain.
            // If your file format differs, adjust these skips accordingly.
            br.readLine(); // skip line 0 (maybe default)
            br.readLine(); // skip line 1 (maybe dims/header)

            while ((line = br.readLine()) != null && rowIndex < numRows) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                // parts[0] may be row index in file; parts[1..] are cell values
                for (int c = 1; c <= numCols && c < parts.length; c++) {
                    try {
                        gridArray.liveCellArray.arrayData[rowIndex][c - 1] = Integer.parseInt(parts[c]);
                    } catch (NumberFormatException nfe) {
                        gridArray.liveCellArray.arrayData[rowIndex][c - 1] = 0;
                    }
                }
                rowIndex++;
            }

            System.out.println("Grid loaded from file: " + rowIndex + " rows");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the sparse vertex list (quadVertices and quadColors) for every alive cell.
     * Coordinates use normalized -1..1 like your prior version, which worked with your test harness.
     * We compute cell sizes based on numRows/numCols so the grid fills the view.
     */
    private void generateLCVertexArray() {
        // Clear previous lists
        quadVertices.clear();
        quadColors.clear();

        final int numRows = gridArray.NUM_ROWS;
        final int numCols = gridArray.NUM_COLS;

        if (numRows <= 0 || numCols <= 0) return;

        // compute normalized cell size assuming NDC -1..1 in both axes (prior code used this)
        float cellWidth = 2.0f / numCols;   // x spans -1..+1
        float cellHeight = 2.0f / numRows;  // y spans -1..+1

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int val = gridArray.liveCellArray.arrayData[r][c];
                if (val == 0) {
                    // If you want to *not* draw dead cells, skip them:
                    continue;
                }
                // Flip Y so row 0 is top of the window like your previous code
                float x = -1.0f + c * cellWidth;
                float y = 1.0f - (r + 1) * cellHeight;

                // Quad vertices in (x,y) pairs (4 verts) - must be length 8
                float[] verts = new float[8];
                verts[0] = x;                 verts[1] = y;                  // v0: top-left
                verts[2] = x + cellWidth;     verts[3] = y;                  // v1: top-right
                verts[4] = x + cellWidth;     verts[5] = y + cellHeight;     // v2: bottom-right
                verts[6] = x;                 verts[7] = y + cellHeight;     // v3: bottom-left

                quadVertices.add(verts);

                // Color for this quad
                if (val == 1) quadColors.add(COLOR_ALIVE);
                else quadColors.add(COLOR_DEAD);
            }
        }
    }

    /**
     * Advance one tick of the Game of Life rules into nextCellArray and swap.
     * Uses toroidal wrapping as implemented in getNNSum().
     */
    private void tickUpdate() {
        final int rows = gridArray.NUM_ROWS;
        final int cols = gridArray.NUM_COLS;

        // initialize next array to zeros
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                gridArray.nextCellArray.arrayData[r][c] = 0;
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = gridArray.getNNSum(r, c);
                int cur = gridArray.liveCellArray.arrayData[r][c];
                if (neighbors == 3) {
                    gridArray.nextCellArray.arrayData[r][c] = 1;
                } else if (neighbors == 2) {
                    gridArray.nextCellArray.arrayData[r][c] = cur; // keep current state
                } else {
                    gridArray.nextCellArray.arrayData[r][c] = 0;
                }
            }
        }

        // swap live / next
        gridArray.swapLiveAndNext();

        // mark vertex array for rebuild
        updateVertexArray = true;
    }

    /**
     * Main render loop. Uses windowManager.runRenderLoop(Runnable) to handle event poll & swap,
     * but we follow the intended flow: clear, generate vertex array if needed, draw, optionally sleep,
     * then advance the CA tick.
     */
    @Override
    public void renderScene() {
        final int numRows = gridArray.NUM_ROWS;
        final int numCols = gridArray.NUM_COLS;

        System.out.println("Rendering scene: rows=" + numRows + ", cols=" + numCols);

        // The frame callback passed to windowManager.runRenderLoop will be executed every frame.
        windowManager.runRenderLoop(() -> {
            // Clear background
            GL11.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3]);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Rebuild the vertex list only if flagged
            if (updateVertexArray) {
                generateLCVertexArray();
                updateVertexArray = false;
            }

            // Draw all quads via windowManager helper
            final int quadCount = quadVertices.size();
            for (int i = 0; i < quadCount; i++) {
                float[] verts = quadVertices.get(i);
                float[] col = quadColors.get(i);
                // windowManager.drawQuad expects float[8] and float[3]
                try {
                    windowManager.drawQuad(verts, col);
                } catch (IllegalArgumentException iae) {
                    // If something unexpected, print and continue
                    System.err.println("drawQuad failed for tile " + i + ": " + iae.getMessage());
                }
            }

            // optional sleep to control simulation speed (0 means no sleep)
            if (SLEEP_INTERVAL > 0) {
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException ignored) { }
            }

            // Advance the Game of Life by one tick
            tickUpdate();
        });
    }
}
