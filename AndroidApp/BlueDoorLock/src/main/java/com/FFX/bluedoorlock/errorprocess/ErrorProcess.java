package com.FFX.bluedoorlock.errorprocess;

public class ErrorProcess {
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
}
