package com.hansight.sae.performance.constant;

public class EngineConfigProps {
    private boolean multiThread;
    private boolean metricOn;
    private int metricPeriod;

    public boolean isMultiThread() {
        return multiThread;
    }

    public void setMultiThread(boolean multiThread) {
        this.multiThread = multiThread;
    }

    public boolean isMetricOn() {
        return metricOn;
    }

    public void setMetricOn(boolean metricOn) {
        this.metricOn = metricOn;
    }

    public int getMetricPeriod() {
        return metricPeriod;
    }

    public void setMetricPeriod(int metricPeriod) {
        this.metricPeriod = metricPeriod;
    }

    @Override
    public String toString() {
        return "EngineConfigProps{" +
                "multiThread=" + multiThread +
                ", metricOn=" + metricOn +
                ", metricPeriod=" + metricPeriod +
                '}';
    }
}
