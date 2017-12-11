package com.deepwelldevelopment.spacequest.renderer;

import com.deepwelldevelopment.spacequest.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Pipeline {

    class Orientation {
        Vector3f scale;
        Vector3f rotation;
        Vector3f pos;

        Orientation() {
            scale = new Vector3f(1.0f, 1.0f, 1.0f);
            rotation = new Vector3f(0.0f, 0.0f, 0.0f);
            pos = new Vector3f(0.0f, 0.0f, 0.0f);
        }
    }

    class PersProjInfo {
        float fov;
        float width;
        float height;
        float zNear;
        float zFar;
    }

    private Vector3f scale;
    private Vector3f worldPos;
    private Vector3f rotateInfo;

    private PersProjInfo persProjInfo;

    private Camera camera;

    private Matrix4f wvpTransformation;
    private Matrix4f vpTransformation;
    private Matrix4f wpTransformation;
    private Matrix4f wvTransformation;
    private Matrix4f wTransformation;
    private Matrix4f vTransformation;
    private Matrix4f projTransformation;

    public Pipeline() {
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
        worldPos = new Vector3f(0.0f, 0.0f, 0.0f);
        rotateInfo = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public void scale(float s) {
        scale(s, s, s);
    }

    public void scale(Vector3f s) {
        scale(s.x, s.y, s.z);
    }

    public void scale(float x, float y, float z) {
        scale.set(x, y, z);
    }

    public void worldPos(float x, float y, float z) {
        worldPos.x = x;
        worldPos.y = y;
        worldPos.z = z;
    }

    public void worldPos(Vector3f pos) {
        worldPos = pos;
    }

    public void rotate(Vector3f r) {
        rotate(r.x, r.y, r.z);
    }

    public void rotate(float rotateX, float rotateY, float rotateZ) {
        rotateInfo.x = rotateX;
        rotateInfo.y = rotateY;
        rotateInfo.z = rotateZ;
    }

    public Matrix4f getWVPTransformation() {
        return wvpTransformation;
    }

    public Matrix4f getVPTransformation() {
        return vpTransformation;
    }

    public Matrix4f getWPTransformation() {
        return wpTransformation;
    }

    public Matrix4f getWVTransformation() {
        return wvTransformation;
    }

    public Matrix4f getWorldTransformation() {
        return wTransformation;
    }

    public Matrix4f getViewTransformation() {
        return vTransformation;
    }

    public Matrix4f getProjTransformation() {
        initPersProjtransform(projTransformation, persProjInfo);
        return projTransformation;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    void initPersProjtransform(Matrix4f m, PersProjInfo p) {
        float ar = p.width / p.height;
        m.setPerspective(p.fov, ar, p.zNear, p.zFar);
    }
}
