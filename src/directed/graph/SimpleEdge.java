package directed.graph;

public final class SimpleEdge {

    final int node1;
    final int node2;
    final Number length;
    final Boolean startForward;

    public SimpleEdge(int node1, int node2, Number length, Boolean startForward) {
        this.node1 = node1;
        this.node2 = node2;
        this.length = length;
        this.startForward = startForward;
    }
}
