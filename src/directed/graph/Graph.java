package directed.graph;

import java.util.*;

public final class Graph {

    private final int numVertex;
    private final Number[] lengths; // [numEdges]
    private final int[][] outEdges; // [numVertex][]
    private final int[][] outEdgesFwd; // [numVertex][]
//    private final int[][] inEdgesFwd; // [numVertex][]
    private final int[][] destination; // [numEdges][2]
    private final Boolean[] startForward; // [numEdges]

    public Graph(int numVertex, SimpleEdge[] edges) {
        this.numVertex = numVertex;
        outEdges = new int[numVertex][];
        outEdgesFwd = new int[numVertex][];
//        inEdgesFwd = new int[numVertex][];
        lengths = new Number[edges.length];
        destination = new int[edges.length][2];
        startForward = new Boolean[edges.length];
        Map<Integer, List<Integer>> outMap = new HashMap<>(numVertex);
        Map<Integer, List<Integer>> outMapFwd = new HashMap<>(numVertex);
        Map<Integer, List<Integer>> inMapFwd = new HashMap<>(numVertex);
        for (int i = 0; i < edges.length; i++) {
            SimpleEdge e = edges[i];
            lengths[i] = e.length;
            startForward[i] = e.startForward;
            destination[i][0] = e.node1;
            destination[i][1] = e.node2;
            put(outMap, e.node1, i);
            put(outMap, e.node2, i);
            put(outMapFwd, e.node1, i);
            put(inMapFwd, e.node2, i);
        }
        for (int i = 0; i < numVertex; i++) {
            outEdges[i] = toArray(outMap.get(i));
            outEdgesFwd[i] = toArray(outMapFwd.get(i));
//            inEdgesFwd[i] = toArray(inMapFwd.get(i));
        }
    }

    private static int[] toArray(List<Integer> c) {
        if (c == null)
            return new int[0];
        int num = c.size();
        int[] array = new int[num];
        int j = 0;
        for (Integer e : c) {
            array[j++] = e.intValue();
        }
        return array;
    }

    private static void put(Map<Integer, List<Integer>> outMap, int vertex, int edge) {
        outMap.computeIfAbsent(vertex, k -> new ArrayList<>()).add(edge);
    }

    public int getVertexNum() {
        return numVertex;
    }

    public int getEdgeNum() {
        return lengths.length;
    }

    public int[] allOut(int vertex) {
        return outEdges[vertex];
    }

    public int[] goingOut(int vertex) {
        return outEdgesFwd[vertex];
    }

    public Number getEdgeLength(int edge) {
        return lengths[edge];
    }

    public int getEdgeSide(int edge, boolean forward) {
        int[] vert = destination[edge];
        return forward ? vert[1] : vert[0];
    }

    public Boolean isStartForward(int edge) {
        return startForward[edge];
    }
}
