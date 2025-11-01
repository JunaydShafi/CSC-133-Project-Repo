/**package pkgDriverA5;

import org.joml.Vector3f;
import pkgSlRenderEngine.SlCARenderer;
import pkgSlRenderEngine.SlCamera;
import pkgSlRenderEngine.SlRenderer;
import pkgSlUtilities.SlWindowManager;

import static pkgDriver.SlSpot.*;
import static pkgDriver.SlSpot.FRUSTUM_BOTTOM;
import static pkgDriver.SlSpot.FRUSTUM_RIGHT;
import static pkgDriver.SlSpot.FRUSTUM_TOP;
import static pkgDriver.SlSpot.Z_FAR;
import static pkgDriver.SlSpot.Z_NEAR;

public class A5Driver {
    public static void main(String[] args) {
        SlWindowManager my_win = SlWindowManager.get(WIN_WIDTH, WIN_HEIGHT);
        int NUM_ROWS = 19, NUM_COLS = 22;

        float[] camParams = {FRUSTUM_LEFT, FRUSTUM_RIGHT, FRUSTUM_BOTTOM, FRUSTUM_TOP, Z_NEAR, Z_FAR };
        final Vector3f myCameraLocation = new Vector3f(0, 0, 0.0f);
        SlCamera myCamera = new SlCamera(camParams, myCameraLocation);
        SlRenderer currentScene = new SlCARenderer(my_win, myCamera, args[0]);

        currentScene.initOpenGL();
        currentScene.initRendering(NUM_ROWS, NUM_COLS);
        currentScene.renderScene();
        my_win.destroyGlfwWindow();
    }  //  public static void main(String[] args)
}  //  public class Driver**/
