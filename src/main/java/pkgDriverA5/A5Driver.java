package pkgDriverA5;

import org.joml.Vector3f;
import pkgSlRenderEngine.SlCamera;
import pkgSlUtilities.SlWindowManager;
import pkgSlRenderEngine.SlRenderer;
import pkgSlRenderEngine.SlCARenderer;

import static pkgDriverA5.A5SlSpot.*;

public class A5Driver {
    public static void main(String[] args) {
        SlWindowManager my_win = SlWindowManager.get(WIN_WIDTH, WIN_HEIGHT);
        int NUM_ROWS = 16, NUM_COLS = 16;

        // Fallback input file name if no argument is passed
        String inputFile = "C:\\Users\\jeoju\\Documents\\Second Semester Fall 2025\\CSC 133 Object Oriented Graph\\ASSIGNMENT_5\\src\\gol_input_1.txt";

        if (args.length > 0) {
            inputFile = args[0];
        }

        float[] camParams = {FRUSTUM_LEFT, FRUSTUM_RIGHT, FRUSTUM_BOTTOM, FRUSTUM_TOP, Z_NEAR, Z_FAR };
        final Vector3f myCameraLocation = new Vector3f(0, 0, 0.0f);
        SlCamera myCamera = new SlCamera(camParams, myCameraLocation);
        //SlRenderer currentScene = new SlCARenderer(my_win, myCamera, args[0]);
        SlRenderer currentScene = new SlCARenderer(my_win, myCamera, inputFile);

        currentScene.initOpenGL();
        currentScene.initRendering(NUM_ROWS, NUM_COLS);
        currentScene.renderScene();
        my_win.destroyGlfwWindow();
    }  //  public static void main(String[] args)
}  //  public class Driver
