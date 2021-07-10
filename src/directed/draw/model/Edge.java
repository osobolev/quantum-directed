package directed.draw.model;

import java.awt.*;

public final class Edge {

    public final Node p1;
    public final Node p2;
    public String weight;
    public Point arc = null;
    public Boolean startForward = null;

    Edge(Node p1, Node p2) {
        this(p1, p2, "1");
    }

    Edge(Node p1, Node p2, String weight) {
        this.p1 = p1;
        this.p2 = p2;
        this.weight = weight;
    }

    public int middleX() {
        return (p1.p.x + p2.p.x) / 2;
    }

    public int middleY() {
        return (p1.p.y + p2.p.y) / 2;
    }

    public boolean isLoop() {
        return p1.equals(p2);
    }
}
