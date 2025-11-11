package pkgSlRenderEngine;

import pkgSlUtilities.*;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static pkgSlRenderEngine.SlSpot.SLEEP_INTERVAL;


 // ***************** EXTRA FEATURE: SPACE = pause/play,  R = restart the Cellular Automata *************************************

public class SlCARenderer extends SlRenderer {

    private SlPingPongArray gridArray;
    private String fileName;

    // cached quads (each quad is float[8] -> 4 (x,y) pairs)
    private final ArrayList<float[]> quadVertices = new ArrayList<>();
    private final ArrayList<float[]> quadColors   = new ArrayList<>();
    private volatile boolean updateVertexArray = true;

    private boolean paused = false;                 // Space bar pause / play
    private int[][] initialSnapshot;                // restart the CA

    private static final float[] BG_COLOR    = new float[]{0.0f, 0.1f, 0.5f, 1.0f};  // true blue
    private static final float[] COLOR_DEAD  = new float[]{1.0f, 0.5f, 0.0f};        // DOnt really need but just because i used this for testimg
    private static final float[] COLOR_ALIVE = new float[]{0.82f, 0.82f, 0.82f};     // silver

    public SlCARenderer(SlWindowManager win, SlCamera cam, String dataFile) {
        super(win);
        this.fileName = dataFile;
        System.out.println("SlCARenderer created with data file: " + dataFile);
    }

    @Override
    public void initRendering(int numRows, int numCols) {
        System.out.println("CARenderer initializing rendering for " + numRows + "x" + numCols);
        gridArray = new SlPingPongArray(numRows, numCols);

        // Load initial grid from file into live array
        loadGridFromFile(fileName, numRows, numCols);

        // Take a snapshot so we can restart whenever we want
        initialSnapshot = new int[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            System.arraycopy(gridArray.liveCellArray.arrayData[r], 0,
                    initialSnapshot[r], 0, numCols);
        }

        windowManager.setKeyActionHandler(this::onKey);

        super.initRendering(numRows, numCols);

        updateVertexArray = true;
    }

    // Check to catch pause and restart keys pressed
    private void onKey(Integer key, Integer action) {
        if (action != GLFW_RELEASE) return;   // act on release to avoid repeats

        if (key == GLFW_KEY_SPACE) {
            paused = !paused;
            System.out.println(paused ? "Paused" : "Playing");
        } else if (key == GLFW_KEY_R) {
            // restore the snapshot
            for (int r = 0; r < gridArray.NUM_ROWS; r++) {
                System.arraycopy(initialSnapshot[r], 0,
                        gridArray.liveCellArray.arrayData[r], 0,
                        gridArray.NUM_COLS);
            }
            updateVertexArray = true;  // force geometry rebuild
            System.out.println("Restarted");
        }
    }

    private void loadGridFromFile(String filename, int numRows, int numCols) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            int defaultVal = 0;
            line = br.readLine();
            if (line != null) { try { defaultVal = Integer.parseInt(line.trim()); } catch (Exception ignore) {} }

            int fileRows = numRows, fileCols = numCols;
            line = br.readLine();
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

                int r;
                try { r = Integer.parseInt(parts[0]); } catch (NumberFormatException nfe) { continue; }
                if (r < 0 || r >= fileRows || r >= numRows) continue;

                int remaining = parts.length - 1;

                if (!rowInit[r]) {
                    for (int c = 0; c < numCols; c++) gridArray.liveCellArray.arrayData[r][c] = 0;
                    rowInit[r] = true;
                }

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

                if (remaining == fileCols + 1) {
                    int offset = 0;
                    try { offset = Integer.parseInt(parts[1]); } catch (NumberFormatException ignore) {}
                    int start = Math.max(0, Math.min(offset, numCols - 1));
                    int maxVals = Math.min(fileCols, numCols - start);

                    for (int i = 0; i < maxVals; i++) {
                        int dstCol = start + i;
                        int srcIdx = 2 + i;
                        try {
                            gridArray.liveCellArray.arrayData[r][dstCol] =
                                    Integer.parseInt(parts[srcIdx]) != 0 ? 1 : 0;
                        } catch (NumberFormatException nfe) {
                            gridArray.liveCellArray.arrayData[r][dstCol] = 0;
                        }
                    }
                    continue;
                }

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

