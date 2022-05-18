package com.FFX.bluedoorlock.errorprocess;

public class ErrorProcess {
    private static int LIMIT = 5;
    private int eCount;

    public ErrorProcess() {
        this.eCount = 0;
    }

    public void passwordError() {
        eCount++;
    }

    public void resetError() {
        eCount = 0;
    }

    public boolean isRetrieable() {
        return eCount < 5;
    }
}
