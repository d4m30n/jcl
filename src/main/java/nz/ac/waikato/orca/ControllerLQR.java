package nz.ac.waikato.orca;

import org.ejml.simple.SimpleMatrix;

import com.mccarthy.control.LQR;
import com.mccarthy.control.SS;
import com.mccarthy.control.UnableToEvaluateStateSolution;

public class ControllerLQR extends ControllerHead {

	private SS sys;
	private LQR stateSolution;
	private SimpleMatrix Q;
	private SimpleMatrix R;

	private SimpleMatrix _x;
	private SimpleMatrix _y = null;
	private SimpleMatrix _u;

	private int[] _uIDs;

	private boolean start = true;

	/**
	 * Puts together the required elements for an LQR controller
	 * 
	 * @param A    - The A matrix
	 * @param B    - The B matrix
	 * @param C    - The C matrix
	 * @param D    - The D matrix
	 * @param K    - The K matrix
	 * @param x    - The starting x matrix
	 * @param u    - The starting u matrix
	 * @param uIDs - A list of parameter IDs for the given parameters in u. NOTE:
	 *             the order of the IDs must be the same as u
	 * @throws UnableToEvaluateStateSolution
	 * @throws Exception                     - If any of the matrix do not match or
	 *                                       they are null then an exception is
	 *                                       thrown
	 */
	public ControllerLQR(double[][] A, double[][] B, double[][] C, double[][] D, double[][] Q, double[][] R, double[] x,
			double[] u, int[] uIDs) throws UnableToEvaluateStateSolution {

		// Creates the x and u matrix
		double[][] tmpx = new double[x.length][1];
		double[][] tmpu = new double[u.length][1];
		for (int i = 0; i < x.length; i++) {
			tmpx[i][0] = x[i];
		}
		for (int i = 0; i < u.length; i++) {
			tmpu[i][0] = Math.log(u[i]);
		}
		sys = new SS(new SimpleMatrix(A), new SimpleMatrix(B), new SimpleMatrix(C), new SimpleMatrix(D));
		this.Q = new SimpleMatrix(Q);
		this.R = new SimpleMatrix(R);
		stateSolution = new LQR(this.sys, this.Q, this.R);
		_x = new SimpleMatrix(tmpx);
		_u = new SimpleMatrix(tmpu);
		_x = sys.stepSystem(_x, _u);
		_y = sys.getOutputVector(_x, _u);
		_uIDs = uIDs;
	}

	public void UpdateB(double[][] b) throws UnableToEvaluateStateSolution {
		SimpleMatrix B = new SimpleMatrix(b);
		SS newSys = new SS(sys.copyA(), B, sys.copyC(), sys.copyD());
		LQR newStateSolution = new LQR(newSys, this.Q, this.R);
		this.sys = newSys;
		this.stateSolution = newStateSolution;
	}

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints, long dtSec) {
		if (areAllSetpointsNull(setpoints))
			return false;
		double[][] r = new double[2][1];
		r[0][0] = setpoints[0];
		r[1][0] = setpoints[1];
		SimpleMatrix _r = new SimpleMatrix(r);
		_x = sys.stepSystem(_x, _u);
		_y = sys.getOutputVector(_x, _u);
		_u = _u.minus(stateSolution.getK().negative().mult(_r.minus(_x)));
		for (int i = 0; i < _u.numRows(); i++) {
			for (ParameterInterface<?> p : parameters) {
				if (p.getID() == _uIDs[i]) {
					p.set(ModelLQR.decodeParameter(_u.get(i, 0)));
					_u.set(i, 0, ModelLQR.encodeParameter(p.getDouble()));
				}
			}
		}
		return true;
	}

	@Override
	public double[] get() {
		if (_y == null)
			return null;
		double[] returnValue = new double[_y.numRows()];
		for (int i = 0; i < _y.numRows(); i++) {
			returnValue[i] = _y.get(i, 0);
		}
		return returnValue;
	}

}