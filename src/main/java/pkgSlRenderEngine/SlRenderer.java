package pkgSlRenderEngine;

import pkgSlUtilities.SlWindowManager;
import org.lwjgl.opengl.GL11;

public class SlRenderer {

    protected SlWindowManager windowManager;
    protected boolean running = true;

    public SlRenderer(SlWindowManager win) {
        this.windowManager = win;
    }


    public void initOpenGL() {
        windowManager.initWindow();
        windowManager.initOpenGL();
        windowManager.initBuffers();
        System.out.println("OpenGL initialized in SlRenderer.");
    }


    public void initRendering(int numRows, int numCols) {
        System.out.println("Initializing rendering base: rows=" + numRows + ", cols=" + numCols);
    }


    public void renderScene() {
        System.out.println("Base renderScene() called (no scene to draw).");

        windowManager.runRenderLoop(() -> {
            // Default clear background if subclass doesn't override
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        });
    }


    public void stopRendering() {
        running = false;
        System.out.println("Rendering stopped.");
    }
}
