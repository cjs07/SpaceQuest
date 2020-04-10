package com.deepwelldevelopment.spacequest.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy.CollisionFilterGroups;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.deepwelldevelopment.spacequest.client.render.BoxMesh;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.google.common.collect.ArrayListMultimap;
import net.dermetfan.utils.Pair;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class PhysicsController {

    private static final Object syncToken = new Object();

    private static final boolean debug = false;
    private static final Vector3 gravity = new Vector3(0, -9.81f, 0);
    private static final short NOTHING = 0;
    private static final short WORLD = 1;
    private static final short PLAYER = 2;
    private static final short PLAYER_COLLIDES_WITH = WORLD;
    private static final short WORLD_COLLIDES_WITH = PLAYER;

    private Vector3 jump = new Vector3(0, 5f, 0);
    private btDefaultCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btSequentialImpulseConstraintSolver solver;
    private btDiscreteDynamicsWorld collisionWorld;
    private Array<Pair<btRigidBody, BoxMesh>> dynamicBodies;
    private HashMap<Mesh, btRigidBody> meshes;
    private ArrayListMultimap<Chunk, BoxMesh> chunkMeshMap;
    private WorldInternalTickCallback worldInternalTickCallback;
    private DebugDrawer debugDrawer;
    private Array<btRigidBody> entities = new Array<>();
    private Array<btRigidBodyConstructionInfo> constructions = new Array<>();
    private Camera camera;
    private Vector3 tmp = new Vector3();
    private Vector3 tmp2 = new Vector3();
    private HashMap<btRigidBody, CollisionObject> entityRigidBodies;
    private HashMap<CollisionObject, btRigidBody> entityCollisionObjects;
    private ClosestRayResultCallback rayResultCallback;
    private Vector3 rayFrom;
    private Vector3 rayTo;
    private Array<btMotionState> states = new Array<>();
    private Array<btRigidBodyConstructionInfo> info = new Array<>();
    private Array<btCollisionShape> shapes = new Array<>();
    private Array<btRigidBody> bodies = new Array<>();
    private Array<btKinematicCharacterController> controllers = new Array<>();
    private btKinematicCharacterController characterController;
    private btPairCachingGhostObject playerGhostObject;
    private btAxisSweep3 btSweep3;
    private btCapsuleShape capsuleShape;
    private btBoxShape boxShape;
    private int numberOfTicks;
    private Vector3 previousPosition = new Vector3();

    private ModelBuilder modelBuilder = new ModelBuilder();
    private boolean playerInWater;
    private boolean flight;

    private World world;

    public PhysicsController(World world, Camera camera) {
        this.world = world;
        this.camera = camera;

        Bullet.init(true);
        btSweep3 = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000,
                1000
        ));
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, btSweep3, solver,
                collisionConfiguration
        );
        collisionWorld.setGravity(gravity);
        collisionWorld.getDispatchInfo().setAllowedCcdPenetration(0.0001f);
        dynamicBodies = new Array<>();
        entityRigidBodies = new HashMap<>();
        entityCollisionObjects = new HashMap<>();
        meshes = new HashMap<>();
        chunkMeshMap = ArrayListMultimap.create();

        if (debug) {
            collisionWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
        }

        worldInternalTickCallback = new WorldInternalTickCallback(collisionWorld);
        rayResultCallback = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);

        playerGhostObject = new btPairCachingGhostObject();
        Matrix4 matrix4 = new Matrix4().setToTranslation(camera.position);
        playerGhostObject.setWorldTransform(matrix4);
        btSweep3.getOverlappingPairCache().setInternalGhostPairCallback(new btGhostPairCallback());

        capsuleShape = new btCapsuleShapeZ(0.45f, 0.9f);
        boxShape = new btBoxShape(new Vector3(0.4f, 0.4f, 0.9f));

        playerGhostObject.setCollisionShape(capsuleShape);
        playerGhostObject.setCollisionFlags(CollisionFlags.CF_CHARACTER_OBJECT);
        characterController = new btKinematicCharacterController(playerGhostObject, capsuleShape,
                0.25f
        );
        characterController.setGravity(gravity);

        collisionWorld.addCollisionObject(playerGhostObject,
                CollisionFilterGroups.CharacterFilter,
                CollisionFilterGroups.StaticFilter | CollisionFilterGroups.DefaultFilter
        );
        collisionWorld.addAction(characterController);
        controllers.add(characterController);
    }

    public void movePlayer(Vector3 force, boolean jump) {
        if (playerInWater && jump) {
            characterController.getGravity().y = 5.5f;
        } else if (playerInWater) {
            characterController.getGravity().y = -0.1f;
        } else if (characterController.getGravity().y != gravity.y) {
            characterController.setGravity(gravity);
        }

        if (playerInWater) {
            force.scl(0.3f);
        }

        characterController.setWalkDirection(force);
        if (jump && characterController.canJump()) {
            characterController.jump(this.jump);
        }
    }

    public boolean isPlayerInWater() {
        return playerInWater;
    }

    public void setPlayerInWater(boolean playerInWater) {
        this.playerInWater = playerInWater;
    }

    public void toggleFlight() {
        flight = !flight;
    }

    public CollisionObject addEntity(float height, float width, Matrix4 transform) {
        btBoxShape collisionShape = new btBoxShape(new Vector3(width / 2, height, width / 2));
        btMotionState dynamicMotionState = new btDefaultMotionState();
        dynamicMotionState.setWorldTransform(transform);
        Vector3 dynamicInertia = new Vector3(0, 0, 0);
        collisionShape.calculateLocalInertia(1f, dynamicInertia);
        btRigidBodyConstructionInfo dynamicConstructionInfo = new btRigidBodyConstructionInfo(1f,
                dynamicMotionState, collisionShape, dynamicInertia
        );
        constructions.add(dynamicConstructionInfo);
        btRigidBody body = new btRigidBody(dynamicConstructionInfo);
        body.setActivationState(4);
        body.setContactProcessingThreshold(0.0f);
        body.setRestitution(0);
        body.setDamping(0.9f, 0.9f);
        body.setLinearFactor(new Vector3(1, 1, 1));
        body.setAngularFactor(Vector3.Zero);
        body.setContactCallbackFlag(2);
        body.setContactCallbackFilter(2);
        collisionWorld.addRigidBody(body);
        CollisionObject collisionObject = new CollisionObject();
        entityRigidBodies.put(body, collisionObject);
        entityCollisionObjects.put(collisionObject, body);
        return collisionObject;
    }

    public void removeEntity(CollisionObject collisionObject) {
        if (entityCollisionObjects.containsKey(collisionObject)) {
            btRigidBody rigidBody = entityCollisionObjects.remove(collisionObject);
            collisionWorld.removeCollisionObject(rigidBody);
            entityRigidBodies.remove(rigidBody);
        }
    }

    public void addGroundMesh(Mesh mesh, Matrix4 transform, boolean nonCollidable) {
        synchronized (syncToken) {
            modelBuilder.begin();
            MeshPart part = modelBuilder.part(UUID.randomUUID().toString(), mesh,
                    GL20.GL_TRIANGLES, null
            );
            modelBuilder.end();

            Array<MeshPart> meshParts = new Array<>();
            meshParts.add(part);
            btBvhTriangleMeshShape btBvhTriangleMeshShape = new btBvhTriangleMeshShape(meshParts);
            shapes.add(btBvhTriangleMeshShape);

            btMotionState groundMotionState = new btDefaultMotionState();
            states.add(groundMotionState);
            groundMotionState.setWorldTransform(transform);
            btRigidBodyConstructionInfo groundBodyConstructionInfo =
                    new btRigidBodyConstructionInfo(0, groundMotionState, btBvhTriangleMeshShape,
                            new Vector3(0, 0, 0)
                    );
            constructions.add(groundBodyConstructionInfo);
            groundBodyConstructionInfo.setFriction(0);
            btRigidBody groundRigidBody = new btRigidBody(groundBodyConstructionInfo);

            if (!nonCollidable) {
                collisionWorld.addRigidBody(groundRigidBody, CollisionFilterGroups.StaticFilter,
                        CollisionFilterGroups.CharacterFilter | CollisionFilterGroups.DefaultFilter
                );
            } else {
                collisionWorld.addRigidBody(groundRigidBody, 64,
                        CollisionFilterGroups.CharacterFilter | CollisionFilterGroups.DefaultFilter
                );
            }

            entities.add(groundRigidBody);
            meshes.put(mesh, groundRigidBody);
        }
    }

    public void removeMesh(Mesh mesh) {
        synchronized (syncToken) {
            btRigidBody rigidBody = meshes.get(mesh);
            if (rigidBody != null) {
                collisionWorld.removeRigidBody(rigidBody);
                rigidBody.dispose();
            }

            if (chunkMeshMap.containsValue(mesh)) {
                chunkMeshMap.values().remove(mesh);
            }
        }
    }

    public void update(float delta) {
        synchronized (syncToken) {
            collisionWorld.stepSimulation(delta, 5);
        }
        numberOfTicks++;
        if (camera != null) {
            if (debug) {
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                debugDrawer.getShapeRenderer().setProjectionMatrix(camera.combined);
                debugDrawer.setDebugMode(DebugDrawModes.DBG_DrawAabb);
                debugDrawer.begin(camera);
                collisionWorld.debugDrawWorld();
                debugDrawer.end();
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
            }

            playerGhostObject.getWorldTransform().getTranslation(tmp);
            double headBob = 0.0;
            float mov = tmp.dst(previousPosition);
            if (!playerInWater && (mov > 0.01f || mov < -0.01f)) {
                headBob = 0.05 * MathUtils.sin((float) (numberOfTicks * 0.5 * MathUtils.PI / 7));
            }
            tmp2.set(tmp.x, (float) (tmp.y + headBob + 0.3f), tmp.z);
            camera.position.set(tmp2);
            previousPosition.set(tmp);
        }


        for (Entry<btRigidBody, CollisionObject> object : entityRigidBodies.entrySet()) {
            object.getKey().getWorldTransform().getTranslation(tmp);
            object.getValue().setPosition(tmp);
            object.getKey().applyCentralImpulse(object.getValue().getVelocity());
            object.getValue().resetVelocity();
        }
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    //TODO: rayPick

    static class WorldInternalTickCallback extends InternalTickCallback {
        WorldInternalTickCallback(btDynamicsWorld dynamicsWorld) {
            super(dynamicsWorld, true);
        }

        @Override
        public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep) {
        }
    }
}
