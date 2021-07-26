package directed.events;

import directed.graph.Graph;
import directed.util.Arithmetic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Schedule {

    private final Graph g;
    private final Arithmetic a;
    private final double eps;
    private final TreeMap<Number, List<SmallEntry>> list = new TreeMap<>();

    private final Object lock = new Object();

    private Number currentTime;
    private int maxCount;
    private Number maxTime;
    private Number addDelay;
    private boolean saturated = false;
    private Number saturationTime = null;
    private long saturationClock;
    private long clockStart;

    public Schedule(Graph g, Arithmetic a, double eps) {
        this.g = g;
        this.a = a;
        this.eps = eps;

        this.currentTime = a.zero();
        this.maxTime = a.zero();
        this.maxCount = 0;
        this.addDelay = a.zero();
    }

    private void schedule(Number time, int edge) {
        SmallEntry newEntry = newEvent(edge);
        ScheduleUtil.schedule(a, list, time, newEntry);
    }

    public void firstPhotons() {
        synchronized (lock) {
            clockStart = System.currentTimeMillis();
            for (int i = 0; i < g.getEdgeNum(); i++) {
                Boolean startForward = g.isStartForward(i);
                if (startForward != null) {
                    double len = g.getEdgeLength(i).doubleValue();
                    double half = len / 2.0;
                    Number t0 = a.evaluate(half);
                    schedule(t0, i);
                    double eps = len - half;
                    addDelay = a.evaluate(-eps);
                }
            }
        }
    }

    private static SmallEntry newEvent(int edge) {
        return new SmallEntry(edge);
    }

    public boolean next() {
        synchronized (lock) {
            if (list.isEmpty())
                return false;
            Number first = list.firstKey();
            List<SmallEntry> e = list.remove(first);
            boolean[][] present = new boolean[g.getVertexNum()][g.getEdgeNum()];
            currentTime = first;
            for (SmallEntry entry : e) {
                int vertex = g.getEdgeSide(entry.edge, true);
                int[] out = g.goingOut(vertex);
                for (int edge : out) {
                    present[vertex][edge] = true;
                }
            }
            for (int vertex = 0; vertex < g.getVertexNum(); vertex++) {
                for (int edge = 0; edge < g.getEdgeNum(); edge++) {
                    if (!present[vertex][edge])
                        continue;
                    Number len = g.getEdgeLength(edge);
                    Number nextTime = a.add(currentTime, len);
                    schedule(nextTime, edge);
                }
            }
            int count = 0;
            for (List<SmallEntry> entries : list.values()) {
                count += entries.size();
            }
            if (count > maxCount) {
                maxCount = count;
                maxTime = currentTime;
                boolean sat = saturation2();
                if (sat) {
                    if (!saturated) {
                        saturationTime = currentTime;
                        saturationClock = System.currentTimeMillis();
                    }
                } else {
                    saturationTime = null;
                }
                saturated = sat;
            }
            return true;
        }
    }

    public StatResult getStat() {
        synchronized (lock) {
            long saturationClock = saturationTime == null ? 0 : this.saturationClock - clockStart;
            Number saturationTime = this.saturationTime == null ? null : a.add(this.saturationTime, addDelay);
            return ScheduleUtil.getStat(
                g, list, a.add(currentTime, addDelay), a.add(maxTime, addDelay), maxCount, eps, saturationTime, saturationClock
            );
        }
    }

    public ShowState showPhotons(double time) {
        return ScheduleUtil.showPhotons(g, list, time, this);
    }

    public boolean saturation() {
        Map<Integer, TreeSet<Double>> byEdge = new TreeMap<>();
        double now = currentTime.doubleValue();
        for (Map.Entry<Number, List<SmallEntry>> entry : list.entrySet()) {
            // Время планируемого прихода в конечную вершину:
            double time = entry.getKey().doubleValue();
            double delta = time - now;
            List<SmallEntry> entries = entry.getValue();
            for (SmallEntry se : entries) {
                int edge = se.edge;
                double edgeLen = g.getEdgeLength(edge).doubleValue();
                double coord = edgeLen - delta;
                byEdge.computeIfAbsent(edge, k -> new TreeSet<>()).add(coord);
            }
        }
        for (TreeSet<Double> onEdge : byEdge.values()) {
            Double prev = null;
            for (Double x : onEdge) {
                if (prev != null) {
                    double delta = x.doubleValue() - prev.doubleValue();
                    if (delta > eps * 2)
                        return false;
                }
                prev = x;
            }
        }
        for (int vertex = 0; vertex < g.getVertexNum(); vertex++) {
            int[] out = g.allOut(vertex);
            for (int i = 1; i < out.length; i++) {
                int edge1 = out[i];
                TreeSet<Double> onEdge1 = byEdge.get(edge1);
                if (onEdge1 == null)
                    continue;
                double dist1;
                if (g.getEdgeSide(edge1, true) == vertex) {
                    double edgeLen1 = g.getEdgeLength(edge1).doubleValue();
                    dist1 = edgeLen1 - onEdge1.last().doubleValue();
                } else {
                    dist1 = onEdge1.first().doubleValue();
                }
                for (int j = 0; j < i; j++) {
                    int edge2 = out[j];
                    TreeSet<Double> onEdge2 = byEdge.get(edge2);
                    if (onEdge2 == null)
                        continue;
                    double dist2;
                    if (g.getEdgeSide(edge2, true) == vertex) {
                        double edgeLen2 = g.getEdgeLength(edge2).doubleValue();
                        dist2 = edgeLen2 - onEdge2.last().doubleValue();
                    } else {
                        dist2 = onEdge2.first().doubleValue();
                    }
                    if (dist1 + dist2 > eps * 2)
                        return false;
                }
            }
        }
        return true;
    }

    public boolean saturation2() {
        Map<Integer, TreeSet<Double>> byEdge = new TreeMap<>();
        double now = currentTime.doubleValue();
        for (Map.Entry<Number, List<SmallEntry>> entry : list.entrySet()) {
            // Время планируемого прихода в конечную вершину:
            double time = entry.getKey().doubleValue();
            double delta = time - now;
            List<SmallEntry> entries = entry.getValue();
            for (SmallEntry se : entries) {
                int edge = se.edge;
                double edgeLen = g.getEdgeLength(edge).doubleValue();
                double coord = edgeLen - delta;
                byEdge.computeIfAbsent(edge, k -> new TreeSet<>()).add(coord);
            }
        }
        int edgeNum = g.getEdgeNum();
        for (int edge = 0; edge < edgeNum; edge++) {
            if (!isEdgeSaturated(byEdge, edge))
                return false;
        }
        return true;
    }

    private boolean isEdgeSaturated(Map<Integer, TreeSet<Double>> byEdge, int edge) {
        TreeSet<Double> onEdge = byEdge.get(edge);
        if (onEdge == null)
            return false;
        double edgeLen = g.getEdgeLength(edge).doubleValue();
        Double prev = null;
        for (Double x : onEdge) {
            if (prev != null) {
                double delta = x.doubleValue() - prev.doubleValue();
                if (delta > eps * 2)
                    return false;
            }
            prev = x;
        }
        double first = onEdge.first().doubleValue();
        if (first > eps) {
            int from = g.getEdgeSide(edge, false);
            int[] out = g.allOut(from);
            boolean startCovered = false;
            for (int otherEdge : out) {
                if (otherEdge == edge)
                    continue;
                TreeSet<Double> onOtherEdge = byEdge.get(otherEdge);
                if (onOtherEdge == null)
                    continue;
                double otherCoord;
                if (g.getEdgeSide(otherEdge, true) == from) {
                    // --otherEdge-->(from)--edge-->
                    double otherLen = g.getEdgeLength(otherEdge).doubleValue();
                    double otherLast = onOtherEdge.last().doubleValue();
                    otherCoord = -(otherLen - otherLast);
                } else {
                    // <--otherEdge--(from)--edge-->
                    double otherFirst = onOtherEdge.first().doubleValue();
                    otherCoord = -otherFirst;
                }
                if (first - otherCoord <= eps * 2) {
                    startCovered = true;
                    break;
                }
            }
            if (!startCovered)
                return false;
        }
        double last = onEdge.last().doubleValue();
        if (last < edgeLen - eps) {
            int to = g.getEdgeSide(edge, true);
            int[] out = g.allOut(to);
            boolean endCovered = false;
            for (int otherEdge : out) {
                if (otherEdge == edge)
                    continue;
                TreeSet<Double> onOtherEdge = byEdge.get(otherEdge);
                if (onOtherEdge == null)
                    continue;
                double otherCoord;
                if (g.getEdgeSide(otherEdge, true) == to) {
                    // --otherEdge-->(to)<--edge--
                    double otherLen = g.getEdgeLength(otherEdge).doubleValue();
                    double otherLast = onOtherEdge.last().doubleValue();
                    otherCoord = edgeLen + (otherLen - otherLast);
                } else {
                    // <--otherEdge--(to)<--edge--
                    double otherFirst = onOtherEdge.first().doubleValue();
                    otherCoord = edgeLen + otherFirst;
                }
                if (otherCoord - last <= eps * 2) {
                    endCovered = true;
                    break;
                }
            }
            if (!endCovered)
                return false;
        }
        return true;
    }
}
