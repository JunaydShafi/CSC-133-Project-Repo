package pkgDriver;

import pkgSlUtilities.SlWindowManager;
import org.lwjgl.BufferUtils;

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

        // build two squares (same positions as before)
        float half = SQUARE_SIDE / 2f;
        float[] square1 = new float[]{
                -half - half,  half + half,   // top-left (cx - half, cy + half) this was your original offsets
                -half + half,  half + half,   // top-right
                -half + half,  half - half,   // bottom-right
                -half - half,  half - half    // bottom-left
        };
        // easier: reuse createOffsetSquare function from earlier - but here build manually:
        float[] squareA = createOffsetSquare(SQUARE_SIDE, -SQUARE_SIDE / 2f, SQUARE_SIDE / 2f);
        float[] squareB = createOffsetSquare(SQUARE_SIDE, SQUARE_SIDE / 2f, -SQUARE_SIDE / 2f);

        // run the render loop: pass a lambda that draws each frame
        windowManager.runRenderLoop(() -> {
            // any per-frame updates would go here (animation, pattern movement, etc.)
            windowManager.drawQuad(squareA, SQUARE_COLOR);
            windowManager.drawQuad(squareB, SQUARE_COLOR);
        });

        // After loop exits, resources already cleaned by windowManager
    }

    private float[][] generateWorldGrid(int rows, int cols, float squareLength, float padding, float offset)
    {

    }

    // helper same as your previous createOffsetSquare (keeps Driver simple)
    // helper same as your previous createOffsetSquare (keeps Driver simple)
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
