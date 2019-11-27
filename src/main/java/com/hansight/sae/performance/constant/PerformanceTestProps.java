package com.hansight.sae.performance.constant;

public class PerformanceTestProps {
    private String epl;
    private int count;

    public String getEpl() {
        return epl;
    }

    public void setEpl(String epl) {
        this.epl = epl;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "PerformanceTestProps{" +
                "epl='" + epl + '\'' +
                ", count=" + count +
                '}';
    }
}
