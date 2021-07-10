package directed.events;

import directed.graph.Graph;

public final class StatResult {

    public final Number currentTime;
    public final int numPhotons;
    public final Number maxTime;
    public final int maxCount;
    public final double eps;
    public final Number saturationTime;
    public final long saturationClock;
    public final int[] edgeNum;
    public final Graph g;

    public StatResult(Number currentTime, int numPhotons,
                      Number maxTime, int maxCount, double eps, Number saturationTime, long saturationClock, int[] edgeNum,
                      Graph g) {
        this.currentTime = currentTime;
        this.numPhotons = numPhotons;
        this.maxTime = maxTime;
        this.maxCount = maxCount;
        this.eps = eps;
        this.saturationTime = saturationTime;
        this.saturationClock = saturationClock;
        this.edgeNum = edgeNum;
        this.g = g;
    }
}
