package pkgSlUtilities;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
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

    // OpenGL / render resources
    private int shaderProgram;
    private int vpMatLocation;
    private int renderColorLocation;
    private int vbo;
    private int ibo;

    private static final int OGL_MATRIX_SIZE = 16;
    private final Matrix4f viewProjMatrix = new Matrix4f();
    private final FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);

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

    // Call from Driver after initGLFWindow()
    public void initOpenGL() {
        // Make sure context is current
        updateContextToThis();
        // create GL capabilities
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(0.043f, 0.380f, 0.588f, 1.0f);

        shaderProgram = glCreateProgram();

        // Vertex shader (simple position * matrix)
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "attribute vec2 position;" +          // using attribute rather than legacy gl_Vertex
                        "void main(void) {" +
                        "  gl_Position = viewProjMatrix * vec4(position, 0.0, 1.0);" +
                        "}");
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Vertex shader compile: " + glGetShaderInfoLog(vs));
        }
        glAttachShader(shaderProgram, vs);

        // Fragment shader
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        "  gl_FragColor = vec4(color, 1.0);" +
                        "}");
        glCompileShader(fs);
        if (glGetShaderi(fs, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Fragment shader compile: " + glGetShaderInfoLog(fs));
        }
        glAttachShader(shaderProgram, fs);

        glBindAttribLocation(shaderProgram, 0, "position");
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program link: " + glGetProgramInfoLog(shaderProgram));
        }
        glUseProgram(shaderProgram);

        vpMatLocation = glGetUniformLocation(shaderProgram, "viewProjMatrix");
        renderColorLocation = glGetUniformLocation(shaderProgram, "color");

        // Setup orthographic projection the same as your previous driver
        viewProjMatrix.setOrtho(-100f, 100f, -100f, 100f, 0f, 10f);
        glUniformMatrix4fv(vpMatLocation, false, viewProjMatrix.get(myFloatBuffer));
    }

    public void initBuffers() {
        vbo = org.lwjgl.opengl.GL15.glGenBuffers();
        ibo = org.lwjgl.opengl.GL15.glGenBuffers();

        final int[] indices = {0, 1, 2, 0, 2, 3};
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length).put(indices).flip();

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, ib, org.lwjgl.opengl.GL15.GL_STATIC_DRAW);

        // Reserve space for 4 verts * 2 components * 4 bytes (float)
        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vbo);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, 4 * 2 * Float.BYTES, org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW);

        // Enable attribute 0 (position) - compatible with glVertexAttribPointer later
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);

        // Ensure element array is bound while drawing (we rebind before draw)
    }

    // ===================== RENDER API =====================
    // The render loop is private. Call runRenderLoop and supply a Runnable that draws each frame.
    public void runRenderLoop(Runnable frameCallback) {
        try {
            while (!glfwWindowShouldClose(glfwWindow)) {
                glfwPollEvents();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                // call the user-supplied per-frame draw logic
                frameCallback.run();

                glfwSwapBuffers(glfwWindow);
            }
        } finally {
            // cleanup when loop exits
            cleanupGLResources();
        }
    }

    // Draw a single quad (4 verts), color vector length 3
    public void drawQuad(float[] verts, float[] color) {
        if (verts == null || verts.length != 8) throw new IllegalArgumentException("verts must be length 8 (4 x vec2)");
        if (color == null || color.length != 3) throw new IllegalArgumentException("color must be length 3");

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer fb = BufferUtils.createFloatBuffer(verts.length).put(verts).flip();
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, fb, org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW);

        // Element buffer
        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);

        // Use shader program and set color
        glUseProgram(shaderProgram);
        glUniform3f(renderColorLocation, color[0], color[1], color[2]);

        // Ensure vertex attribute pointer is enabled and points to buffer
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
    }

    // ===================== CONTEXT helpers =====================
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
    private void cleanupGLResources() {
        // delete GL buffers and program
        try {
            if (vbo != 0) org.lwjgl.opengl.GL15.glDeleteBuffers(vbo);
            if (ibo != 0) org.lwjgl.opengl.GL15.glDeleteBuffers(ibo);
            if (shaderProgram != 0) glDeleteProgram(shaderProgram);
        } catch (Exception ignored) {}

        // destroy window and callbacks
        if (keyCallback != null) keyCallback.free();
        if (fbCallback != null) fbCallback.free();
        if (glfwWindow != 0) glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        if (errorCallback != null) glfwSetErrorCallback(null).free();
    }

    // ===================== GETTERS =====================
    public long getWindow() { return glfwWindow; }
    public int getWidth() { return WIN_WIDTH; }
    public int getHeight() { return WIN_HEIGHT; }
}
