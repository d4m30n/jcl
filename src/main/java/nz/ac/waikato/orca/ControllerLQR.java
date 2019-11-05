package nz.ac.waikato.orca;

import org.ejml.simple.SimpleMatrix;
import org.python.util.PythonInterpreter;
import org.python.core.PyArray;

public class ControllerLQR extends ControllerHead {

	// private double[][] _A;
	// private double[][] _B;
	// private double[][] _C;
	// private double[][] _D;

	// private double[][] _negativeK;

	// private double[][] _x;
	// private double[][] _y;
	// private double[][] _u;

	private SimpleMatrix _A;
	private SimpleMatrix _B;
	private SimpleMatrix _C;
	private SimpleMatrix _D;

	private SimpleMatrix _K;

	private SimpleMatrix _x;
	private SimpleMatrix _y = null;
	private SimpleMatrix _u;

	private int[] _uIDs;

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
	 * @throws Exception - If any of the matrix do not match or they are null then
	 *                   an exception is thrown
	 */
	public ControllerLQR(double[][] A, double[][] B, double[][] C, double[][] D, double[][] Q, double[][] R, double[] x, double[] u,
			int[] uIDs) throws Exception {
		// Checks to see if any of the matrixes are null
		if (A == null)
			throw new Exception("A matrix is required");
		else if (B == null)
			throw new Exception("B matrix is required");
		else if (C == null)
			throw new Exception("C matrix is required");
		else if (D == null)
			throw new Exception("D matrix is required");
		else if (Q == null)
			throw new Exception("Q matrix is required");
		else if (R == null)
			throw new Exception("R matrix is required");
		else if (x == null)
			throw new Exception("x matrix is required");
		else if (u == null)
			throw new Exception("u matrix is required");
		else if (uIDs == null)
			throw new Exception(
					"A list of parameter ID is required inorder to allocate the new parameter values in the controller");

		// Validates the length of the matrixes
		if (A.length != A[0].length)
			throw new Exception("A must be a square matrix");
		else if (A[0].length != x.length)
			throw new Exception("A must have the same number of rows as x");
		else if (Q.length != A.length && Q[0].length != A[0].length){
			throw new Exception("Q must have the same number of rows and cols as A");
		}
		else if (B[0].length != u.length)
			throw new Exception("B must have the same number of rows as u");
		else if (B.length != x.length)
			throw new Exception("B must have the same number of cols as x");

		if (C.length != C[0].length)
			throw new Exception("C must be a square matrix");
		else if (C[0].length != x.length)
			throw new Exception("C must have the same number of rows as x");
		else if (D[0].length != u.length)
			throw new Exception("D must have the same number of rows as u");
		else if (D.length != x.length)
			throw new Exception("D must have the same number of cols as x");
		else if (u.length != uIDs.length)
			throw new Exception("There must be the same cols in uIDs and u");
		else if (R.length != u.length && R[0].length != u.length){
			throw new Exception("R must have the same number of rows as u and must be a square matrix");
		}

		// Creates the x and u matrix
		double[][] tmpx = new double[x.length][1];
		double[][] tmpu = new double[u.length][1];
		for (int i = 0; i < x.length; i++) {
			tmpx[i][0] = x[i];
		}
		for (int i = 0; i < u.length; i++) {
			tmpu[i][0] = Math.log(u[i]);
		}
		double[][] K;
		try(PythonInterpreter py = new PythonInterpreter()){
			PyArray pA = new PyArray(PyArray,A.length);
			PyArray pB = new PyArray(PyArray,B.length);
			PyArray pQ = new PyArray(PyArray,Q.length);
			PyArray pR = new PyArray(PyArray,R.length);
			py.exec("from control.matlab import *");
			py.exec("import numpy as np");
		}
		// _A = A;
		// _B = B;
		// _C = C;
		// _D = D;
		// _negativeK = mult(K, -1);
		// _x = tmpx;
		// _u = tmpu;

		_A = new SimpleMatrix(A);
		_B = new SimpleMatrix(B);
		_C = new SimpleMatrix(C);
		_D = new SimpleMatrix(D);
		_K = new SimpleMatrix(K);
		_x = new SimpleMatrix(tmpx);
		_u = new SimpleMatrix(tmpu);
		_uIDs = uIDs;
		_A.print();
		_B.print();
		_C.print();
		_D.print();
		_x.print();
		_u.print();
		_K.print();
	}

