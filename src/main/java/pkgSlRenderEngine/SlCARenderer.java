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
    private static final float[] BG_COLOR = new float[]{0.0f, 0.1f, 0.5f, 1.0f};
    private static final float[] COLOR_DEAD = new float[]{1.0f, 0.5f, 0.0f}; // orange
    private static final float[] COLOR_ALIVE = new float[]{0.82f, 0.82f, 0.82f}; // silver

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

            // --- headers ---
            int defaultVal = 0;
            line = br.readLine();                       // header #1: default
            if (line != null) {
                try { defaultVal = Integer.parseInt(line.trim()); } catch (Exception ignore) {}
            }

            int fileRows = numRows, fileCols = numCols;
            line = br.readLine();                       // header #2: "rows cols"
            if (line != null) {
                String[] rc = line.trim().split("\\s+");
                if (rc.length >= 2) {
                    try {
                        fileRows = Integer.parseInt(rc[0]);
                        fileCols = Integer.parseInt(rc[1]);
                    } catch (NumberFormatException ignore) {}
                }
            }

            boolean[] rowInit = new boolean[numRows];

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                // first token = row index
                int r;
                try { r = Integer.parseInt(parts[0]); } catch (NumberFormatException nfe) { continue; }
                if (r < 0 || r >= fileRows || r >= numRows) continue;

                int remaining = parts.length - 1;

                // initialize row once
                if (!rowInit[r]) {
                    for (int c = 0; c < numCols; c++) gridArray.liveCellArray.arrayData[r][c] = 0;
                    rowInit[r] = true;
                }

                // --- DENSE without offset: <row> v1..vN (N == fileCols) ---
                if (remaining == fileCols) {
                    int limit = Math.min(numCols, fileCols);
                    for (int c = 0; c < limit; c++) {
                        try {
                            gridArray.liveCellArray.arrayData[r][c] =
                                    Integer.parseInt(parts[c + 1]) != 0 ? 1 : 0;
                        } catch (NumberFormatException nfe) {
                            gridArray.liveCellArray.arrayData[r][c] = 0;
                        }
                    }
                    continue;
                }

                // --- DENSE with offset: <row> offset v1..vN (N == fileCols) ---
                if (remaining == fileCols + 1) {
                    int offset = 0;
                    try { offset = Integer.parseInt(parts[1]); } catch (NumberFormatException ignore) {}
                    int start = Math.max(0, Math.min(offset, numCols - 1));
                    int maxVals = Math.min(fileCols, numCols - start);

                    for (int i = 0; i < maxVals; i++) {
                        int dstCol = start + i;
                        int srcIdx = 2 + i; // values start after offset
                        try {
                            gridArray.liveCellArray.arrayData[r][dstCol] =
                                    Integer.parseInt(parts[srcIdx]) != 0 ? 1 : 0;
                        } catch (NumberFormatException nfe) {
                            gridArray.liveCellArray.arrayData[r][dstCol] = 0;
                        }
                    }
                    continue;
                }

                // --- SPARSE PAIRS: <row> c0 v0 c1 v1 ... (remaining even) ---
                if (remaining >= 2 && (remaining % 2 == 0)) {
                    for (int i = 1; i + 1 < parts.length; i += 2) {
                        try {
                            int c = Integer.parseInt(parts[i]);
                            int v = Integer.parseInt(parts[i + 1]);
                            if (c >= 0 && c < numCols && c < fileCols) {
                                gridArray.liveCellArray.arrayData[r][c] = (v != 0) ? 1 : 0;
                            }
                        } catch (NumberFormatException ignore) {}
                    }
                    continue;
                }

                // --- RLE: <row> len0 len1 len2 ... (toggle from defaultVal) ---
                int curVal = defaultVal;
                int col = 0;
                for (int i = 1; i < parts.length && col < Math.min(numCols, fileCols); i++) {
                    int len;
                    try { len = Integer.parseInt(parts[i]); } catch (NumberFormatException nfe) { continue; }
                    int end = Math.min(Math.min(numCols, fileCols), col + len);
                    for (int c = col; c < end; c++) gridArray.liveCellArray.arrayData[r][c] = curVal;
                    col = end;
                    curVal = (curVal == 0) ? 1 : 0;
                }
            }

            // quick debug: how many live cells did we actually load?
            int live = 0;
            for (int rr = 0; rr < gridArray.NUM_ROWS; rr++)
                for (int cc = 0; cc < gridArray.NUM_COLS; cc++)
                    if (gridArray.liveCellArray.arrayData[rr][cc] == 1) live++;
            System.out.println("Live cells after load: " + live);

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
        quadVertices.clear();
        quadColors.clear();

        final int rows = gridArray.NUM_ROWS;
        final int cols = gridArray.NUM_COLS;
        if (rows <= 0 || cols <= 0) return;

        // Projection: [-aspect..+aspect] x [-1..+1]
        final float aspect = (float) windowManager.getWidth() / windowManager.getHeight();

        // ---- layout controls (tweak these 2 numbers to match the prof) ----
        final float TOP_MARGIN_FRAC  = 0.18f;   // % of total vertical span kept as top margin
        final float LEFT_MARGIN_FRAC = 0.05f;   // % of total horizontal span kept as left margin
        final float PAD_FRAC         = 0.40f;   // % of each cell kept as gap (both axes)

        // Usable world-space extents after margins
        final float totalW = 2.0f * aspect;
        final float totalH = 2.0f;
        final float usableW = totalW * (1.0f - LEFT_MARGIN_FRAC - LEFT_MARGIN_FRAC); // symmetric L/R
        final float usableH = totalH * (1.0f - TOP_MARGIN_FRAC - 0.00f);             // top margin only

        // Cell size in world space
        final float dx = usableW / cols;
        final float dy = usableH / rows;

        // Grid origin (bottom-left) after margins; Y is anchored from top
        final float xLeft = -aspect + totalW * LEFT_MARGIN_FRAC;
        final float yTop  =  1.0f    - totalH * TOP_MARGIN_FRAC;

        // Inner inset for visible gaps
        final float px = dx * (PAD_FRAC * 0.5f);
        final float py = dy * (PAD_FRAC * 0.5f);

        for (int r = 0; r < rows; r++) {
            // top-anchored row: compute this row’s bottom and top
            final float yBottom = yTop - (r + 1) * dy;
            final float yTopRow = yBottom + dy;

            for (int c = 0; c < cols; c++) {
                if (gridArray.liveCellArray.arrayData[r][c] == 0) continue;

                final float x0 = xLeft + c * dx;
                final float x1 = x0 + dx;

                // apply padding inset so tiles don’t touch
                final float xmin = x0 + px;
                final float xmax = x1 - px;
                final float ymin = yBottom + py;
                final float ymax = yTopRow - py;

                float[] verts = new float[8];
                verts[0] = xmin; verts[1] = ymin;
                verts[2] = xmax; verts[3] = ymin;
                verts[4] = xmax; verts[5] = ymax;
                verts[6] = xmin; verts[7] = ymax;

                quadVertices.add(verts);
                quadColors.add(COLOR_ALIVE);
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
