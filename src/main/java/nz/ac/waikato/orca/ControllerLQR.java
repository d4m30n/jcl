package nz.ac.waikato.orca;

import org.ejml.simple.SimpleMatrix;
import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

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
	public ControllerLQR(double[][] A, double[][] B, double[][] C, double[][] D, double[][] Q, double[][] R, double[] x,
			double[] u, int[] uIDs) throws Exception {
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
		else if (Q.length != A.length && Q[0].length != A[0].length) {
			throw new Exception("Q must have the same number of rows and cols as A");
		} else if (B[0].length != u.length)
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
		else if (R.length != u.length && R[0].length != u.length) {
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
		_A = new SimpleMatrix(A);
		_B = new SimpleMatrix(B);
		_C = new SimpleMatrix(C);
		_D = new SimpleMatrix(D);
		_K = evaluateK(A, B, Q, R);
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

	private SimpleMatrix evaluateK(double[][] A, double[][] B, double[][] Q, double[][] R) {
		try {
			int port = 8845;
			JSONObject obj = new JSONObject();
			JSONArray Aarray = new JSONArray();
			JSONArray Barray = new JSONArray();
			JSONArray Qarray = new JSONArray();
			JSONArray Rarray = new JSONArray();
			JSONArray tmp = new JSONArray();
			for (int r = 0; r < A.length; r++) {
				JSONArray listtmp = new JSONArray();
				for (int c = 0; c < A[0].length; c++) {
					listtmp.add(A[r][c]);
				}
				tmp.add(listtmp);

			}
			Aarray.add(tmp);
			tmp = new JSONArray();
			for (int r = 0; r < B.length; r++) {
				JSONArray listtmp = new JSONArray();
				for (int c = 0; c < B[0].length; c++) {
					listtmp.add(B[r][c]);
				}
				tmp.add(listtmp);
			}
			Barray.add(tmp);
			tmp = new JSONArray();
			for (int r = 0; r < Q.length; r++) {
				JSONArray listtmp = new JSONArray();
				for (int c = 0; c < Q[0].length; c++) {
					listtmp.add(Q[r][c]);
				}
				tmp.add(listtmp);
			}
			Qarray.add(tmp);
			tmp = new JSONArray();
			for (int r = 0; r < R.length; r++) {
				JSONArray listtmp = new JSONArray();
				for (int c = 0; c < R[0].length; c++) {
					listtmp.add(R[r][c]);
				}
				tmp.add(listtmp);

			}
			Rarray.add(tmp);
			obj.put("A", Aarray);
			obj.put("B", Barray);
			obj.put("Q", Qarray);
			obj.put("R", Rarray);
			Socket send = new Socket("127.0.0.1", port);
			BufferedOutputStream dout = new BufferedOutputStream(send.getOutputStream());
			byte[] outdata = obj.toString().getBytes();
			byte[] datasize = new byte[4];
			datasize = ByteBuffer.allocate(4).putInt(outdata.length).array();
			dout.write(datasize);
			dout.flush();
			dout.write(outdata);
			dout.flush();
			DataInputStream din = new DataInputStream(send.getInputStream());
			byte[] indatasize = new byte[4];
			din.read(indatasize);
			int recevesize = ByteBuffer.wrap(indatasize).getInt();
			byte[] recevedata = new byte[recevesize];
			din.read(recevedata);
			send.close();
			String jsonK = new String(recevedata);
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(jsonK);
			JSONArray Karray = (JSONArray) json.get("K");
			JSONArray KarrayInner = (JSONArray) Karray.get(0);
			double[][] K = new double[Karray.size()][KarrayInner.size()];
			for (int r = 0; r < K.length; r++) {
				JSONArray innerK = (JSONArray) Karray.get(r);
				for (int c = 0; c < K[0].length; c++) {
					K[r][c] = (double) innerK.get(c);
				}
			}
			SimpleMatrix rK = new SimpleMatrix(K);

			// JSONParser parse = new JSONParser(returnK);
			// JSONArray array = (JSONArray)parse.parse(returnK);

			return rK;
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(-1);
			return null;
		}
	}

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints) {
		if (areAllSetpointsNull(setpoints))
			return false;
		double[][] r = new double[2][1];
		r[0][0] = setpoints[0];
		r[1][0] = setpoints[1];
		// _x.set(0, 0, Math.log(measurements[0]) - intercepts[0]);
		// _x.set(1, 0, Math.log(measurements[1]) - intercepts[1]);
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
					p.set(ModelLQR.decodeParameter(_u.get(i, 0)));
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