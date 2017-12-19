package com.deepwelldevelopment.spacequest;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.renderer.ResourceManager;
import com.deepwelldevelopment.spacequest.renderer.Texture;
import com.deepwelldevelopment.spacequest.util.GLManager;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SpaceQuest {

    public static SpaceQuest INSTANCE;

    private Matrix4f projMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f modelMatrix = new Matrix4f();
    private Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f invViewMatrix = new Matrix4f();
    private Matrix4f invViewProjMatrix = new Matrix4f();
    private Matrix4f mvp = new Matrix4f();
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private FrustumIntersection frustumIntersection = new FrustumIntersection();

    GLFWKeyCallback keyCallback;
    GLFWCursorPosCallback cpCallback;
    GLFWMouseButtonCallback mbCallback;

    long lastTime;
    long window;
    long lastChunkTime;

    Vector3f position = new Vector3f(0.0f, 32.0f, 0.0f);
    float horizontalAngle = 3.14f;
    float verticalAngle = 0.0f;
    float fov = 45.0f;
    float speed = 7.5f;
    float mouseSpeed = 0.5f;

    int width = 1024;
    int height = 768;

    private boolean windowed = true;
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;
    private float mouseX = 0.0f;
    private float mouseY = 0.0f;

    public ArrayList<Texture> textures;

    Block b;

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
//        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
//            public void invoke(long window, double xpos, double ypos) {
//                float normX = (float) ((xpos - width/2.0) / width * 2.0);
//                float normY = (float) ((ypos - height/2.0) / height * 2.0);
//                SpaceQuest.this.mouseX = Math.max(-width/2.0f, Math.min(width/2.0f, normX));
//                SpaceQuest.this.mouseY = Math.max(-height/2.0f, Math.min(height/2.0f, normY));
//            }
//        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
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

        createCapabilities(); //DON'T EVER FORGET THIS CALL; FUCK YOU C++ FOR NOT NEEDING IT YOU CONFOUNDED ME FOR 20 MINUTES
        glClearColor(0.0f, 0.0f, 0.4f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
//        glEnable(GL_CULL_FACE);

        new GLManager(16);
        ResourceManager.INSTANCE.loadTextures();

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vShader = createShader("vertex.vert", GL_VERTEX_SHADER);
        int fShader = createShader("fragment.frag", GL_FRAGMENT_SHADER);
        int program = createShaderProgram(vShader, fShader);

        int matrixID = glGetUniformLocation(program, "MVP");

        projMatrix.perspective((float) Math.toRadians(45.0f), 4.0f / 3.0f, 0.1f, 100f);
        viewMatrix.lookAt(0, 32, 0, 0, 0, 0, 0, 1, 0);
        modelMatrix.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
        mvp = projMatrix.mul(viewMatrix.mul(modelMatrix));

        textures = new ArrayList<>();

        World world = new World();
        lastChunkTime = System.nanoTime();

        while (!glfwWindowShouldClose(window)) {
            //handle cross-thread requests
//            ThreadManager.INSTANCE.getRequestsForThread(ThreadManager.RENDER_THREAD).forEach(CrossThreadRequest::complete);
//            System.out.println("Render thread completed cross threading");
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) / 1E9f;
            lastTime = thisTime;
//            System.out.println(dt);
            world.initBlocks();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(program);
            glUniformMatrix4fv(matrixID, false, mvp.get(matrixBuffer));

            world.render();

            glfwSwapBuffers(window);
            glfwPollEvents();

            updateControls(dt);
        }
        ThreadManager.INSTANCE.setRunning(false);
        ThreadManager.INSTANCE.finish();
        world.cleanup();
        ResourceManager.INSTANCE.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        glfwTerminate();
    }

    void updateControls(float deltaTime) {
//        DoubleBuffer xBuf = BufferUtils.createDoubleBuffer(1);
//        DoubleBuffer yBuf = BufferUtils.createDoubleBuffer(1);
//        glfwGetCursorPos(window, xBuf, yBuf);
//        mouseX = (float) xBuf.get(0);
//        mouseY = (float) yBuf.get(0);
//        glfwSetCursorPos(window, width/2, height/2);
//        horizontalAngle += mouseSpeed * deltaTime * (width/2 - mouseX);
//        verticalAngle += mouseSpeed * deltaTime * (height/2 - mouseY);

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

        Vector3f direction = new Vector3f((float) (cos(verticalAngle) * Math.sin(horizontalAngle)), (float ) sin(verticalAngle), (float) (cos(verticalAngle) * cos(horizontalAngle)));
        Vector3f forward = new Vector3f((float) (cos(verticalAngle) * Math.sin(horizontalAngle)), 0.0f, (float) (cos(verticalAngle) * cos(horizontalAngle)));
        Vector3f right = new Vector3f((float) sin(horizontalAngle - PI/2.0f), 0.0f, (float) cos(verticalAngle + PI/2.0f));
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        if (keyDown[GLFW_KEY_W]){
            position = add(position, forward.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_S]){
            position = subtract(position, forward.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_D]){
            position = add(position, right.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_A]){
            position = subtract(position, right.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_SPACE]) {
            position = add(position, up.mul(deltaTime * speed));
        }
        if (keyDown[GLFW_KEY_LEFT_SHIFT]) {
            position = subtract(position, up.mul(deltaTime * speed));
        }

        projMatrix = new Matrix4f().perspective((float) Math.toRadians(fov), 4.0f / 3.0f, 0.1f, 100f);
        viewMatrix = new Matrix4f().lookAt(position, add(position, direction), up);
        modelMatrix = new Matrix4f().set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
        mvp = projMatrix.mul(viewMatrix.mul(modelMatrix));
        frustumIntersection.set(mvp);
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
