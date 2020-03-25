package nz.ac.waikato.orca;

public class ModelLQR {

  private static double[] intercept = { 0.39887, 4.2307403 };
  public static final int CPU = 0;
  public static final int MEMORY = 1;

  private ModelLQR() {
  };

  public static void UpdateIntercept(double NewIntercept, int type) {
    intercept[type] = NewIntercept;
  }

  public static double encodeMeasurement(double measurement, int type) {
    return Math.log(measurement) - intercept[type];
  }

  public static double encodeParameter(double parameter) {
    return Math.log(parameter);
  }

  public static double decodeMeasurement(double measurement, int type) {
    return Math.exp(measurement + intercept[type]);
  }

  public static double decodeParameter(double parameter) {
    return Math.exp(parameter);
  }

}