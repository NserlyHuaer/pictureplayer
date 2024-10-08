package Tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EqualsProportion {

    public static double Start(double first, double second, double third, double fourth)  {
        if (first == 0)
            return Run(first, second, third, fourth);
        else if (second == 0)
            return Run2(first, second, third, fourth);
        else if (third == 0)
            return Run3(first, second, third, fourth);
        else if (fourth == 0)
            return Run4(first, second, third, fourth);
        else
            throw new NumberFormatException("No \"x\"(0) Number");
    }

    private static double Run(double u, double i, double o, double p) {
        double zz = i * o;
        BigDecimal q = new BigDecimal(zz);
        BigDecimal weer = new BigDecimal(p);
        double qwe = q.divide(weer, 14, RoundingMode.HALF_UP).doubleValue();
        return qwe;


    }

    // 未知量为i
    private static double Run2(double u, double i, double o, double p) {
        double zz = u * p;// 这是未知量外的总数
        BigDecimal q = new BigDecimal(zz);// 转换为Big...
        BigDecimal weer = new BigDecimal(o);// 未知量隔壁的数
        double qwe = q.divide(weer, 14, RoundingMode.HALF_UP).doubleValue();
        return qwe;

    }

    // 未知量为o
    private static double Run3(double u, double i, double o, double p) {
        double zz = u * p;// 这是未知量外的总数
        BigDecimal q = new BigDecimal(zz);// 转换为Big...
        BigDecimal weer = new BigDecimal(i);// 未知量隔壁的数

        double qwe = q.divide(weer, 14, RoundingMode.HALF_UP).doubleValue();
        return qwe;

    }

    // 未知量为p
    private static double Run4(double u, double i, double o, double p) {
        double zz = i * o;// 这是未知量外的总数
        BigDecimal q = new BigDecimal(zz);// 转换为Big...
        BigDecimal weer = new BigDecimal(u);// 未知量隔壁的数

        double qwe = q.divide(weer, 14, RoundingMode.HALF_UP).doubleValue();
        return qwe;

    }
}
