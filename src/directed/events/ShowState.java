package directed.events;

import java.util.List;
import java.util.Map;

public final class ShowState {

    public final Map<Integer, List<Double>> edges;
    public final Map<Integer, Double> edgeLens;

    public ShowState(Map<Integer, List<Double>> edges, Map<Integer, Double> edgeLens) {
        this.edges = edges;
        this.edgeLens = edgeLens;
    }
}
