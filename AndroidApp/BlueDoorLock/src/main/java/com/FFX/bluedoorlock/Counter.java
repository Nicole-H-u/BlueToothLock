package com.FFX.bluedoorlock;

public class Counter {
    private long count = 0;

    public Counter(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void resetCount(long count) {
        this.count = 0;
    }
}
