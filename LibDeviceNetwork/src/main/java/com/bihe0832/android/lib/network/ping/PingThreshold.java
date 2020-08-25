package com.bihe0832.android.lib.network.ping;

/**
 * Author: waynescliu
 * Date: 2020/8/13
 * Description:用于判定ping的结果
 */
public class PingThreshold {
    private int highDelay;
    private int avgMax;
    private float highDelayRateMax;
    private float lossRateMax;

    public int getHighDelay() {
        return highDelay;
    }

    public void setHighDelay(int highDelay) {
        this.highDelay = highDelay;
    }

    public int getAvgMax() {
        return avgMax;
    }

    public void setAvgMax(int avgMax) {
        this.avgMax = avgMax;
    }

    public float getHighDelayRateMax() {
        return highDelayRateMax;
    }

    public void setHighDelayRateMax(float highDelayRateMax) {
        this.highDelayRateMax = highDelayRateMax;
    }

    public float getLossRateMax() {
        return lossRateMax;
    }

    public void setLossRateMax(float lossRateMax) {
        this.lossRateMax = lossRateMax;
    }
}
