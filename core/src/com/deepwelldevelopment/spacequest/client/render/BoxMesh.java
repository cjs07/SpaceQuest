package com.deepwelldevelopment.spacequest.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ShortArray;
import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;

public class BoxMesh {

    private static int meshBuilding;
    public final Object rebuilding = new Object();
    protected Matrix4 transform;
    protected Matrix4 transformWithRealY;
    protected FloatArray vertices;
    protected ShortArray indices;
    protected FloatArray nonCollidableVertices;
    protected ShortArray nonCollidableIndicies;
    private float[] v;
    private short[] i;
    private float[] nonCollidableV;
    private short[] nonCollidableI;
    private boolean needsRebuild = false;
    private Mesh mesh;
    private Mesh nonColliadableMesh;

    private BoundingBox meshBoundingBox = new BoundingBox();
    private BoundingBox nonColliadableMeshBoundingBox = new BoundingBox();

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
        return nonColliadableMesh;
    }

    private void setNonCollidableMesh(Mesh mesh) {
        if (mesh != null && mesh.getNumVertices() > 0 && mesh.getNumIndices() > 0) {
            if (this.nonColliadableMesh != null) {
                this.nonColliadableMesh.dispose();
                this.nonColliadableMesh = null;
            }
            this.nonColliadableMesh = mesh;
            this.nonColliadableMesh.calculateBoundingBox(nonColliadableMeshBoundingBox);
        }
    }

    public boolean needsRebuild() {
        return needsRebuild;
    }

    public void update() {
        if (needsRebuild) {
            rebuild();
        }
    }

    public void setNeedsRebuild() {
        synchronized (rebuilding) {
            v = vertices.toArray();
            i = indices.toArray();
            nonCollidableV = nonCollidableVertices.toArray();
            nonCollidableI = nonCollidableIndicies.toArray();
            needsRebuild = true;
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
        if (nonCollidableIndicies == null) {
            nonCollidableIndicies = new ShortArray();
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
                            rebuild(nonCollidableVertices, nonCollidableIndicies, nonCollidableV, nonCollidableI,
                                    transform, false);
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

    public BoundingBox getMeshBoundingBox() {
        return meshBoundingBox;
    }

    public BoundingBox getNonColliadableMeshBoundingBox() {
        return nonColliadableMeshBoundingBox;
    }

    private void rebuild(FloatArray vertices, ShortArray indices, float[] v, short[] i, Matrix4 transform,
                         boolean tempAddToPhysics) {
        PhysicsController physicsController = SpaceQuest.getSpaceQuest().getPhysicsController();
        Mesh inProgressMesh = null;
        try {
            inProgressMesh = new Mesh(true, 4 * (v.length / 12), 6 * i.length, VertexAttribute.Position(),
                    VertexAttribute.TexCoords(0), VertexAttribute.TexCoords(1), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
            inProgressMesh.setVertices(v);
            inProgressMesh.setIndices(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        vertices.clear();
        indices.clear();
        vertices.shrink();
        indices.shrink();

//        if (tempAddToPhysics) {
        try {
            if (inProgressMesh.getNumVertices() > 0 && inProgressMesh.getNumIndices() > 0) {
                physicsController.addGroundMesh(inProgressMesh, transform, !tempAddToPhysics);
                if (!tempAddToPhysics) {
                    physicsController.removeMesh(getNonCollidableMesh());
                } else {
                    physicsController.removeMesh(getMesh());
                }

            }
        } catch (GdxRuntimeException ex) {
            // need to figure out the "Mesh must be indexed and triangulated" exception
        } catch (Exception ex) {
            ex.printStackTrace();

        }
//        }

        if (tempAddToPhysics) {
            setMesh(inProgressMesh);
        } else {
            setNonCollidableMesh(inProgressMesh);
        }

        meshBuilding--;
    }
}
