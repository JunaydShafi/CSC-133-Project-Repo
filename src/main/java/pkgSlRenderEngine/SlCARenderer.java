package pkgSlRenderEngine;

import pkgSlUtilities.*;
import org.joml.Vector3f;

public class SlCARenderer extends SlRenderer {

    private SlCamera camera;
    private SlPingPongArray gridArray;
    private String fileName;

    public SlCARenderer(SlWindowManager win, SlCamera cam, String dataFile) {
        super(win);
        this.camera = cam;
        this.fileName = dataFile;
        System.out.println("SlCARenderer created with data file: " + dataFile);
    }

    @Override
    public void initRendering(int numRows, int numCols) {
        System.out.println("CARenderer initializing rendering for " + numRows + "x" + numCols);
        gridArray = new SlPingPongArray(numRows, numCols);
        super.initRendering(numRows, numCols);
    }

    @Override
    public void renderScene() {
        System.out.println("CARenderer rendering scene...");
        windowManager.runRenderLoop(() -> {
            gridArray.randomizeViaFisherYatesKnuth();
            // TODO: Draw grid cells here using windowManager.drawQuad(...)
        });
    }
}
