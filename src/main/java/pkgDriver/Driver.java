package pkgDriver;

import pkgSlUtilities.SlWindowManager;

public class Driver {
    private static final float SQUARE_SIZE = 14f;
    private static final float PADDING = 3f;
    private static final float OFFSET = 5f;
    private static final int ROWS = 11;
    private static final int COLUMNS = 7;
    private static final float[] SQUARE_COLOR = {0.75f, 0.75f, 0.75f};

    private final SlWindowManager windowManager = SlWindowManager.getInstance();

    public static void main(String[] args) {
        new Driver().start();
    }

    private void start() {
        windowManager.initWindow();
        windowManager.initOpenGL();
        windowManager.initBuffers();

        float[][] grid = generateGrid(ROWS, COLUMNS, SQUARE_SIZE, PADDING, OFFSET);

        windowManager.runRenderLoop(() -> {
            for (float[] square : grid) {
                windowManager.drawQuad(square, SQUARE_COLOR);
            }
        });
    }

    private float[][] generateGrid(int rows, int cols, float size, float padding, float offset) {
        float aspect = (float) windowManager.getWidth() / windowManager.getHeight();
        float[][] squares = new float[rows * cols][8];
        float startX = -100f * aspect + offset;
        float startY = 100f - offset;
        int index = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float cx = startX + col * (size + padding) + size / 2f;
                float cy = startY - row * (size + padding) - size / 2f;
                squares[index++] = createSquare(cx, cy, size);
            }
        }
        return squares;
    }

    private float[] createSquare(float cx, float cy, float size) {
        float half = size / 2f;
        return new float[]{
                cx - half, cy - half,
                cx + half, cy - half,
                cx + half, cy + half,
                cx - half, cy + half
        };
    }
}
