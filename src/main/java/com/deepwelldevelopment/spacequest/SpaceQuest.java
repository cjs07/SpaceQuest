package com.deepwelldevelopment.spacequest;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.renderer.ResourceManager;
import com.deepwelldevelopment.spacequest.renderer.Texture;
import com.deepwelldevelopment.spacequest.util.GLManager;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShader;
import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShaderProgram;
import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SpaceQuest {

    public static final int MAX_CHUNKS = 16;

    public static SpaceQuest INSTANCE;

    private boolean resetFrameBuffer;

    public Matrix4f projMatrix = new Matrix4f();
    Matrix4f invProjMatrix = new Matrix4f();
    public Matrix4f viewMatrix = new Matrix4f();
    public Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f invViewMatrix = new Matrix4f();
    private Matrix4f modelMatrix = new Matrix4f();
    public Matrix4f invViewProjMatrix = new Matrix4f();
    private Matrix4f mvp = new Matrix4f();
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    Vector3f tmpVector = new Vector3f();
    public Vector3f cameraPosition = new Vector3f(0.0f, 32.0f, 0.0f);
    public Vector3f cameraLookAt = new Vector3f(0.0f, 32.0f, 0.0f);
    Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    private FrustumIntersection frustumIntersection = new FrustumIntersection();

    GLFWKeyCallback keyCallback;
    GLFWCursorPosCallback cpCallback;
    GLFWMouseButtonCallback mbCallback;

    long lastTime;
    long window;
    long lastChunkTime;

    public Vector3f position = new Vector3f(0.0f, 32.0f, 0.0f);
    public float horizontalAngle = 3.14f;
    public float verticalAngle = 0.0f;
    float fov = 45.0f;
    float speed = 7.5f;
    float mouseSpeed = 0.25f;

    public int width = 1024;
    public int height = 768;

    private boolean windowed = true;
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;
    private float mouseX = 0.0f;
    private float mouseY = 0.0f;

    public ArrayList<Texture> textures;

    World world;

    void run() throws IOException {
        INSTANCE = this;
        new ThreadManager();
        new ResourceManager();
        ThreadManager.INSTANCE.attatchRenderThread(Thread.currentThread());
        if (!glfwInit()) {
            System.err.println("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "Space Quest", NULL, NULL);
        if (window == NULL) {
            System.err.println("Failed to open Window. Is OpenGL 3.3 supported on your system?");
            return;
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_UNKNOWN)
                    return;
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    keyDown[key] = true;
                } else {
                    keyDown[key] = false;
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                SpaceQuest.this.mouseX = (float) xpos;
                SpaceQuest.this.mouseY = (float) ypos;
            }
        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                world.playerClicked(button, true);
                if (button == GLFW_MOUSE_BUTTON_LEFT) {
                    if (action == GLFW_PRESS) {
                        leftMouseDown = true;
                    }
                    else if (action == GLFW_RELEASE)
                        leftMouseDown = false;
                } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                    if (action == GLFW_PRESS) {
                        rightMouseDown = true;
                    } else if (action == GLFW_RELEASE)
                        rightMouseDown = false;
                }
            }
        });

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

        createCapabilities(); //DON'T EVER FORGET THIS CALL; FUCK YOU C++ FOR NOT NEEDING IT YOU CONFOUNDED ME FOR 20 MINUTES
        glClearColor(0.0f, 0.0f, 0.4f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
//        glEnable(GL_CULL_FACE);

        new GLManager(MAX_CHUNKS);
        ResourceManager.INSTANCE.loadTextures();

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int rayBuffer = glGenBuffers();
        int rayUVBUffer = glGenBuffers();

        int vShader = createShader("vertex.vert", GL_VERTEX_SHADER);
        int fShader = createShader("fragment.frag", GL_FRAGMENT_SHADER);
        int program = createShaderProgram(vShader, fShader);

        int matrixID = glGetUniformLocation(program, "MVP");

        int crosshairVShader = createShader("crosshair.vert", GL_VERTEX_SHADER);
        int crosshairFShader = createShader("crosshair.frag", GL_FRAGMENT_SHADER);
        int crosshairProgram = createShaderProgram(crosshairVShader, crosshairFShader);
        int crosshairVertexBuffer = glGenBuffers();
        FloatBuffer crosshairVertices = BufferUtils.createFloatBuffer(8);
        crosshairVertices.put(width/2 - 10).put(height/2);
        crosshairVertices.put(width/2 + 10).put(height/2);
        crosshairVertices.put(width/2).put(height/2 - 10);
        crosshairVertices.put(width/2).put(height/2 + 10);
        crosshairVertices.flip();
        glBindBuffer(GL_ARRAY_BUFFER, crosshairVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, crosshairVertices, GL_STATIC_DRAW);
        int screenBuffer = glGenBuffers();
        FloatBuffer screen = BufferUtils.createFloatBuffer(2);
        screen.put(width);
        screen.put(height);
        glBindBuffer(GL_ARRAY_BUFFER, screenBuffer);
        glBufferData(GL_ARRAY_BUFFER, screenBuffer, GL_STATIC_DRAW);
        int screenID = glGetUniformLocation(crosshairProgram, "screen");

        int rayVShader = createShader("ray.vert", GL_VERTEX_SHADER);
        int rayFShader = createShader("ray.frag", GL_FRAGMENT_SHADER);
        int rayProgram = createShaderProgram(rayVShader, rayFShader);

        int rayMatrixID = glGetUniformLocation(program, "MVP");

        projMatrix.perspective((float) toRadians(45.0f), (float) width / height, 0.1f, 100f);
        viewMatrix.lookAt(0, 32, 0, 0, 0, 0, 0, 1, 0);
        modelMatrix.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
        mvp = projMatrix.mul(viewMatrix.mul(modelMatrix));
//        projMatrix.invertPerspectiveView(viewMatrix, invViewProjMatrix);

        textures = new ArrayList<>();

        world = new World();
        lastChunkTime = System.nanoTime();

        glfwSetCursorPos(window, width/2, height/2);

        Vector3f direction = new Vector3f();
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        Vector2f nearFar = new Vector2f();

        while (!glfwWindowShouldClose(window)) {
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;
            world.initBlocks();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(program);
            glUniformMatrix4fv(matrixID, false, mvp.get(matrixBuffer));

            glUseProgram(crosshairProgram);
            glEnableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, crosshairVertexBuffer);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
            glUniform2fv(screenID, screen);
            glDrawArrays(GL_LINES, 0, crosshairVertices.capacity());

            glDisableVertexAttribArray(0);

            glUseProgram(program);
            world.render();

            Block selectedBlock = null;
            float closesetDistance = Float.POSITIVE_INFINITY;
            viewMatrix.positiveZ(direction).negate();
            for (Block b : world.getAllBlocks()) {
                if (b != null) {
                    b.setSelected(true);
                    min.set(b.x, b.y, b.z);
                    max.set(b.x + 1, b.y + 1, b.z + 1);
                    if (Intersectionf.intersectRayAab(cameraPosition, direction, min, max, nearFar) && nearFar.x < closesetDistance) {
                        closesetDistance = nearFar.x;
                        selectedBlock = b;
                    }
                }
            }
            if (selectedBlock != null) {
                selectedBlock.setSelected(true);
            }

            drawRay(rayProgram, rayMatrixID, rayBuffer);

            glfwSwapBuffers(window);
            glfwPollEvents();

            updateControls(dt);
        }
        ThreadManager.INSTANCE.setRunning(false);
        ThreadManager.INSTANCE.finish();
        world.cleanup();
        ResourceManager.INSTANCE.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(rayBuffer);
        glDeleteBuffers(rayUVBUffer);
        glDeleteProgram(program);
        glfwTerminate();
    }

    void drawRay(int program, int matrixID, int buffer) {
        FloatBuffer points = BufferUtils.createFloatBuffer(3 * 25);
//        Vector3f direction = new Vector3f((float) (cos(verticalAngle) * sin(horizontalAngle)), (float) sin(verticalAngle), (float) (cos(verticalAngle) * cos(horizontalAngle)));
//        Vector3f direction = new Vector3f();
//        invProjMatrix.transformProject(direction);
        Vector3f direction = new Vector3f();
        viewProjMatrix.positiveZ(direction);
        direction.negate();
        float currX = cameraPosition.x;
        float currY = cameraPosition.y;
        float currZ = cameraPosition.z;

        for(int i = 0; i < 25; i++) {
            points.put(currX).put(currY).put(currZ);
            currX += direction.x;
            currY += direction.y;
            currZ += direction.z;
        }

        points.flip();

        glUseProgram(program);
        glUniformMatrix4fv(matrixID, false, mvp.get(matrixBuffer));
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, points, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_LINE_STRIP, 0, points.capacity());
        glDisableVertexAttribArray(0);
    }

    void updateControls(float deltaTime) {
        horizontalAngle += mouseSpeed * deltaTime * ((float)width/2 - mouseX);
        verticalAngle += mouseSpeed * deltaTime * ((float)height/2 - mouseY);
        //don't allow player to have full vertical motion, includes slight compensation, look inversion occurs at exactly -pi/2 and pi/2
        if (verticalAngle < -PI/2)
            verticalAngle = (float) (-PI/2) + 0.0000001f;
        if (verticalAngle > PI/2)
            verticalAngle = (float) (PI/2) - 0.0000001f;

        glfwSetCursorPos(window, width/2, height/2);

        if (keyDown[GLFW_KEY_Q]){
            horizontalAngle += mouseSpeed * deltaTime;
        }

        if (keyDown[GLFW_KEY_E]){
            horizontalAngle -= mouseSpeed * deltaTime;
        }

        if (keyDown[GLFW_KEY_R]){
            verticalAngle += mouseSpeed * deltaTime;
        }

        if (keyDown[GLFW_KEY_F]){
            verticalAngle -= mouseSpeed * deltaTime;
        }

//        Vector3f direction = new Vector3f((float) (cos(verticalAngle) * Math.sin(horizontalAngle)), (float ) sin(verticalAngle), (float) (cos(verticalAngle) * cos(horizontalAngle)));
//        Vector3f right = new Vector3f((float) sin(horizontalAngle - PI/2.0f), 0.0f, (float) cos(horizontalAngle - PI/2.0f));
//        Vector3f up = new Vector3f(right).cross(direction);

        Vector3f direction = new Vector3f((float) (cos(verticalAngle) * sin(horizontalAngle)), (float ) sin(verticalAngle), (float) (cos(verticalAngle) * cos(horizontalAngle)));
        Vector3f forward = new Vector3f((float) (cos(verticalAngle) * sin(horizontalAngle)), 0.0f, (float) (cos(verticalAngle) * cos(horizontalAngle)));
        cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f right = new Vector3f(forward).cross(cameraUp).normalize();
//        System.out.println(verticalAngle + ", " + direction.y);

        if (keyDown[GLFW_KEY_W]){
            cameraPosition.add(forward.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_S]){
            cameraPosition.sub(forward.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_D]){
            cameraPosition.add(right.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_A]){
            cameraPosition.sub(right.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_SPACE]) {
            cameraPosition.add(cameraUp.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_LEFT_SHIFT]) {
            cameraPosition.sub(cameraUp.mul(deltaTime * speed));
        }

        cameraLookAt = new Vector3f(add(cameraPosition, direction));

        projMatrix = new Matrix4f().perspective((float) toRadians(fov), 4.0f / 3.0f, 0.1f, 100f);
        projMatrix.invert(invProjMatrix);
        viewMatrix.setLookAt(cameraPosition, cameraLookAt, cameraUp);
        viewMatrix.invert(invViewMatrix);
        viewProjMatrix.set(projMatrix).mul(viewMatrix);
        viewProjMatrix.invert(invViewProjMatrix);
        mvp = viewProjMatrix;
        frustumIntersection.set(viewProjMatrix);
//        viewMatrix = new Matrix4f().lookAt(cameraPosition, add(cameraPosition, direction), cameraUp);
//        viewMatrix = projMatrix.lookAt(cameraPosition, add(cameraPosition, direction), cameraUp);
//        System.out.println(viewMatrix);
//        modelMatrix = new Matrix4f().set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
//        mvp = projMatrix.mul(viewMatrix.mul(modelMatrix));
//        mvp = viewMatrix.mul(modelMatrix);
//        frustumIntersection.set(mvp);

//        viewMatrix.setLookAt(cameraPosition, add(cameraPosition, direction), cameraUp);
//        mvp = viewMatrix;
//        frustumIntersection.set(mvp);
//
//        if (resetFrameBuffer) {
//            projMatrix.setPerspective((float) toRadians(45.0f), (float) width / height, 0.1f, 100f);
//            resetFrameBuffer = false;
//        }

        projMatrix.invertPerspectiveView(viewMatrix, invViewProjMatrix);
    }

    Vector3f add(Vector3f vec1, Vector3f vec2) {
        float x1 = vec1.x();
        float y1 = vec1.y();
        float z1 = vec1.z();
        float x2 = vec2.x();
        float y2 = vec2.y();
        float z2 = vec2.z();
        return new Vector3f(x1 + x2, y1 + y2, z1 + z2);
    }

    Vector3f subtract(Vector3f vec1, Vector3f vec2) {
        float x1 = vec1.x();
        float y1 = vec1.y();
        float z1 = vec1.z();
        float x2 = vec2.x();
        float y2 = vec2.y();
        float z2 = vec2.z();
        return new Vector3f(x1 - x2, y1 - y2, z1 - z2);
    }

    public static void main(String[] args) throws IOException {
        new SpaceQuest().run();
    }
}
