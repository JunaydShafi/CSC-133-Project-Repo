/**package pkgSlRenderEngine;


import org.joml.Matrix4f;
import org.joml.Vector3f;

import static pkgDriver.SlSpot.*;

public class SlCamera {
    private Matrix4f projectionMatrix, viewMatrix;
    public Vector3f defaultLookFrom;
    public Vector3f defaultLookAt;
    public Vector3f defaultUpVector;

    // Inside getViewMatrix() or elsewhere, don't update the above: will be needed
    // if we reset the camera to starting position. Instead mutate the following where needed:
    public Vector3f curLookAt;
    public Vector3f curLookFrom;
    public Vector3f curUpVector;

    private float[]  frustum;

    private void init_camera() {
        defaultLookFrom = new Vector3f(0.0f, 0.0f, 00.0f);
        defaultLookAt = new Vector3f(0.0f, 0.0f, -1.0f);
        defaultUpVector = new Vector3f(0.0f, 1.0f, 0.0f);

        curLookFrom = new Vector3f(defaultLookFrom);
        curLookAt   = new Vector3f(defaultLookAt);
        curUpVector = new Vector3f(defaultUpVector);
    }

    public SlCamera(Vector3f look_from, Vector3f look_at, Vector3f view_up) {
        defaultLookFrom.set(look_from);
        defaultLookAt.set(look_at);
        defaultUpVector.set(view_up);

        curLookFrom = new Vector3f(defaultLookFrom);
        curLookAt   = new Vector3f(defaultLookAt);
        curUpVector = new Vector3f(defaultUpVector);
    }

    public SlCamera(float[] my_frustum, Vector3f lf_position) {
        frustum = my_frustum.clone();
        init_camera();
        defaultLookFrom.set(lf_position);
        curLookFrom.set(lf_position);
        projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        viewMatrix = new Matrix4f();
        viewMatrix.identity();
        setOrthoProjection();
    }

    public void setOrthoProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(frustum[0], frustum[1], frustum[2],
                frustum[3], frustum[4], frustum[5]);
    }

    // Careful: this is camera movement to imitate an object moving right. So
    // positive arguments are interpreted as negative increment to CURRENT position
    public void relativeMoveCamera(float deltaX, float deltaY) {
        curLookFrom.x -= deltaX;
        curLookFrom.y -= deltaY;
    }  // public void relativeMoveCamera(...)

    public void moveCameraWithinWindow(float deltaX, float deltaY) {
        relativeMoveCamera(deltaX, deltaY);
        if (curLookFrom.x < -FRUSTUM_RIGHT || curLookFrom.y < -FRUSTUM_TOP) {
            restoreCamera();
        }
    }
    void restoreCamera() {
        init_camera();
        curLookFrom.set(defaultLookFrom);
        projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        viewMatrix = new Matrix4f();
        viewMatrix.identity();
        setOrthoProjection();
    }

    public Vector3f getCurLookFrom() {
        return curLookFrom;
    }

    public void setCurLookFrom(Vector3f new_lf) {
        curLookFrom.set(new_lf);
    }

    public Vector3f getCurLookAt() {
        return curLookAt;
    }

    public void setCurLookAt(Vector3f new_la) {
        curLookAt.set(new_la);
    }

    public Matrix4f getViewMatrix() {
        curLookAt.set(defaultLookAt);
        viewMatrix.identity();
        viewMatrix.lookAt(curLookFrom, curLookAt.add(curLookFrom), curUpVector);

        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}
**/