package nz.ac.waikato.orca;

public class ControllerPID extends ControllerHead {
	public final int INTEGRALHISTORY;
	private double[] perror = new double[2];
	private double[] integral = new double[2];
	private double[][] integralHistory;
	private int[] integralHistoryPlace = new int[2];
	private boolean useIntegralHistory = true;

	private final int ADVRADGEBUFFER = 2;
	private double[][] RunningMeasure = new double[2][ADVRADGEBUFFER];
	private int[] RunningMeasurePlace = new int[2];
	private int[] totalMeasure = new int[2];
	private double[] MeasureAdv = new double[2];
	private long dt = 1;
	private double Kp = 2;
	private double Ki = 1;
	private double Kd = 0;

	public ControllerPID(double Kp, double Ki, double Kd, int integralHistory) {
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
		this.INTEGRALHISTORY = integralHistory;
		if (integralHistory == -1) {
			useIntegralHistory = false;
		} else {
			useIntegralHistory = true;
			this.integralHistory = new double[2][INTEGRALHISTORY];
		}
	}

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints, long dtSec) {
		if (areAllSetpointsNull(setpoints))
			return false;
		for (int i = 0; i < setpoints.length; i++) {
			Double setpoint = setpoints[i];
			double measurement = Measurement(measurements[i], i);
			if (setpoint != null) {
				double error = setpoint - measurement;
				double integral = calculateIntegral(error, dtSec, i);
				double derivative = (error - perror[i]) / dtSec;
				double output = ((Kp * error) + (Ki * integral) + (Kd * derivative)) / 5;
				perror[i] = error;
				for (ParameterInterface<?> p : parameters) {
					if (p.getWeight() == 0) {
						p.set(0d);
					} else {
						double reduce = 1;

						switch (p.getName()) {
							case "Hash":
								reduce = 1;
								break;
							case "Sleep":
								reduce = 3;
								break;
							case "Depth":
								reduce = 0.01;
								break;
							case "Buttons":
								reduce = 0.01;
								break;
							case "Breadth":
								reduce = 0.01;
								break;
						}

						p.set(p.getDouble() + (output * (p.getWeight() * reduce)));
					}
				}
			}
		}
		return false;
	}

	private double Measurement(double measurement, int place) {
		if (RunningMeasurePlace[place] >= ADVRADGEBUFFER) {
			RunningMeasurePlace[place] = 0;
		}
		MeasureAdv[place] += measurement;
		MeasureAdv[place] -= RunningMeasure[place][RunningMeasurePlace[place]];
		RunningMeasure[place][RunningMeasurePlace[place]] = measurement;
		RunningMeasurePlace[place]++;
		if (totalMeasure[place] < ADVRADGEBUFFER)
			totalMeasure[place]++;
		return MeasureAdv[place] / totalMeasure[place];
	}

	private double calculateIntegral(double error, long dt, int place) {
		integral[place] += (error * dt);
		if (useIntegralHistory) {
			if (integralHistoryPlace[place] >= INTEGRALHISTORY) {
				integralHistoryPlace[place] = 0;
			}
			integral[place] -= integralHistory[place][integralHistoryPlace[place]];
			integralHistory[place][integralHistoryPlace[place]] = (error * dt);
			integralHistoryPlace[place]++;
		}
		return integral[place];
	}

	@Override
	public double[] get() {
		// TODO Auto-generated method stub
		return null;
	}

}