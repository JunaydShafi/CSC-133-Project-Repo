package pkgSlRenderEngine;

import pkgSlUtilities.SlWindowManager;

public class SlRenderer {

    protected SlWindowManager windowManager;

    public SlRenderer(SlWindowManager win) {
        this.windowManager = win;
    }

    public void initOpenGL() {
        // delegate to the window manager for now
        windowManager.initWindow();
        windowManager.initOpenGL();
        windowManager.initBuffers();
        System.out.println("OpenGL initialized in SlRenderer.");
    }

    public void initRendering(int numRows, int numCols) {
        System.out.println("Initializing rendering base: rows=" + numRows + ", cols=" + numCols);
    }

    public void renderScene() {
        System.out.println("Base renderScene() called.");
        windowManager.runRenderLoop(() -> {
            // draw frame (later overridden in subclass)
        });
    }
}
