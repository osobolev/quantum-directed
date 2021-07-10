package directed.draw;

import directed.events.StatResult;

import java.text.DecimalFormat;
import java.util.Iterator;

abstract class StatMode {

    private static final DecimalFormat df8 = new DecimalFormat("#.########");
    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    private abstract static class EdgeStatMode extends StatMode {

        protected EdgeStatMode(String text) {
            super(text);
        }

        public final String getEdgeValue(StatResult stat, int edge) {
            DecimalFormat format = getFormat();
            double value = getNumberValue(stat, edge);
            return format == null ? String.valueOf(value) : format.format(value);
        }

        protected abstract DecimalFormat getFormat();

        protected abstract double getNumberValue(StatResult stat, int edge);
    }

    static final StatMode NUMBER = new EdgeStatMode("Num Packets") {

        protected DecimalFormat getFormat() {
            return null;
        }

        protected double getNumberValue(StatResult stat, int edge) {
            return stat.edgeNum[edge];
        }
    };
    static final StatMode NUM_BY_LEN = new EdgeStatMode("Packets/Length") {

        protected DecimalFormat getFormat() {
            return df2;
        }

        protected double getNumberValue(StatResult stat, int edge) {
            return stat.edgeNum[edge] / stat.g.getEdgeLength(edge).doubleValue();
        }
    };
    static final StatMode NUM_BY_LEN_BY_NUM = new EdgeStatMode("Packets/Length/Total") {

        protected DecimalFormat getFormat() {
            return df8;
        }

        protected double getNumberValue(StatResult stat, int edge) {
            return stat.edgeNum[edge] / stat.g.getEdgeLength(edge).doubleValue() / stat.numPhotons;
        }
    };

    private final String text;

    private StatMode(String text) {
        this.text = text;
    }

    public final String toString() {
        return text;
    }

    public abstract String getEdgeValue(StatResult stat, int edge);

    public Iterator<String> getValues(StatResult stat) {
        return new IntIterator(stat.g.getEdgeNum()) {
            protected String getString(int i) {
                return getEdgeValue(stat, i);
            }
        };
    }
}
