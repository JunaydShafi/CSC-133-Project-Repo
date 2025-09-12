package pkgDriver;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Driver {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    long window;

    // window
    private static int WIN_WIDTH  = 1000;
    private static int WIN_HEIGHT = 800;
    private static final int WIN_POS_X = 30;
    private static final int WIN_POS_Y = 90;
    private static final int MSAA_SAMPLES = 8;
    private static final int VSYNC_INTERVAL = 1;

    private static final float ASPECT = (float) WIN_WIDTH / WIN_HEIGHT;

    // ortho world
    private static final float ORTHO_LEFT   = -100f;
    private static final float ORTHO_RIGHT  =  100f;
    private static final float ORTHO_BOTTOM = -100f;
    private static final float ORTHO_TOP    =  100f;
    private static final float ORTHO_NEAR   =    0f;
    private static final float ORTHO_FAR    =   10f;

    // square size as world units
    private static final float SQUARE_SIDE = 40f;

    // color Silver
    private static final float[] SQUARE_COLOR = {0.75f, 0.75f, 0.75f};

    // background
    private static final float[] CLEAR_COLOR = {0.043f, 0.380f, 0.588f, 1.0f};

    // GL state
    private static final int OGL_MATRIX_SIZE = 16;
    int shader_program;
    int vpMatLocation = 0;
    int renderColorLocation = 0;
    Matrix4f viewProjMatrix = new Matrix4f();
    FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);

    // buffers
    int vbo;
    int ibo;

    public static void main(String[] args) {
        new Driver().render();
    }

    void render() {
        try {
            initGLFWindow();
            initOpenGL();
            initBuffers();
            loop();
            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void initGLFWindow() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, MSAA_SAMPLES);

        window = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, "CSC 133", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    WIN_WIDTH = w;
                    WIN_HEIGHT = h;
                    glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
                }
            }
        });

        glfwSetWindowPos(window, WIN_POS_X, WIN_POS_Y);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(VSYNC_INTERVAL);
        glfwShowWindow(window);
    }

    void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(CLEAR_COLOR[0], CLEAR_COLOR[1], CLEAR_COLOR[2], CLEAR_COLOR[3]);

        // shader using built-ins
        shader_program = glCreateProgram();

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        "  gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        "  gl_FragColor = vec4(color, 1.0);" +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);

        glLinkProgram(shader_program);
        glUseProgram(shader_program);

        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        renderColorLocation = glGetUniformLocation(shader_program, "color");

        // set projection once
        viewProjMatrix.setOrtho(ORTHO_LEFT, ORTHO_RIGHT, ORTHO_BOTTOM, ORTHO_TOP, ORTHO_NEAR, ORTHO_FAR);
        glUniformMatrix4fv(vpMatLocation, false, viewProjMatrix.get(myFloatBuffer));
    }

    void initBuffers() {
        // generate
        vbo = GL15.glGenBuffers();
        ibo = GL15.glGenBuffers();

        // indices for 2 triangles (quad)
        final int[] indices = {0, 1, 2, 0, 2, 3};

        // upload indices once to ELEMENT_ARRAY_BUFFER
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER,
                (IntBuffer) BufferUtils.createIntBuffer(indices.length).put(indices).flip(),
                GL15.GL_STATIC_DRAW);

        // bind VBO and allocate an initial empty buffer (we will update per-draw)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        // allocate enough space for 4 vertices * 2 components (float)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 4 * 2 * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

        // Enable client vertex array and set pointer - VBO must be bound BEFORE this call
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 0, 0L);
    }

    // Create a square *centered* at the origin (0,0)
    private float[] createCenteredSquare(float side) {
        float half = side / 2f;
        return new float[]{
                -half, -half,   // bottom-left
                half, -half,   // bottom-right
                half,  half,   // top-right
                -half,  half    // top-left
        };
    }

    // Draw by uploading vertex data to VBO and issuing drawElements
    private void drawVerticesSingleQuad(float[] verts, float r, float g, float b) {
        // bind and upload vertex data
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer fb = (FloatBuffer) BufferUtils.createFloatBuffer(verts.length).put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_DYNAMIC_DRAW);

        // ensure vertex pointer refers to current VBO (safe to call here)
        glVertexPointer(2, GL_FLOAT, 0, 0L);

        // set color uniform
        glUniform3f(renderColorLocation, r, g, b);

        // draw
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
    }

    // Create a square centered at an offset (cx, cy)
    private float[] createOffsetSquare(float side, float cx, float cy) {
        float half = side / 2f;
        return new float[]{
                cx - half, cy - half,
                cx + half, cy - half,
                cx + half, cy + half,
                cx - half, cy + half
        };
    }

    void loop() {
        // size
        final float side = SQUARE_SIDE;

        // offset by half-size so corners meet at origin
        final float offset = side / 2f;

        // Square 1: shifted up-left
        float[] square1 = createOffsetSquare(side, -offset, +offset);

        // Square 2: shifted down-right
        float[] square2 = createOffsetSquare(side, +offset, -offset);

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // draw first (green)
            drawVerticesSingleQuad(square1, SQUARE_COLOR[0], SQUARE_COLOR[1], SQUARE_COLOR[2]);

            // draw second (also green)
            drawVerticesSingleQuad(square2, SQUARE_COLOR[0], SQUARE_COLOR[1], SQUARE_COLOR[2]);

            glfwSwapBuffers(window);
        }
    }

}
