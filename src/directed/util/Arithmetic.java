package directed.util;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Arithmetic {

    private final double timeTol;
    private final Apfloat timeTolBD;
    private final int precision;

    private Arithmetic(double timeTol, int precision) {
        this.timeTol = timeTol;
        //this.timeTolBD = BigDecimal.valueOf(timeTol);
        this.timeTolBD = new Apfloat(timeTol);
        this.precision = precision;
    }

    public Number zero() {
        if (precision <= 0) {
            return 0.0;
        } else {
            //return BigDecimal.ZERO;
            return Apfloat.ZERO;
        }
    }

    public Number add(Number n1, Number n2) {
        if (precision <= 0) {
            return n1.doubleValue() + n2.doubleValue();
        } else {
//            BigDecimal b1 = (BigDecimal) n1;
//            BigDecimal b2 = (BigDecimal) n2;
            Apfloat b1 = (Apfloat) n1;
            Apfloat b2 = (Apfloat) n2;
            return b1.add(b2);
        }
    }

    public boolean isNear(Number n1, Number n2) {
        if (precision <= 0) {
            return Math.abs(n1.doubleValue() - n2.doubleValue()) < timeTol;
        } else {
//            BigDecimal b1 = (BigDecimal) n1;
//            BigDecimal b2 = (BigDecimal) n2;
//            return b1.subtract(b2).abs().compareTo(timeTolBD) < 0;
            Apfloat b1 = (Apfloat) n1;
            Apfloat b2 = (Apfloat) n2;
            return ApfloatMath.abs(b1.subtract(b2)).compareTo(timeTolBD) < 0;
        }
    }

    public Number evaluate(Number evalResult) {
        if (precision <= 0) {
            return evalResult.doubleValue();
        } else {
//            if (evalResult instanceof BigDecimal) {
//                return evalResult;
//            } else {
//                return BigDecimal.valueOf(evalResult.doubleValue());
//            }
            if (evalResult instanceof Apfloat) {
                return evalResult;
            } else if (evalResult instanceof BigDecimal) {
                return new Apfloat((BigDecimal) evalResult, precision);
            } else {
                return new Apfloat(evalResult.doubleValue(), precision);
            }
        }
    }

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private static BigDecimal sqrt(BigDecimal a, int scale) {
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(a.doubleValue()));

        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = a.divide(x0, scale, RoundingMode.HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(TWO, scale, RoundingMode.HALF_UP);
        }

        return x1;
    }

    public Number sqrt(Number n) {
        if (precision <= 0) {
            return Math.sqrt(n.doubleValue());
        } else {
//            BigDecimal b;
//            if (n instanceof BigDecimal) {
//                b = (BigDecimal) n;
//            } else {
//                b = BigDecimal.valueOf(n.doubleValue());
//            }
//            return sqrt(b, precision);
            Apfloat b;
            if (n instanceof Apfloat) {
                b = (Apfloat) n;
            } else if (n instanceof BigDecimal) {
                b = new Apfloat((BigDecimal) n, precision);
            } else {
                b = new Apfloat(n.doubleValue(), precision);
            }
            return ApfloatMath.sqrt(b);
        }
    }

    public static Arithmetic createArithmetic(double timeTol, int precision) {
        return new Arithmetic(timeTol, precision);
    }
}
