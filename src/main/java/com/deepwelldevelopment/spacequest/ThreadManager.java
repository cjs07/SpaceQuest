package com.deepwelldevelopment.spacequest;

import java.util.ArrayList;

public class ThreadManager {

    public static final int RENDER_THREAD = 0;
    public static final int GENERATION_THREAD = 1;

    public static ThreadManager INSTANCE;

    /**
     * The thread responsible for rendering to the screen and managing openGL bindings
     */
    Thread renderThread;

    /***
     * Thread responsible for managing world generation
     */
    Thread generationThread;

    boolean hasCrossThreadRequests;

    final ArrayList<CrossThreadRequest> activeRequests;

    public ThreadManager() {
        if (INSTANCE != null) {
            System.err.println("A new Thread Manager is being created with an active instance already in memory. This WILL cause unpredictable behavior, and will likely cause  " +
                    "corruption. This error is likely a result of modification to the games source code. Unless you or a trusted source performed these modifications, you should " +
                    "download a new copy of the game.");
        }

        activeRequests = new ArrayList<>();

        INSTANCE = this;
    }

    public void attachRenderThread(Thread thread) {
        renderThread = thread;
        renderThread.setName("renderThread");
    }

    public void attachGenerationThread(Thread thread) {
        generationThread = thread;
        generationThread.setName("generationThread");
    }

    public Thread getRenderThread() {
        return renderThread;
    }

    public Thread getGenerationThread() {
        return generationThread;
    }

    public boolean hasCrossThreadRequests() {
        return hasCrossThreadRequests;
    }

    public ArrayList<CrossThreadRequest> getRequestsForThread(int thread) {
        synchronized (activeRequests) {
            ArrayList<CrossThreadRequest> threadRequests = new ArrayList<>();
            for (CrossThreadRequest request : activeRequests) {
                if (thread == request.getDestinationThread()) {
                    threadRequests.add(request);
                }
            }
            return threadRequests;
        }
    }

    public void openRequest(CrossThreadRequest request) {
        synchronized (activeRequests) {
            activeRequests.add(request);
        }
    }
}
