package directed.events;

import directed.draw.model.GraphModel;
import directed.graph.Graph;
import directed.util.Arithmetic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SatStat {

    private static final DecimalFormatSymbols DFS = DecimalFormatSymbols.getInstance();

    private static String format(Number x) {
        return String.valueOf(x.doubleValue()).replace('.', DFS.getDecimalSeparator());
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage:");
            System.err.println("sat <graph file> <eps file>");
            System.err.println("sat <graph file> <neps>");
            return;
        }
        Path graphFile = Paths.get(args[0]);
        GraphModel graphModel = new GraphModel();
        try (BufferedReader rdr = Files.newBufferedReader(graphFile)) {
            graphModel.load(rdr);
        }
        Arithmetic arithmetic = Arithmetic.createArithmetic(1e-10, 0);
        Graph graph = graphModel.toGraph(arithmetic);
        double minEdgeLen = Double.MAX_VALUE;
        double maxEdgeLen = Double.MIN_VALUE;
        for (int i = 0; i < graph.getEdgeNum(); i++) {
            double len = graph.getEdgeLength(i).doubleValue();
            minEdgeLen = Math.min(minEdgeLen, len);
            maxEdgeLen = Math.max(maxEdgeLen, len);
        }

        List<Double> epss;
        Path epsFile = Paths.get(args[1]);
        if (Files.exists(epsFile)) {
            epss = Files.lines(epsFile)
                .filter(line -> {
                    String trimmed = line.trim();
                    return !trimmed.isEmpty() && !trimmed.startsWith("#");
                })
                .map(line -> Double.parseDouble(line.replace(',', '.')))
                .collect(Collectors.toList());
        } else {
            int maxi = Integer.parseInt(args[1]);
            double maxEps = minEdgeLen / 2;
            epss = new ArrayList<>();
            for (int i = 0; i < maxi; i++) {
                double eps = (double) (maxi - i) / maxi * maxEps;
                epss.add(eps);
            }
        }

        for (Double eps : epss) {
            Schedule schedule = new Schedule(graph, arithmetic, eps.doubleValue());
            schedule.firstPhotons();
            Number firstSaturation = null;
            Number lastSaturation = null;
            while (true) {
                if (!schedule.next())
                    break;
                StatResult stat = schedule.getStat();
                if (stat.saturationTime != null) {
                    if (firstSaturation == null) {
                        firstSaturation = stat.saturationTime;
                    }
                    if (lastSaturation != null) {
                        double elapsed = stat.currentTime.doubleValue() - lastSaturation.doubleValue();
                        if (elapsed > maxEdgeLen * 2) {
                            System.out.printf("%s\t%s\t%s%n", format(eps), format(firstSaturation), format(stat.saturationTime));
                            break;
                        }
                    } else {
                        lastSaturation = stat.currentTime;
                    }
                } else {
                    lastSaturation = null;
                }
            }
        }
    }
}
