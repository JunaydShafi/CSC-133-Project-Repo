package pkgSlRenderEngine;

import pkgSlUtilities.SlWindowManager;
import org.lwjgl.opengl.GL11;

/**
 * Base renderer class for all rendering types (e.g., CA Renderer).
 * Provides OpenGL setup, window management, and a render loop hook.
 */
public class SlRenderer {

    protected SlWindowManager windowManager;
    protected boolean running = true;

    public SlRenderer(SlWindowManager win) {
        this.windowManager = win;
    }

    /**
     * Initializes the OpenGL context and prepares the rendering window.
     */
    public void initOpenGL() {
        windowManager.initWindow();
        windowManager.initOpenGL();
        windowManager.initBuffers();
        System.out.println("OpenGL initialized in SlRenderer.");
    }

    /**
     * Called once before rendering starts to set up scene parameters.
     */
    public void initRendering(int numRows, int numCols) {
        System.out.println("Initializing rendering base: rows=" + numRows + ", cols=" + numCols);
    }

    /**
     * Main rendering loop. Subclasses should override this to draw their scene.
     */
    public void renderScene() {
        System.out.println("Base renderScene() called (no scene to draw).");

        windowManager.runRenderLoop(() -> {
            // Default clear background if subclass doesn't override
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        });
    }

    /**
     * Gracefully stops the render loop (if supported by windowManager).
     */
    public void stopRendering() {
        running = false;
        System.out.println("Rendering stopped.");
    }
}
