package Tools;

public class Simplified {
    // 被除数
    private static double bcs;
    // 除数
    private static double cs;
    // 乘以个数，数字越大支持越大小数，同时计算速度将减少
    private static final long charter = 10000000000L;
    private static long[] longs;

    public long[] getLastResult() {
        return longs;
    }

    public static long[] Computing(double bcs, double cs) {
        var bcs123 = bcs * Simplified.charter;
        var cs123 = cs * Simplified.charter;
        a:
        for (int i = 2; i <= Math.min(bcs123, cs123); i++) {
            for (; ; ) {
                if (bcs123 == cs123) {
                    bcs123 = 1;
                    cs123 = 1;
                    break a;
                } else if ((bcs123 % i == 0) && (cs123 % i == 0)) {
                    bcs123 = bcs123 / i;//
                    cs123 = cs123 / i;//
                } else {
                    break;
                }
            }
        }

        Simplified.bcs = bcs123;
        Simplified.cs = cs123;
        longs = new long[]{(long) bcs, (long) cs};
        return longs;
    }

}
