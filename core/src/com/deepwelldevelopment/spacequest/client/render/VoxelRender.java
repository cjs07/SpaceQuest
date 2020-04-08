package com.deepwelldevelopment.spacequest.client.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoxelRender implements RenderableProvider {

    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    private static int numberOfVertices = 0;
    private static int numberOfIndices = 0;
    private static int numberOfVisibleChunks = 0;
    private static int numberOfChunks = 0;
    private static int blockCounter = 0;
    private static World terrain;
    private static int numberOfVisibleBlocks;
    private final PerspectiveCamera camera;
    private final Vector3 worldChunkPosition = new Vector3();
    private final Vector3 center = new Vector3();
    private final Vector3 dimensions = new Vector3();
    private Material material;
    private boolean alpha;

    public VoxelRender(PerspectiveCamera camera, Material material, boolean alpha, World terrain) {
        this.camera = camera;
        this.material = material;
        this.alpha = alpha;
        VoxelRender.terrain = terrain;
    }

    /**
     * Returns {@link Renderable} instances. Renderables are obtained from the provided
     * {@link Pool} and added to the provided array. The Renderables obtained using
     * {@link Pool#obtain()} will later be put back into the pool, do not store them
     * internally. The resulting array can be rendered via a {@link ModelBatch}.
     *
     * @param renderables the output array
     * @param pool        the pool to obtain Renderables from
     */
    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        numberOfVertices = 0;
        numberOfIndices = 0;
        numberOfVisibleChunks = 0;
        blockCounter = 0;
        numberOfVisibleBlocks = 0;
        numberOfChunks = 0;

        for (Chunk chunk : terrain.getChunks()) {
            numberOfChunks++;
            blockCounter += chunk.getBlockCounter();
            worldChunkPosition.set(chunk.getWorldPosition());
            worldChunkPosition.y = camera.position.y;
            boolean b = camera.frustum.sphereInFrustum(worldChunkPosition, World.CHUNK_WIDTH * 2f);
            if (!b) {
                chunk.setActive(false);
            } else {
                numberOfVisibleBlocks += chunk.getBlockCounter();
                numberOfVisibleChunks++;
                chunk.setActive(true);
            }

            Array<VoxelMesh> meshes;
            if (alpha) {
                meshes = chunk.getAlphaMeshes();
            } else {
                meshes = chunk.getMeshes();
            }
            for (final BoxMesh boxMesh : meshes) {
                if (boxMesh == null) continue;
                Mesh mesh = boxMesh.getMesh();
                Mesh nonCollidableMesh = boxMesh.getNonCollidableMesh();
                if (mesh != null) {
                    BoundingBox boundingBox = boxMesh.getMeshBoundingBox();
                    boundingBox.getCenter(center);
                    boundingBox.getDimensions(dimensions);
                    worldChunkPosition.set(chunk.getWorldPosition());
                    worldChunkPosition.add(center);
                    if (camera.frustum.boundsInFrustum(worldChunkPosition, dimensions)) {
                        addToRenderables(renderables, pool, mesh, boxMesh.getTransform());
                    }
                }
                if (nonCollidableMesh != null) {
                    BoundingBox boundingBox = boxMesh.getNonCollidableMeshBoundingBox();
                    boundingBox.getCenter(center);
                    boundingBox.getDimensions(dimensions);
                    worldChunkPosition.set(chunk.getWorldPosition());
                    worldChunkPosition.add(center);
                    if (camera.frustum.boundsInFrustum(worldChunkPosition, dimensions)) {
                        addToRenderables(renderables, pool, nonCollidableMesh,
                                boxMesh.getTransform()
                        );
                    }
                }
                if (boxMesh.isNeedsRebuild() && !chunk.isRecalculating()) {
                    executorService.submit(boxMesh::update);
                }

            }
        }
    }

    private void addToRenderables(Array<Renderable> renderables, Pool<Renderable> pool,
            Mesh mesh, Matrix4 transform) {
        if (mesh.getNumVertices() < 1 || mesh.getNumIndices() < 1) return;
        Renderable renderable = pool.obtain();
        renderable.material = material;
        renderable.meshPart.offset = 0;
        renderable.meshPart.size = mesh.getNumIndices();
        renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
        renderable.meshPart.mesh = mesh;
        renderables.add(renderable);

        renderable.worldTransform.set(transform);

        numberOfVertices += mesh.getNumVertices();
        numberOfIndices += mesh.getNumIndices();

    }

    public static int getNumberOfVertices() {
        return numberOfVertices;
    }

    public static int getNumberOfIndices() {
        return numberOfIndices;
    }

    public static int getNumberOfVisibleChunks() {
        return numberOfVisibleChunks;
    }

    public static int getNumberOfChunks() {
        return numberOfChunks;
    }

    public static int getBlockCounter() {
        return blockCounter;
    }

    public static int getNumberOfVisibleBlocks() {
        return numberOfVisibleBlocks;
    }
}
