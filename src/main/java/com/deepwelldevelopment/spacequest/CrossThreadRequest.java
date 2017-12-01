package com.deepwelldevelopment.spacequest;

public class CrossThreadRequest {

    public static final int BLOCK_INIT_REQUEST = 0;

    int sendingThread;
    int destinationThread;
    int requestType;
    ICrossThreadObject requestObject;

    public CrossThreadRequest(int sendingThread, int destinationThread, int requestType, ICrossThreadObject requestObject) {
        this.sendingThread = sendingThread;
        this.destinationThread = destinationThread;
        this.requestType = requestType;
        this.requestObject = requestObject;
    }

    public int getSendingThread() {
        return sendingThread;
    }

    public int getDestinationThread() {
        return destinationThread;
    }

    public int getRequestType() {
        return requestType;
    }

    public ICrossThreadObject getRequestObject() {
        return requestObject;
    }

    public void complete() {
        requestObject.completeRequest();
    }
}
