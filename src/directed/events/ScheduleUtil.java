package directed.events;

import directed.graph.Graph;
import directed.util.Arithmetic;

import java.util.*;

public final class ScheduleUtil {

    public static <K extends Number, T> void schedule(Arithmetic a, TreeMap<K, List<T>> list, K time, T newEntry) {
        List<T> existing = list.get(time);
        if (existing != null) {
            existing.add(newEntry);
        } else {
            Map.Entry<K, List<T>> upper = list.ceilingEntry(time);
            List<T> addTo = null;
            if (upper != null) {
                if (a.isNear(time, upper.getKey())) {
                    addTo = upper.getValue();
                }
            }
            if (addTo == null) {
                Map.Entry<K, List<T>> lower = list.floorEntry(time);
                if (lower != null) {
                    if (a.isNear(time, lower.getKey())) {
                        addTo = lower.getValue();
                    }
                }
            }
            if (addTo == null) {
                List<T> newList = new ArrayList<>();
                newList.add(newEntry);
                list.put(time, newList);
            } else {
                addTo.add(newEntry);
            }
        }
    }

    public static <K extends Number> StatResult getStat(Graph g, TreeMap<K, List<SmallEntry>> list,
                                                        K currentTime, K maxTime, int maxCount,
                                                        double eps, Number saturationTime, long saturationClock) {
        int count = 0;
        int nedges = g.getEdgeNum();
        int[] edgeNum = new int[nedges];
        for (List<SmallEntry> entries : list.values()) {
            for (SmallEntry entry : entries) {
                int edge = entry.edge;
                edgeNum[edge]++;
                count++;
            }
        }
        return new StatResult(currentTime, count, maxTime, maxCount, eps, saturationTime, saturationClock, edgeNum, g);
    }

    public static <K extends Number> ShowState showPhotons(Graph g, TreeMap<K, List<SmallEntry>> list, double time, Schedule schedule) {
        while (true) {
            if (list.isEmpty())
                return null;
            Number first = list.firstKey();
            if (first.doubleValue() < time) {
                schedule.next();
            } else {
                break;
            }
        }
        Map<Integer, List<Double>> map = new HashMap<>();
        for (Map.Entry<K, List<SmallEntry>> entries : list.entrySet()) {
            double arrival = entries.getKey().doubleValue();
            for (SmallEntry entry : entries.getValue()) {
                int edge = entry.edge;
                double edgeLength = g.getEdgeLength(edge).doubleValue();
                double started = arrival - edgeLength;
                double position = (time - started) / edgeLength;
                List<Double> showList = map.computeIfAbsent(edge, k -> new ArrayList<>());
                showList.add(position);
            }
        }
        Map<Integer, Double> edgeLens = new HashMap<>();
        for (int i = 0; i < g.getEdgeNum(); i++) {
            edgeLens.put(i, g.getEdgeLength(i).doubleValue());
        }
        return new ShowState(map, edgeLens);
    }
}
