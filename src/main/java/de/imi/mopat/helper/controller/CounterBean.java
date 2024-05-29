package de.imi.mopat.helper.controller;

/**
 * Helper class to realize a loop counter with Thymeleaf.
 */
public class CounterBean {

    private int count;

    /**
     * Initialize with 0.
     */
    public CounterBean() {
        count = 0;
    }

    /**
     * Initialize with custom value.
     *
     * @param init value
     */
    public CounterBean(final int init) {
        count = init;
    }

    /**
     * Get current saved counter value.
     *
     * @return counter value
     */
    public int get() {
        return count;
    }

    /**
     * Reset Counter to 0.
     */
    public void clear() {
        count = 0;
    }

    /**
     * Set counter to value.
     *
     * @param i to set the counter to
     */
    public void set(final int i) {
        count = i;
    }

    /**
     * Increment counter by 1.
     */
    public void increment() {
        count++;
    }

    /**
     * Increment counter by 1 and return.
     *
     * @return counter
     */
    public int incrementAndGet() {
        return ++count;
    }

    /**
     * Decrement counter by 1.
     */
    public void decrement() {
        count--;
    }

    /**
     * Decrement counter by 1 and return.
     *
     * @return counter
     */
    public int decrementAndGet() {
        return count--;
    }

    /**
     * Return counter as String.
     *
     * @return count as string
     */
    public String toString() {
        return "" + count;
    }
}
