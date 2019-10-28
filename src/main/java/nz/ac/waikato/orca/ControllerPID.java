package nz.ac.waikato.orca;

public class ControllerPID extends ControllerHead {
	public final int INTEGRALHISTORY = 30;
	private double perror = 0;
	private double integral = 0;
	private double[] integralHistory = new double[INTEGRALHISTORY];
	private int integralHistoryPlace = 0;
	private long dt = 0;
	private int Kp = 0;
	private int Ki = 0;
	private int Kd = 0;

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints) {
		if (areAllSetpointsNull(setpoints))
			return false;
		for (int i = 0; i < setpoints.length; i++) {
			Double setpoint = setpoints[i];
			double measurement = measurements[i];
			if (setpoint != null) {
				double error = setpoint - measurement;
				double integral = calculateIntegral(error, dt);
				double derivative = (error - perror) / dt;
				double output = (Kp * error) + (Ki * integral) + (Kd * derivative);
				perror = error;
				for (ParameterInterface<?> p : parameters) {
					p.set(output);
				}
			}
		}
		return false;
	}

	private double calculateIntegral(double error, long dt) {
		integral = integral + error * dt;
		if (integralHistoryPlace == INTEGRALHISTORY) {
			integralHistoryPlace = 0;
		}
		integral -= integralHistory[integralHistoryPlace];
		integralHistory[integralHistoryPlace] = integral;
		integralHistoryPlace++;
		return integral;
	}

	@Override
	public double[] get() {
		// TODO Auto-generated method stub
		return null;
	}

}