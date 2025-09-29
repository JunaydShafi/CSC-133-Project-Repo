package pkgSlUtilities;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SlWindowManager {
    private static int WIN_WIDTH = 1000;
    private static int WIN_HEIGHT = 800;
    private static final int WIN_POS_X = 30;
    private static final int WIN_POS_Y = 90;
    private static final int MSAA_SAMPLES = 8;
    private static final int VSYNC_INTERVAL = 1;
    private static final String WINDOW_TITLE = "CSC 133";

    private long glfwWindow;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWFramebufferSizeCallback fbCallback;

    // ===================== INIT =====================
    public void initGLFWindow() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, MSAA_SAMPLES);

        glfwWindow = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(glfwWindow, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        });

        glfwSetFramebufferSizeCallback(glfwWindow, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    WIN_WIDTH = w;
                    WIN_HEIGHT = h;
                    glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
                }
            }
        });

        glfwSetWindowPos(glfwWindow, WIN_POS_X, WIN_POS_Y);
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(VSYNC_INTERVAL);
        glfwShowWindow(glfwWindow);
    }

    // ===================== CONTEXT =====================
    public void updateContextToThis() {
        glfwMakeContextCurrent(glfwWindow);
    }

    public void swapBuffers() {
        glfwSwapBuffers(glfwWindow);
    }

    public boolean isGlfwWindowClosed() {
        return glfwWindowShouldClose(glfwWindow);
    }

    // ===================== CLEANUP =====================
    public void destroyGlfwWindow() {
        glfwDestroyWindow(glfwWindow);
        if (keyCallback != null) keyCallback.free();
        if (fbCallback != null) fbCallback.free();
        glfwTerminate();
        if (errorCallback != null) glfwSetErrorCallback(null).free();
    }

    // ===================== GETTERS =====================
    public long getWindow() {
        return glfwWindow;
    }

    public int getWidth() {
        return WIN_WIDTH;
    }

    public int getHeight() {
        return WIN_HEIGHT;
    }
}
