// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.units;

public class UnitsTools
{
    public static final int mPERs2 = 1;
    public static final double rad = 1.0;
    public static final double deg = 1.0;
    public static final int mm2 = 1;
    public static final int m2 = 1;
    public static final int km2 = 1;
    public static final int A = 1;
    public static final int cd = 1;
    public static final int mm = 1;
    public static final int m = 1;
    public static final int km = 1;
    public static final int g = 1;
    public static final int kg = 1;
    public static final int mPERs = 1;
    public static final int kmPERh = 1;
    public static final int mol = 1;
    public static final int K = 1;
    public static final int C = 1;
    public static final int s = 1;
    public static final int min = 1;
    public static final int h = 1;
    
    public static double toRadians(final double angdeg) {
        return Math.toRadians(angdeg);
    }
    
    public static double toDegrees(final double angrad) {
        return Math.toDegrees(angrad);
    }
    
    public static int fromMilliMeterToMeter(final int mm) {
        return mm / 1000;
    }
    
    public static int fromMeterToMilliMeter(final int m) {
        return m * 1000;
    }
    
    public static int fromMeterToKiloMeter(final int m) {
        return m / 1000;
    }
    
    public static int fromKiloMeterToMeter(final int km) {
        return km * 1000;
    }
    
    public static int fromGramToKiloGram(final int g) {
        return g / 1000;
    }
    
    public static int fromKiloGramToGram(final int kg) {
        return kg * 1000;
    }
    
    public static double fromMeterPerSecondToKiloMeterPerHour(final double mps) {
        return mps * 3.6;
    }
    
    public static double fromKiloMeterPerHourToMeterPerSecond(final double kmph) {
        return kmph / 3.6;
    }
    
    public static int fromKelvinToCelsius(final int k) {
        return k - 273;
    }
    
    public static int fromCelsiusToKelvin(final int c) {
        return c + 273;
    }
    
    public static int fromSecondToMinute(final int s) {
        return s / 60;
    }
    
    public static int fromMinuteToSecond(final int min) {
        return min * 60;
    }
    
    public static int fromMinuteToHour(final int min) {
        return min / 60;
    }
    
    public static int fromHourToMinute(final int h) {
        return h * 60;
    }
}