	private double[] intercepts = { 3.379724, 4.107224 };

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints) {
		if (areAllSetpointsNull(setpoints))
			return false;
		double[][] r = new double[setpoints.length][1];
		for (int i = 0; i < setpoints.length; i++) {
			if (setpoints[i] == null) {
				setpoints[i] = 0d;
			}
			r[i][0] = setpoints[i];
		}
		//_x.set(0, 0, Math.log(measurements[0]) - intercepts[0]);
		//_x.set(1, 0, Math.log(measurements[1]) - intercepts[1]);
		SimpleMatrix _r = new SimpleMatrix(r);
		// _x = add(mult(_A, _x), mult(_B, _u));
		// _y = add(mult(_C, _x), mult(_D, _u));
		// _u = subtract(_u, mult(_negativeK, subtract(r, _x)));
		_x = (_A.mult(_x)).plus(_B.mult(_u));
		_y = (_C.mult(_x)).plus(_D.mult(_u));
		_u = _u.minus(_K.negative().mult(_r.minus(_x)));
		for (int i = 0; i < _u.numRows(); i++) {
			for (ParameterInterface<?> p : parameters) {
				if (p.getID() == _uIDs[i]) {
					p.set(Math.exp(_u.get(i, 0)));
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

	/**
	 * Adds two matrices together
	 * http://www.javawithus.com/programs/matrix-addition-and-subtraction
	 * 
	 * @param a - The first matrix to add
	 * @param b - The second matrix to add
	 * @return - Returns the new matrix or null if null was passed in as either
	 *         parameter
	 */
	private double[][] add(double[][] a, double[][] b) {
		if (a == null || b == null)
			return null;
		int rows = a.length;
		int columns = a[0].length;
		double[][] result = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result[i][j] = a[i][j] + b[i][j];
			}
		}
		return result;
	}

	/**
	 * Subtract two matrices together
	 * http://www.javawithus.com/programs/matrix-addition-and-subtraction
	 * 
	 * @param a - The first matrix to subtract
	 * @param b - The second matrix to subtract
	 * @return - The new matrix or null if either of the parameters are passed as
	 *         null
	 */
	private double[][] subtract(double[][] a, double[][] b) {
		if (a == null || b == null)
			return null;
		int rows = a.length;
		int columns = a[0].length;
		double[][] result = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result[i][j] = a[i][j] - b[i][j];
			}
		}
		return result;
	}

	/**
	 * Multiply two matrices together
	 * http://www.javawithus.com/programs/matrix-multiplication
	 * 
	 * @param a - The first matrix to multiply
	 * @param b - The second matrix to multiply
	 * @return - The multiplication of both matrices or null if a null parameter is
	 *         passed
	 */
	private double[][] mult(double[][] a, double[][] b) {
		if (a == null || b == null)
			return null;
		if (a[0].length != b.length)
			return null;
		int rowsInA = a.length;
		int columnsInA = a[0].length; // same as rows in B
		int columnsInB = b[0].length;
		double[][] c = new double[rowsInA][columnsInB];
		for (int i = 0; i < rowsInA; i++) {
			for (int j = 0; j < columnsInB; j++) {
				for (int k = 0; k < columnsInA; k++) {
					c[i][j] = c[i][j] + a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}

	/**
	 * The multiplication of a matrix with a single value
	 * 
	 * @param a - The first matrix to multiply
	 * @param b - The double to multiply the matrix with
	 * @return - The return matrix once multiplyed or null if a null value is passed
	 */
	private double[][] mult(double[][] a, double b) {
		if (a == null)
			return null;
		double[][] c = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				c[i][j] = a[i][j] * b;
			}
		}
		return c;
	}

}