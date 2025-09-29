package pkgDriver;

import pkgSlUtilities.SlWindowManager;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Driver {

    private static final float SQUARE_SIDE = 40f;
    private static final float[] SQUARE_COLOR = {0.75f, 0.75f, 0.75f};
    private static final float[] CLEAR_COLOR = {0.043f, 0.380f, 0.588f, 1.0f};
    private static final int OGL_MATRIX_SIZE = 16;

    int shader_program;
    int vpMatLocation;
    int renderColorLocation;
    Matrix4f viewProjMatrix = new Matrix4f();
    FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);

    int vbo;
    int ibo;

    // Use our window manager
    SlWindowManager windowManager = new SlWindowManager();

    public static void main(String[] args) {
        new Driver().render();
    }

    void render() {
        try {
            windowManager.initGLFWindow();
            initOpenGL();
            initBuffers();
            loop();
            windowManager.destroyGlfwWindow();
        } finally {
            // cleanup handled in SlWindowManager
        }
    }

    void initOpenGL() {
        windowManager.updateContextToThis();
        org.lwjgl.opengl.GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, windowManager.getWidth(), windowManager.getHeight());
        glClearColor(CLEAR_COLOR[0], CLEAR_COLOR[1], CLEAR_COLOR[2], CLEAR_COLOR[3]);

        shader_program = glCreateProgram();

        // Vertex shader
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        "  gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);

        // Fragment shader
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

        viewProjMatrix.setOrtho(-100f, 100f, -100f, 100f, 0f, 10f);
        glUniformMatrix4fv(vpMatLocation, false, viewProjMatrix.get(myFloatBuffer));
    }

    void initBuffers() {
        vbo = GL15.glGenBuffers();
        ibo = GL15.glGenBuffers();

        final int[] indices = {0, 1, 2, 0, 2, 3};

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER,
                (IntBuffer) BufferUtils.createIntBuffer(indices.length).put(indices).flip(),
                GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 4 * 2 * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 0, 0L);
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

    private void drawVerticesSingleQuad(float[] verts, float r, float g, float b) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer fb = BufferUtils.createFloatBuffer(verts.length).put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_DYNAMIC_DRAW);

        glVertexPointer(2, GL_FLOAT, 0, 0L);

        glUniform3f(renderColorLocation, r, g, b);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
    }

    void loop() {
        float[] square1 = createOffsetSquare(SQUARE_SIDE, -SQUARE_SIDE / 2, SQUARE_SIDE / 2);
        float[] square2 = createOffsetSquare(SQUARE_SIDE, SQUARE_SIDE / 2, -SQUARE_SIDE / 2);

        while (!windowManager.isGlfwWindowClosed()) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            drawVerticesSingleQuad(square1, SQUARE_COLOR[0], SQUARE_COLOR[1], SQUARE_COLOR[2]);
            drawVerticesSingleQuad(square2, SQUARE_COLOR[0], SQUARE_COLOR[1], SQUARE_COLOR[2]);

            windowManager.swapBuffers();
        }
    }
}
