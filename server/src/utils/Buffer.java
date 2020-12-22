package utils;

import java.util.concurrent.LinkedBlockingDeque;

public class Buffer<T> extends LinkedBlockingDeque<T> {

    public Buffer(int capacity) {
        super(capacity);
    }

    public synchronized boolean add(T toAdd) {
        if (this.remainingCapacity() > 0) {
            super.add(toAdd);
            return true;
        }
        return false;
    }

    public synchronized boolean isFull() {
        return this.remainingCapacity() == 0;
    }

}