            // Print info to debug
            int live = 0;
            for (int rr = 0; rr < gridArray.NUM_ROWS; rr++)
                for (int cc = 0; cc < gridArray.NUM_COLS; cc++)
                    if (gridArray.liveCellArray.arrayData[rr][cc] == 1) live++;
            System.out.println("Live cells after load: " + live);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateLCVertexArray() {
        quadVertices.clear();
        quadColors.clear();

        final int rows = gridArray.NUM_ROWS;
        final int cols = gridArray.NUM_COLS;
        if (rows <= 0 || cols <= 0) return;

        final float aspect = (float) windowManager.getWidth() / windowManager.getHeight();

        final float TOP_MARGIN_FRAC  = 0.18f;
        final float LEFT_MARGIN_FRAC = 0.05f;
        final float PAD_FRAC         = 0.40f;

        final float totalW = 2.0f * aspect;
        final float totalH = 2.0f;
        final float usableW = totalW * (1.0f - LEFT_MARGIN_FRAC - LEFT_MARGIN_FRAC);
        final float usableH = totalH * (1.0f - TOP_MARGIN_FRAC);

        final float dx = usableW / cols;
        final float dy = usableH / rows;

        final float xLeft = -aspect + totalW * LEFT_MARGIN_FRAC;
        final float yTop  =  1.0f    - totalH * TOP_MARGIN_FRAC;

        final float px = dx * (PAD_FRAC * 0.5f);
        final float py = dy * (PAD_FRAC * 0.5f);

        for (int r = 0; r < rows; r++) {
            final float yBottom = yTop - (r + 1) * dy;
            final float yTopRow = yBottom + dy;

            for (int c = 0; c < cols; c++) {
                if (gridArray.liveCellArray.arrayData[r][c] == 0) continue;

                final float x0 = xLeft + c * dx;
                final float x1 = x0 + dx;

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

    private void tickUpdate() {
        final int rows = gridArray.NUM_ROWS;
        final int cols = gridArray.NUM_COLS;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                gridArray.nextCellArray.arrayData[r][c] = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = gridArray.getNNSum(r, c);
                int cur = gridArray.liveCellArray.arrayData[r][c];
                if (neighbors == 3) {
                    gridArray.nextCellArray.arrayData[r][c] = 1;
                } else if (neighbors == 2) {
                    gridArray.nextCellArray.arrayData[r][c] = cur;
                } else {
                    gridArray.nextCellArray.arrayData[r][c] = 0;
                }
            }
        }

        gridArray.swapLiveAndNext();
        updateVertexArray = true;
    }

    @Override
    public void renderScene() {
        final int numRows = gridArray.NUM_ROWS;
        final int numCols = gridArray.NUM_COLS;

        System.out.println("Rendering scene: rows=" + numRows + ", cols=" + numCols);

        windowManager.runRenderLoop(() -> {
            GL11.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3]);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            if (updateVertexArray) {
                generateLCVertexArray();
                updateVertexArray = false;
            }

            final int quadCount = quadVertices.size();
            for (int i = 0; i < quadCount; i++) {
                float[] verts = quadVertices.get(i);
                float[] col   = quadColors.get(i);
                try { windowManager.drawQuad(verts, col); }
                catch (IllegalArgumentException iae) {
                    System.err.println("drawQuad failed for tile " + i + ": " + iae.getMessage());
                }
            }

            if (SLEEP_INTERVAL > 0) {
                try { Thread.sleep(SLEEP_INTERVAL); } catch (InterruptedException ignored) { }
            }

            if (!paused) {
                tickUpdate();
            }
        });
    }
}
