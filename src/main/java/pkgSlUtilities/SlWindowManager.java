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

    private int width = 1000;
    private int height = 800;
    private static final int POS_X = 30;
    private static final int POS_Y = 90;
    private static final int MSAA_SAMPLES = 8;
    private static final int VSYNC_INTERVAL = 1;
    private static final String TITLE = "CSC 133";

    private long glfwWindow;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWFramebufferSizeCallback fbCallback;

    private int shaderProgram;
    private int vpMatrixLocation;
    private int colorLocation;
    private int vbo;
    private int ibo;

    private static final int MATRIX_SIZE = 16;
    private final Matrix4f viewProjMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(MATRIX_SIZE);

    // ========================= Singleton =========================
    private static SlWindowManager instance;

    private SlWindowManager() {}

    public static SlWindowManager getInstance() {
        if (instance == null) {
            instance = new SlWindowManager();
        }
        return instance;
    }

    public static SlWindowManager get(int width, int height) {
        if (instance == null) {
            instance = new SlWindowManager();
        }
        instance.width = width;
        instance.height = height;
        return instance;
    }

    public void initWindow() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, MSAA_SAMPLES);

        glfwWindow = glfwCreateWindow(width, height, TITLE, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create GLFW window");

        setupCallbacks();
        glfwSetWindowPos(glfwWindow, POS_X, POS_Y);
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(VSYNC_INTERVAL);
        glfwShowWindow(glfwWindow);
    }
    private void setupCallbacks() {
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
                    width = w;
                    height = h;
                    glViewport(0, 0, width, height);
                    updateProjectionMatrix();
                }
            }
        });
    }


    public void initOpenGL() {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, width, height);
        glClearColor(0.043f, 0.380f, 0.588f, 1.0f);

        compileShaders();
        updateProjectionMatrix();
    }

    private void compileShaders() {
        shaderProgram = glCreateProgram();

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader,
                "uniform mat4 viewProjMatrix;" +
                        "attribute vec2 position;" +
                        "void main(void) {" +
                        "  gl_Position = viewProjMatrix * vec4(position, 0.0, 1.0);" +
                        "}");
        glCompileShader(vertexShader);
        checkShaderCompile(vertexShader, "Vertex");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        "  gl_FragColor = vec4(color, 1.0);" +
                        "}");
        glCompileShader(fragmentShader);
        checkShaderCompile(fragmentShader, "Fragment");

        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindAttribLocation(shaderProgram, 0, "position");
        glLinkProgram(shaderProgram);

        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader program link error: " + glGetProgramInfoLog(shaderProgram));

        glUseProgram(shaderProgram);

        vpMatrixLocation = glGetUniformLocation(shaderProgram, "viewProjMatrix");
        colorLocation = glGetUniformLocation(shaderProgram, "color");
    }

    private void checkShaderCompile(int shader, String type) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException(type + " shader compile error: " + glGetShaderInfoLog(shader));
    }

    private void updateProjectionMatrix() {
        float aspect = (float) width / height;

// Use -1..+1 NDC-like projection scaled by aspect ratio so quads built in SlCARenderer
// (which compute x in [-1,1] and y in [-1,1]) cover the viewport properly.
        viewProjMatrix.setOrtho(-1.0f * aspect, 1.0f * aspect, -1.0f, 1.0f, -1.0f, 1.0f);

        glUseProgram(shaderProgram);
        glUniformMatrix4fv(vpMatrixLocation, false, viewProjMatrix.get(matrixBuffer));

    }

    public void initBuffers() {
        int vao = org.lwjgl.opengl.GL30.glGenVertexArrays();
        org.lwjgl.opengl.GL30.glBindVertexArray(vao);

        vbo = org.lwjgl.opengl.GL15.glGenBuffers();
        ibo = org.lwjgl.opengl.GL15.glGenBuffers();

        IntBuffer indices = BufferUtils.createIntBuffer(6);
        indices.put(new int[]{0, 1, 2, 0, 2, 3});
        indices.flip();
        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, indices, org.lwjgl.opengl.GL15.GL_STATIC_DRAW);

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vbo);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, 4 * 2 * Float.BYTES, org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);
    }

    public void runRenderLoop(Runnable frameCallback) {
        try {
            while (!glfwWindowShouldClose(glfwWindow)) {
                glfwPollEvents();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                frameCallback.run();
                glfwSwapBuffers(glfwWindow);
            }
        } finally {
            cleanup();
        }
    }

    public void drawQuad(float[] vertices, float[] color) {
        if (vertices == null || vertices.length != 8) throw new IllegalArgumentException("Vertices must be length 8");
        if (color == null || color.length != 3) throw new IllegalArgumentException("Color must be length 3");

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip();
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, buffer, org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW);

        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);

        glUseProgram(shaderProgram);
        glUniform3f(colorLocation, color[0], color[1], color[2]);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
    }

    private void cleanup() {
        try {
            if (vbo != 0) org.lwjgl.opengl.GL15.glDeleteBuffers(vbo);
            if (ibo != 0) org.lwjgl.opengl.GL15.glDeleteBuffers(ibo);
            if (shaderProgram != 0) glDeleteProgram(shaderProgram);
        } catch (Exception ignored) {}
        if (keyCallback != null) keyCallback.free();
        if (fbCallback != null) fbCallback.free();
        if (glfwWindow != 0) glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        if (errorCallback != null) glfwSetErrorCallback(null).free();
    }

    public void destroyGlfwWindow() {
        if (glfwWindow != 0) {
            glfwDestroyWindow(glfwWindow);
            glfwWindow = 0;
        }
        glfwTerminate();
    }

    public void initRendering(int rows, int cols) {
        System.out.println("Initializing rendering with rows=" + rows + " cols=" + cols);
    }

    public void renderScene() {
        System.out.println("Rendering scene...");
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
