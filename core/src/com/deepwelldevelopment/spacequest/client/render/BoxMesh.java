package com.deepwelldevelopment.spacequest.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ShortArray;

public class BoxMesh {

    private static int meshBuilding;
    protected final Object rebuilding = new Object();
    protected Matrix4 transform;
    protected Matrix4 transformWithRealY;
    protected FloatArray vertices;
    protected ShortArray indices;
    protected FloatArray nonCollidableVertices;
    protected ShortArray nonCollidableIndices;
    private float[] v;
    private short[] i;
    private float[] nonCollidableV;
    private short[] nonCollidableI;
    private boolean needsRebuild = false;
    private Mesh mesh;
    private Mesh nonCollidableMesh;

    private BoundingBox meshBoundingBox = new BoundingBox();
    private BoundingBox nonCollidableMeshBoundingBox = new BoundingBox();

    public void update() {
        if (needsRebuild) {
            rebuild();
        }
    }

    protected void setupMesh() {
        if (vertices == null) {
            vertices = new FloatArray();
        }
        if (indices == null) {
            indices = new ShortArray();
        }
        if (nonCollidableVertices == null) {
            nonCollidableVertices = new FloatArray();
        }
        if (nonCollidableIndices == null) {
            nonCollidableIndices = new ShortArray();
        }
    }

    private void rebuild() {
        if (meshBuilding < 4) {
            meshBuilding += 2;
            Gdx.app.postRunnable(() -> {
                try {
                    synchronized (rebuilding) {
                        rebuild(vertices, indices, v, i, transform, true);
                        if (nonCollidableVertices.size > 0) {
                            rebuild(nonCollidableVertices, nonCollidableIndices, nonCollidableV,
                                    nonCollidableI, transform, false
                            );
                        } else {
                            meshBuilding--;
                        }
                        needsRebuild = false;
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private void rebuild(FloatArray vertices, ShortArray indices, float[] v, short[] i,
            Matrix4 transform, boolean tempAddToPhysics) {
        //TODO: work with physics
        Mesh inProgressMesh = null;
        try {
            inProgressMesh = new Mesh(true, 4 * (v.length / 12), 6 * i.length,
                    VertexAttribute.Position(), VertexAttribute.TexCoords(0),
                    VertexAttribute.TexCoords(1), VertexAttribute.Normal(),
                    VertexAttribute.ColorUnpacked()
            );
            inProgressMesh.setVertices(v);
            inProgressMesh.setIndices(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        vertices.clear();
        indices.clear();
        vertices.shrink();
        indices.shrink();

        try {
            if (inProgressMesh.getNumVertices() > 0 && inProgressMesh.getNumIndices() > 0) {
                //add ground mesh to physics
                if (!tempAddToPhysics) {
                    //remove the mesh from physics
                } else {
                    //remove the nc mesh from physics
                }
            }
        } catch (GdxRuntimeException e) {
            System.err.println("A gdx exception occurred. Behavior may be undefined");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tempAddToPhysics) {
            setMesh(inProgressMesh);
        } else {
            setNonCollidableMesh(inProgressMesh);
        }
        meshBuilding--;
    }

    public Matrix4 getTransform() {
        return transform;
    }

    public Mesh getMesh() {
        return mesh;
    }

    private void setMesh(Mesh mesh) {
        if (mesh != null && mesh.getNumVertices() > 0 && mesh.getNumIndices() > 0) {
            if (this.mesh != null) {
                this.mesh.dispose();
                this.mesh = null;
            }
            this.mesh = mesh;
            this.mesh.calculateBoundingBox(meshBoundingBox);
        }
    }

    public Mesh getNonCollidableMesh() {
        return nonCollidableMesh;
    }

    private void setNonCollidableMesh(Mesh mesh) {
        if (nonCollidableMesh != null && nonCollidableMesh.getNumVertices() > 0 &&
                nonCollidableMesh.getNumIndices() > 0) {
            if (this.nonCollidableMesh != null) {
                this.nonCollidableMesh.dispose();
                this.nonCollidableMesh = null;
            }
            this.nonCollidableMesh = mesh;
            this.nonCollidableMesh.calculateBoundingBox(nonCollidableMeshBoundingBox);
        }
    }

    public boolean isNeedsRebuild() {
        return needsRebuild;
    }

    public void setNeedsRebuild() {
        synchronized (rebuilding) {
            v = vertices.toArray();
            i = indices.toArray();
            nonCollidableV = nonCollidableVertices.toArray();
            nonCollidableI = nonCollidableIndices.toArray();
            needsRebuild = true;
        }
    }

    public BoundingBox getMeshBoundingBox() {
        return meshBoundingBox;
    }

    public BoundingBox getNonCollidableMeshBoundingBox() {
        return nonCollidableMeshBoundingBox;
    }
}
