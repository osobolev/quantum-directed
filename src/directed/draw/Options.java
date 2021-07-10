package directed.draw;

import java.io.File;
import java.util.prefs.Preferences;

final class Options {

    final double timeTol;
    final int precision;
    final double eps;
    final boolean useLog;
    final File logFile;
    final StatModeEnum logMode;

    Options(double timeTol, int precision, double eps, boolean useLog, File logFile, StatModeEnum logMode) {
        this.timeTol = timeTol;
        this.precision = precision;
        this.eps = eps;
        this.useLog = useLog;
        this.logFile = logFile;
        this.logMode = logMode;
    }

    static File getDefaultFile() {
        return new File("graphlog.txt");
    }

    static Options load(Preferences prefs) {
        double timeTol = prefs.getDouble("time.tol", 1e-10);
        int precision = prefs.getInt("precision", 0);
        double eps = prefs.getDouble("eps", 0.1);
        boolean useLog = prefs.getBoolean("log.use", false);
        String logFileName = prefs.get("log.file", getDefaultFile().getAbsolutePath());
        String logFileMode = prefs.get("log.mode", StatModeEnum.NUM_BY_LEN_BY_NUM.name());
        return new Options(
            timeTol, precision, eps,
            useLog, new File(logFileName), StatModeEnum.valueOf(logFileMode)
        );
    }

    void save(Preferences prefs) {
        prefs.putDouble("time.tol", timeTol);
        prefs.putInt("precision", precision);
        prefs.putDouble("eps", eps);
        prefs.putBoolean("log.use", useLog);
        prefs.put("log.file", logFile == null ? null : logFile.getAbsolutePath());
        prefs.put("log.mode", logMode.name());
    }
}
