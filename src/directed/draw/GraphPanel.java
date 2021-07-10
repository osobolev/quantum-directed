package directed.draw;

import directed.draw.model.Edge;
import directed.draw.model.GraphModel;
import directed.draw.model.Node;
import directed.events.ShowState;
import directed.events.StatResult;
import directed.graph.Graph;
import directed.util.Arithmetic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class GraphPanel extends JComponent {

    private static final double L = 20;
    private static final double K = 2;
    private static final int R = 10;
    private static final int D = R * 2;
    private static final Color START_COLOR = Color.red.darker();

    private final GraphModel model = new GraphModel();
    final DecimalFormat df4;
    private StatModeEnum mode;

    private Node selected = null;
    private Edge edgeSelected = null;
    private Node dragging = null;
    private Node edgeDragging = null;
    private Point maxEdgeDrag = null;
    private Point current = null;
    private Point edgeBreaking = null;

    private File saved = null;
    private boolean changed = false;

    private StatResult result = null;
    private ShowState showState = null;

    GraphPanel(StatModeEnum mode) {
        this.mode = mode;

        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.US);
        df4 = new DecimalFormat("#.####", dfs);
        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    Window window = SwingUtilities.getWindowAncestor(GraphPanel.this);
                    if (edgeSelected != null) {
                        if (result == null) {
                            Boolean direction = model.getStartDirection(edgeSelected);
                            WeightDialog dlg = new WeightDialog(
                                (Frame) window, edgeSelected.weight, direction
                            );
                            if (dlg.isOk()) {
                                model.setEdgeWeight(edgeSelected, dlg.getResult());
                                Boolean startDirection = dlg.getStartDirection();
                                model.setStartEdge(edgeSelected, startDirection);
                                changed = true;
                                repaint();
                            }
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (edgeDragging != null) {
                    Node node = model.findNode(e.getPoint(), R);
                    if (node != null) {
                        if (result == null) {
                            if (edgeDragging.equals(node)) {
                                double d = edgeDragging.p.distance(maxEdgeDrag);
                                if (d > R * 2) {
                                    model.connectLoop(edgeDragging, maxEdgeDrag);
                                    changed = true;
                                }
                            } else {
                                model.connect(edgeDragging, node);
                                changed = true;
                            }
                        }
                    }
                    edgeDragging = null;
                    maxEdgeDrag = null;
                    repaint();
                } else if (dragging != null) {
                    dragging = null;
                } else if (edgeBreaking != null) {
                    edgeBreaking = null;
                } else {
                    Node node = model.findNode(e.getPoint(), R);
                    if (node != null) {
                        selected = node;
                        edgeSelected = null;
                        moveFocus();
                        repaint();
                    } else {
                        Edge edge = model.findEdge(e.getPoint(), R);
                        if (edge != null) {
                            selected = null;
                            edgeSelected = edge;
                            moveFocus();
                            repaint();
                        } else {
                            Node exists = model.findNode(e.getPoint(), D);
                            if (exists == null) {
                                if (result == null) {
                                    model.add(new Node(e.getPoint()));
                                    changed = true;
                                    repaint();
                                }
                            }
                        }
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {

            public void mouseDragged(MouseEvent e) {
                Node node;
                Point point;
                if (dragging != null) {
                    node = dragging;
                    point = null;
                } else if (edgeDragging != null) {
                    node = edgeDragging;
                    point = null;
                } else if (edgeBreaking != null) {
                    node = null;
                    point = edgeBreaking;
                } else {
                    node = model.findNode(e.getPoint(), R);
                    point = null;
                    if (node == null) {
                        Edge edge = model.findEdge(e.getPoint(), R);
                        if (edge != null) {
                            if (edge.arc == null) {
                                edge.arc = new Point(edge.middleX(), edge.middleY());
                            }
                            point = edge.arc;
                        }
                    }
                }
                if (node != null || point != null) {
                    boolean left = (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0;
                    if (point != null) {
                        point.setLocation(e.getPoint());
                        edgeBreaking = point;
                        changed = true;
                        repaint();
                    } else {
                        if (left) {
                            edgeDragging = node;
                            current = e.getPoint();
                            if (maxEdgeDrag == null) {
                                maxEdgeDrag = current;
                            } else {
                                if (current.distance(edgeDragging.p) > maxEdgeDrag.distance(edgeDragging.p)) {
                                    maxEdgeDrag = current;
                                }
                            }
                            repaint();
                        } else {
                            node.p.setLocation(e.getPoint());
                            dragging = node;
                            changed = true;
                            repaint();
                        }
                    }
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (selected != null) {
                        if (result == null) {
                            if (model.removeNode(selected, edgeSelected)) {
                                edgeSelected = null;
                            }
                            selected = null;
                            changed = true;
                            repaint();
                        }
                    } else if (edgeSelected != null) {
                        if (result == null) {
                            model.removeEdge(edgeSelected);
                            edgeSelected = null;
                            changed = true;
                            repaint();
                        }
                    }
                }
            }
        });
        setFocusable(true);
    }

    private void moveFocus() {
        this.requestFocusInWindow();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g, false);
        if (showState != null) {
            g.setColor(Color.blue);
            List<Edge> edges = model.getEdges();
            for (Map.Entry<Integer, List<Double>> entry : showState.edges.entrySet()) {
                int edge = entry.getKey().intValue();
                List<Double> photons = entry.getValue();
                Edge e = edges.get(edge);
                for (Double photon : photons) {
                    double k = photon.doubleValue();
                    double xp;
                    double yp;
                    double displayLen;
                    if (e.arc == null) {
                        xp = (e.p2.p.x - e.p1.p.x) * k + e.p1.p.x;
                        yp = (e.p2.p.y - e.p1.p.y) * k + e.p1.p.y;
                        displayLen = e.p2.p.distance(e.p1.p);
                    } else {
                        if (e.isLoop()) {
                            Point2D pc = new Point.Double((e.p1.p.x + e.arc.x) / 2.0, (e.p1.p.y + e.arc.y) / 2.0);
                            double dx = e.p1.p.x - pc.getX();
                            double dy = e.p1.p.y - pc.getY();
                            double a = 2 * Math.PI * k;
                            double dx1 = dx * Math.cos(a) + dy * Math.sin(a);
                            double dy1 = dy * Math.cos(a) - dx * Math.sin(a);
                            xp = pc.getX() + dx1;
                            yp = pc.getY() + dy1;
                            double r = pc.distance(e.p1.p);
                            displayLen = 2 * Math.PI * r;
                        } else {
                            double toArc = e.p1.p.distance(e.arc);
                            double fromArc = e.arc.distance(e.p2.p);
                            double len = k * (toArc + fromArc);
                            if (len > toArc) {
                                double k1 = (len - toArc) / fromArc;
                                xp = (e.p2.p.x - e.arc.x) * k1 + e.arc.x;
                                yp = (e.p2.p.y - e.arc.y) * k1 + e.arc.y;
                            } else {
                                double k1 = len / toArc;
                                xp = (e.arc.x - e.p1.p.x) * k1 + e.p1.p.x;
                                yp = (e.arc.y - e.p1.p.y) * k1 + e.p1.p.y;
                            }
                            displayLen = toArc + fromArc;
                        }
                    }
                    int xc = (int) Math.round(xp);
                    int yc = (int) Math.round(yp);
                    g.fillOval(xc - 5, yc - 5, 10, 10);
                    if (result != null) {
                        double realLen = showState.edgeLens.get(edge).doubleValue();
                        int r = (int) Math.round(result.eps * displayLen / realLen);
                        g.drawOval(xc - r, yc - r, r * 2, r * 2);
                    }
                }
            }
        }
    }

    void draw(Graphics g, boolean large) {
        Font font;
        if (large) {
            font = getFont().deriveFont(18f);
        } else {
            font = getFont().deriveFont(Font.BOLD);
        }
        g.setFont(font);
        g.setColor(getForeground());

        FontMetrics fm = g.getFontMetrics();
        int h = fm.getHeight();
        if (result != null) {
            int y = 20;
            g.drawString("Time: " + df4.format(result.currentTime), 20, y);
            y += h;
            String statStr = "Packets: " + result.numPhotons +
                             (result.maxTime == null ? "" : " (max. " + result.maxCount + " since T=" + df4.format(result.maxTime) + ")");
            g.drawString(statStr, 20, y);
            y += h;
            String satStr = "Saturated: " + (result.saturationTime != null ? "since T=" + df4.format(result.saturationTime) : "no");
            g.drawString(satStr, 20, y);
            y += h;
        }

        List<Edge> edges = model.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            Boolean startForward = model.getStartDirection(e);
            boolean isStart = startForward != null;
            if (isStart) {
                g.setColor(START_COLOR);
            } else {
                g.setColor(Color.gray);
            }
            int x1;
            int y1;
            if (e.arc == null) {
                g.drawLine(e.p1.p.x, e.p1.p.y, e.p2.p.x, e.p2.p.y);
                x1 = e.middleX();
                y1 = e.middleY();
            } else {
                if (e.isLoop()) {
                    Point2D pc = new Point.Double((e.p1.p.x + e.arc.x) / 2.0, (e.p1.p.y + e.arc.y) / 2.0);
                    int r = (int) Math.round(pc.distance(e.p1.p));
                    g.drawOval((int) Math.round(pc.getX() - r), (int) Math.round(pc.getY() - r), 2 * r, 2 * r);
                } else {
                    g.drawLine(e.p1.p.x, e.p1.p.y, e.arc.x, e.arc.y);
                    g.drawLine(e.arc.x, e.arc.y, e.p2.p.x, e.p2.p.y);
                }
                x1 = e.arc.x;
                y1 = e.arc.y;
            }
            if (showState == null) {
                g.setColor(Color.black);
                String edgeString = getEdgeString(i, e);
                if (edgeString != null) {
                    g.drawString(edgeString, x1 + 5, y1 + 5);
                }
                drawArrow(g, e, startForward);
            } else {
                drawArrow(g, e, startForward);
            }
            if (e == edgeSelected) {
                g.setColor(Color.black);
                g.fillRect(x1 - 3, y1 - 3, 6, 6);
            }
        }
        List<Node> nodes = model.getNodes();
        for (Node p : nodes) {
            g.setColor(Color.yellow.darker());
            g.fillOval(p.p.x - R, p.p.y - R, D, D);
            if (p.equals(selected)) {
                g.setColor(Color.black);
                g.drawRoundRect(p.p.x - R, p.p.y - R, D, D, 3, 3);
            }
        }
        if (edgeDragging != null && current != null) {
            g.setColor(Color.black);
            g.drawLine(edgeDragging.p.x, edgeDragging.p.y, current.x, current.y);
        }
    }

    private static void drawArrow(Graphics g, Edge e, Boolean startForward) {
        if (startForward != null) {
            g.setColor(START_COLOR);
        }
        int xs2;
        int ys2;
        double dx;
        double dy;
        double r;
        if (e.isLoop()) {
            int xs1 = e.p1.p.x;
            int ys1 = e.p1.p.y;
            xs2 = e.arc.x;
            ys2 = e.arc.y;
            double dx1 = xs2 - xs1;
            double dy1 = ys2 - ys1;
            dx = dy1;
            dy = -dx1;
            r = 0;
        } else {
            int xs1;
            int ys1;
            if (e.arc == null) {
                xs1 = e.p1.p.x;
                ys1 = e.p1.p.y;
            } else {
                xs1 = e.arc.x;
                ys1 = e.arc.y;
            }
            xs2 = e.p2.p.x;
            ys2 = e.p2.p.y;
            dx = xs2 - xs1;
            dy = ys2 - ys1;
            r = R;
        }
        double len = Math.sqrt(dx * dx + dy * dy);
        dx /= len;
        dy /= len;
        int xe0 = (int) Math.round(xs2 - r * dx);
        int ye0 = (int) Math.round(ys2 - r * dy);
        double xe1 = xe0 - L * dx - L / K * dy;
        double ye1 = ye0 - L * dy + L / K * dx;
        double xe2 = xe0 - L * dx + L / K * dy;
        double ye2 = ye0 - L * dy  - L / K * dx;
        g.drawLine(xe0, ye0, (int) Math.round(xe1), (int) Math.round(ye1));
        g.drawLine(xe0, ye0, (int) Math.round(xe2), (int) Math.round(ye2));
    }

    private String getEdgeString(int i, Edge e) {
        if (result == null) {
            return e.weight;
        } else {
            return mode.mode.getEdgeValue(result, i);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 500);
    }

    File getSaved() {
        return saved;
    }

    boolean isChanged() {
        return changed;
    }

    void save(File file) throws FileNotFoundException {
        model.save(file);

        saved = file;
        changed = false;
    }

    private void reset() {
        selected = null;
        edgeSelected = null;
        dragging = null;
        edgeDragging = null;
        maxEdgeDrag = null;
        current = null;
        edgeBreaking = null;
    }

    void load(File file) throws FileNotFoundException {
        doLoad(file, new FileReader(file));
    }

    void load(File file, Readable source) throws FileNotFoundException {
        if (source == null) {
            source = new FileReader(file);
        }
        doLoad(file, source);
    }

    private void doLoad(File file, Readable source) {
        model.load(source);
        reset();

        repaint();
        saved = file;
        changed = false;
    }

    void createNew() {
        model.createNew();
        reset();

        repaint();
        saved = null;
        changed = false;
    }

    Graph toGraph(Arithmetic a) {
        return model.toGraph(a);
    }

    private double needTime = 0;

    void setStat(StatResult result, ShowState state) {
        if (result != null && result.currentTime.doubleValue() >= needTime) {
            double time = result.currentTime.doubleValue();
            needTime = Math.max(Math.floor(time + 1), needTime + 1);
        }
        this.result = result;
        this.showState = state;
        repaint();
    }

    void clearStat() {
        needTime = 0;
        showState = null;
        this.result = null;
        repaint();
    }

    StatModeEnum getMode() {
        return mode;
    }

    void setMode(StatModeEnum mode) {
        this.mode = mode;
        repaint();
    }
}
