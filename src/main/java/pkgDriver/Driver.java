package pkgDriver;

import pkgSlUtilities.SlWindowManager;

public class Driver {
    private static final float SQUARE_SIDE = 14;
    private static final float PADDING = 3f;
    private static final float OFFSET = 5f;
    private static final float[] SQUARE_COLOR = {0.75f, 0.75f, 0.75f};

    SlWindowManager windowManager = new SlWindowManager();

    public static void main(String[] args) {
        new Driver().start();
    }

    void start() {
        // initialize window + GL resources
        windowManager.initGLFWindow();
        windowManager.initOpenGL();
        windowManager.initBuffers();

        // Generate world-space 4x4 grid (top-left corner)
        float[][] gridSquares = generateWorldGrid(11, 7, SQUARE_SIDE, PADDING, OFFSET);

        // run the render loop
        windowManager.runRenderLoop(() -> {
            for (float[] square : gridSquares) {
                windowManager.drawQuad(square, SQUARE_COLOR);
            }
        });
    }

    private float[][] generateWorldGrid(int rows, int cols, float squareLength, float padding, float offset) {
        float aspect = (float) windowManager.getWidth() / windowManager.getHeight();
        float[][] squares = new float[rows * cols][8];
        int index = 0;

        // top-left corner using aspect-correct startX
        float startX = -100f * aspect + offset; // left edge
        float startY = 100f - offset;           // top edge

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float cx = startX + col * (squareLength + padding) + squareLength / 2f;
                float cy = startY - row * (squareLength + padding) - squareLength / 2f;
                squares[index++] = createOffsetSquare(squareLength, cx, cy);
            }
        }

        return squares;
    }

    private float[] createOffsetSquare(float side, float cx, float cy) {
        float half = side / 2f;
        return new float[]{
                cx - half, cy - half,
                cx + half, cy - half,
                cx + half, cy + half,
                cx - half, cy + half
        };
    }
}
