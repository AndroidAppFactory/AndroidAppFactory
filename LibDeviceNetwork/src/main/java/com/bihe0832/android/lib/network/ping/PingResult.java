package com.bihe0832.android.lib.network.ping;


import com.bihe0832.android.lib.log.ZLog;

public class PingResult {
    public String addr;
    public int avgDelay;
    public int min = Integer.MAX_VALUE;
    public int max = Integer.MIN_VALUE;
    public double lossRate;  // 百分比(i.e. lossRate %)
    public double highDelayRate; // 百分比(i.e. highDelayRate %)
    public int numSent = 0;
    public int numReceived = 0;
    public int numHighDelay = 0;
    public int sumDelay = 0;
    public boolean isPingRouter = false;
    public String pingResultDesc = "";
    // 1表示良好，2表示差，负数表示ping的不是路由器
    public int pingResultFlag = -1;
    private final String STATUS_ROUTER_WEEK = "路由器不稳定";
    private final String STATUS_ROUTER_NORMAL = "路由器稳定";

    public PingResult(String _addr, int _avgDelay, double _lossRate, boolean _isPingRouter) {
        if(_addr == null || _addr.trim().equals("")) {
            ZLog.d("PingResult的构造函数传入了空IP地址");
        } else {
            this.addr = _addr.trim();
        }
        this.avgDelay = _avgDelay;
        this.lossRate = _lossRate;
        this.isPingRouter = _isPingRouter;
    }

    public PingResult(String _addr, int _avgDelay, double _lossRate, double _highDelayRate, boolean _isPingRouter) {
        if(_addr == null || _addr.trim().equals("")) {
            ZLog.d("PingResult的构造函数传入了空IP地址");
        } else {
            this.addr = _addr.trim();
        }
        this.avgDelay = _avgDelay;
        this.lossRate = _lossRate;
        this.highDelayRate = _highDelayRate;
        this.isPingRouter = _isPingRouter;
    }

    /*
     * 注意freshStat不包括构造函数传入的参数
     * 计算均值、高延迟率和丢失率，并生成路由状态描述
     */
    public void freshStat(PingThreshold pingThreshold) {
        this.avgDelay = this.sumDelay / this.numSent;
        this.highDelayRate = 100 * (this.numHighDelay / (double) this.numSent);
        this.lossRate = 100 * ((this.numSent - this.numReceived) / (double) this.numSent);
        if(this.isPingRouter) {
            this.pingResultDesc = getPingResultDesc(pingThreshold);
        }
    }

    private String getPingResultDesc(PingThreshold pingThreshold) {
        if(this.avgDelay >= pingThreshold.getAvgMax() ||
                PingUtil.compareDouble(this.highDelayRate, pingThreshold.getHighDelayRateMax()) >= 0 ||
                PingUtil.compareDouble(this.lossRate, pingThreshold.getLossRateMax()) >= 0) {
            this.pingResultFlag = 2;
            return this.STATUS_ROUTER_WEEK;
        } else {
            this.pingResultFlag = 1;
            return this.STATUS_ROUTER_NORMAL;
        }
    }

}
