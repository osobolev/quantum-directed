package directed.draw;

import directed.events.Schedule;
import directed.events.ShowState;
import directed.events.StatResult;
import directed.graph.Graph;
import directed.util.Arithmetic;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;

final class Running {

    private final GraphPanel panel;
    private final Schedule schedule;
    private final Thread calcThread;

    private final boolean showPhotons;

    private boolean running = true;

    private double showing = 0;
    private double delta = 0;
    private boolean inited = false;

    Running(GraphPanel panel, Options options, boolean showPhotons) {
        this.panel = panel;
        this.showPhotons = showPhotons;
        Arithmetic a = Arithmetic.createArithmetic(options.timeTol, options.precision);
        Graph graph = panel.toGraph(a);
        this.schedule = new Schedule(graph, a, options.eps);

        calcThread = new Thread(() -> {
            init();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
            }
            PrintWriter w = null;
            if (options.useLog) {
                try {
                    w = new PrintWriter(options.logFile);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
            char separator = dfs.getDecimalSeparator();
            double prevTime = 0;
            while (true) {
                synchronized (schedule) {
                    if (!running)
                        break;
                    if (w != null) {
                        StatResult stat = schedule.getStat();
                        double time = stat.currentTime.doubleValue();
                        if (time > prevTime + 1.0) {
                            w.print(String.valueOf(time).replace('.', separator));
                            Iterator<String> values = options.logMode.mode.getValues(stat);
                            while (values.hasNext()) {
                                String value = values.next();
                                w.print("\t" + value.replace('.', separator));
                            }
                            w.println();
                            prevTime = time;
                        }
                    }
                    if (!schedule.next())
                        break;
                }
            }
            if (w != null) {
                w.close();
            }
        });
    }

    private void init() {
        if (!inited) {
            inited = true;
            schedule.firstPhotons();
        }
    }

    void start() {
        if (!showPhotons) {
            calcThread.start();
        }
    }

    StatResult getStat() {
        return schedule.getStat();
    }

    ShowState getPhotons() {
        if (showPhotons) {
            init();
            synchronized (schedule) {
                double time = showing;
                showing += delta;
                return schedule.showPhotons(time);
            }
        } else {
            return null;
        }
    }

    void stop() {
        synchronized (schedule) {
            running = false;
        }
    }

    /**
     * @param speed 0 to 1
     */
    void setSpeed(double speed) {
        synchronized (schedule) {
            delta = (Math.pow(10, speed * 2 - 1) - 0.1) * 0.01;
        }
    }

    boolean refresh() {
        StatResult result = getStat();
        ShowState map = getPhotons();
        if (showPhotons && map == null) {
            return false;
        } else {
            panel.setStat(result, map);
            return true;
        }
    }
}
