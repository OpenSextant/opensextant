/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.extraction;

/**
 *
 * @author ubaldino
 */
public class ExtractionMetrics {

    private String name = null;
    private int callCount = 0;
    /**
     * total time spent for this metric in milliseconds
     */
    private long totalTime = 0;

    /**
     * A named metric
     */
    public ExtractionMetrics(String nm) {
        this.name = nm;
    }

    @Override
    public String toString() {
        return "Metric " + this.name + " Calls:" + this.getCallCount()
                + " Average time(ms):" + this.getAverageTime() 
                + " with Total time(ms):" + this.getTotalTime();
    }

    /**
     * avg time spent for this metric in milliseconds
     */
    public int getAverageTime() {
        return (int) (totalTime / callCount);
    }

    public void addTime(long time) {
        totalTime += time;
        ++callCount;
    }

    public void addTime(long time, int calls) {
        totalTime += time;
        callCount += calls;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public int getCallCount() {
        return callCount;
    }
}
