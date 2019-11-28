package com.hansight.sae.performance.constant;

import java.util.List;

public class PerformanceTestProps {
    private List<String> epl;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getEpl() {
        return epl;
    }

    public void setEpl(List<String> epl) {
        this.epl = epl;
    }

    @Override
    public String toString() {
        return "PerformanceTestProps{" +
                "count=" + count +
                "epl=" + epl +
                '}';
    }
}
