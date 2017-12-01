package com.deepwelldevelopment.spacequest;

import java.util.ArrayList;

public class ThreadManager {

    public static final int RENDER_THREAD = 0;
    public static final int GENERATION_THREAD = 1;

    public static ThreadManager INSTANCE;

    Thread renderThread;
    Thread generationThread;

    boolean hasCrossThreadRequests;
    volatile boolean running;

    final ArrayList<CrossThreadRequest> activeRequests;

    public ThreadManager() {
        if (INSTANCE != null) {
            System.out.println("[CRITiCAL ERROR] Thread manager is attempting to create a new instance, however there is already an active instance." +
                    "This WILL have unpredictable results and WILL result in game and save file corruption. This error is most likely due to a modification to the game code. " +
                    "Unless you or a trusted source has made a modification to your game, you should quit the game and reinstall a fresh copy to fix the error. If the " +
                    "error persists of reinstalling please report this a critical error to www.deepwelldevelopment.com/spacequest/report");
        }

        activeRequests = new ArrayList<>();
        running = true;

        INSTANCE = this;
    }

    public void finish() {
        synchronized (activeRequests) {
            activeRequests.notifyAll();
        }
    }


    public void attatchRenderThread(Thread thread) {
        this.renderThread = thread;
    }

    public void attachGenerationThread(Thread thread) {
        this.generationThread = thread;
    }

    public void setRunning(boolean running) {
        this.running = running;
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

    public boolean isRunning() {
        return running;
    }

    public ArrayList<CrossThreadRequest> getRequestsForThread(int thread) {
        synchronized (activeRequests) {
            ArrayList<CrossThreadRequest> threadRequests = new ArrayList<>();
            for (CrossThreadRequest request : activeRequests) {
                if (thread == request.getDestinationThread()) {
                    threadRequests.add(request);
                }
            }
            activeRequests.removeAll(threadRequests);
            activeRequests.notifyAll();
            return threadRequests;
        }
    }

    public void openRequest(CrossThreadRequest request) {
        synchronized (activeRequests) {
            activeRequests.add(request);
            try {
                activeRequests.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
