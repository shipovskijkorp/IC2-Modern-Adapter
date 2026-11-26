package com.shipovskijkorp.ic2modernadapter.energy.util;

/** Numeric SI formatting from IC2 Experimental 2.8.222-ex112 Util.toSiString.
 * Retains original spaces, significant digits and rounding, including zero and infinity. */
public final class OriginalEnergyFormat {
    private OriginalEnergyFormat() {}

    public static String item(double stored, double capacity) {
        return toSiString(stored, 3) + "/" + toSiString(capacity, 3) + " EU";
    }

    /** EnergyGauge uses four significant digits and the caller's localized unit. */
    public static String gauge(double stored, double capacity) {
        return toSiString(stored, 4) + "/" + toSiString(capacity, 4) + " ";
    }

    public static String toSiString(double value, int digits) {
        int dVal;
        String si;
        double mul;
        if (value == 0.0) {
            return "0 ";
        }
        if (Double.isNaN(value)) {
            return "NaN ";
        }
        String ret = "";
        if (value < 0.0) {
            ret = "-";
            value = -value;
        }
        if (Double.isInfinite(value)) {
            return ret + "\u221e ";
        }
        double log = Math.log10(value);
        if (log >= 0.0) {
            int reduce = (int)Math.floor(log / 3.0);
            mul = 1.0 / Math.pow(10.0, reduce * 3);
            switch (reduce) {
                case 0: {
                    si = "";
                    break;
                }
                case 1: {
                    si = "k";
                    break;
                }
                case 2: {
                    si = "M";
                    break;
                }
                case 3: {
                    si = "G";
                    break;
                }
                case 4: {
                    si = "T";
                    break;
                }
                case 5: {
                    si = "P";
                    break;
                }
                case 6: {
                    si = "E";
                    break;
                }
                case 7: {
                    si = "Z";
                    break;
                }
                case 8: {
                    si = "Y";
                    break;
                }
                default: {
                    si = "E" + reduce * 3;
                    break;
                }
            }
        } else {
            int expand = (int)Math.ceil(-log / 3.0);
            mul = Math.pow(10.0, expand * 3);
            switch (expand) {
                case 0: {
                    si = "";
                    break;
                }
                case 1: {
                    si = "m";
                    break;
                }
                case 2: {
                    si = "\u00b5";
                    break;
                }
                case 3: {
                    si = "n";
                    break;
                }
                case 4: {
                    si = "p";
                    break;
                }
                case 5: {
                    si = "f";
                    break;
                }
                case 6: {
                    si = "a";
                    break;
                }
                case 7: {
                    si = "z";
                    break;
                }
                case 8: {
                    si = "y";
                    break;
                }
                default: {
                    si = "E-" + expand * 3;
                }
            }
        }
        int iVal = (int)Math.floor(value *= mul);
        value -= (double)iVal;
        int iDigits = 1;
        if (iVal > 0) {
            iDigits = (int)((double)iDigits + Math.floor(Math.log10(iVal)));
        }
        if ((double)(dVal = (int)Math.round(value * (mul = Math.pow(10.0, digits - iDigits)))) >= mul) {
            dVal = (int)((double)dVal - mul);
            iDigits = 1;
            if (++iVal > 0) {
                iDigits = (int)((double)iDigits + Math.floor(Math.log10(iVal)));
            }
        }
        ret = ret + Integer.toString(iVal);
        if (digits > iDigits && dVal != 0) {
            ret = ret + String.format(".%0" + (digits - iDigits) + "d", dVal);
        }
        ret = ret.replaceFirst("(\\.\\d*?)0+$", "$1");
        return ret + " " + si;
    }
}
