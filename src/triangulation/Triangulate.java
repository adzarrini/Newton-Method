package triangulation;

import java.awt.geom.Point2D;
import Jama.Matrix;

/* This code is written to find the location of a point when given 3 points.
 * Extra information is necessary. This extra information must be either 3 distances or 3 angles to the points.
 * Code is structured so that an object of triangulation is made and need variables are assigned through the constructor
 * 
 */

public class Triangulate {
	
	// Point locations of the 3 known positions
	private Point2D p1;
	private Point2D p2;
	private Point2D p3;
	
	// Distance away from 3 known positions
	private double r1;
	private double r2;
	private double r3;
	
	// Angle from an arbitrary measuring point to the Line of Sight (LOS) of know points
	private double ang1;
	private double ang2;
	private double ang3;
	
	public Triangulate() {
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		p3 = new Point2D.Double();
	}
	
	// This method must be called before 
	public void setPoints(Point2D p1, Point2D p2, Point2D p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;		
	}
	
	// This method must be called before 
	public void setRadius(double r1, double r2, double r3) {
		this.r1 = r1;
		this.r2 = r2;
		this.r3 = r3;
	}
		
	// This method must be called before 
	public void setAngles(double ang1, double ang2, double ang3) {
		this.ang1 = ang1;
		this.ang2 = ang2;
		this.ang3 = ang3;
	}
	
	public Point2D findLocation() {
		double[] ang = this.getInnerAngle();
		for (int i = 0; i < ang.length; i++) {
			ang[i] = Math.toRadians(ang[i]);
		}
		double tol = 1e-9;
		
		Matrix var = newtonMethod(tol);
		
		setRadius(var.get(0, 0), r2 = var.get(1, 0), r3 = var.get(2, 0));
		
		return trilaterate();
	}
	
	private Matrix newtonMethod(double tolerance) {
		double error = tolerance + 1;
		int counter = 0;
		Matrix jacobian = new Matrix(9,9);
		Matrix function = new Matrix(9,1);
		Matrix x = new Matrix(9,1,1);
		Matrix xnew = new Matrix(9,1);
		while (error > tolerance) {
			jacobian = evaluateJacobian(x);
			function = evaluateFunction(x);
			xnew = x.minus(jacobian.solve(function));
			error = getMaxAbs(xnew.minus(x));
			x = xnew.plus(new Matrix(9,1));
			counter++;
		}
		System.out.println("Newton Method iteration count of : " + counter);
		System.out.println("Maximum error from the solution: " + error + "\n");
		
		
		
		return xnew;
	}
	
	private Matrix evaluateJacobian(Matrix x0) {
		double[][] jacobian = new double[9][9];
		double[] x = new double[9];
		for(int i = 0; i < 9; i++) {
			x[i] = x0.get(i, 0);
		}
		double[] angleInner = getInnerAngle();
		double[] dist = getDistance();
		
		jacobian[0][0] = Math.sin(angleInner[0]); jacobian[0][4] = -dist[0]*Math.cos(x[4]);
		jacobian[1][1] = Math.sin(angleInner[0]); jacobian[1][3] = -dist[0]*Math.cos(x[3]);
		jacobian[2][1] = Math.sin(angleInner[1]); jacobian[2][6] = -dist[1]*Math.cos(x[6]);
		jacobian[3][2] = Math.sin(angleInner[1]); jacobian[3][5] = -dist[1]*Math.cos(x[5]);
		jacobian[4][2] = Math.sin(angleInner[2]); jacobian[4][8] = -dist[2]*Math.cos(x[8]);
		jacobian[5][0] = Math.sin(angleInner[2]); jacobian[5][7] = -dist[2]*Math.cos(x[7]);
		jacobian[6][4] = -2*dist[0]*dist[1]*Math.sin(x[4]+x[5]); jacobian[6][5] = -2*dist[0]*dist[1]*Math.sin(x[4]+x[5]);
		jacobian[7][6] = -2*dist[1]*dist[2]*Math.sin(x[6]+x[7]); jacobian[7][7] = -2*dist[1]*dist[2]*Math.sin(x[6]+x[7]); 
		jacobian[8][8] = -2*dist[2]*dist[0]*Math.sin(x[8]+x[3]); jacobian[8][3] = -2*dist[2]*dist[0]*Math.sin(x[8]+x[3]);
		
		return new Matrix(jacobian, 9, 9);
	}
	
	private Matrix evaluateFunction(Matrix x0) {
		double[][] function = new double[9][1];
		double[] x = new double[9];
		for(int i = 0; i < 9; i++) {
			x[i] = x0.get(i, 0);
		}
		double[] angleInner = getInnerAngle();
		double[] dist = getDistance();
		
		function[0][0] = x[0]*Math.sin(angleInner[0]) - dist[0]*Math.sin(x[4]);
		function[1][0] = x[1]*Math.sin(angleInner[0]) - dist[0]*Math.sin(x[3]);
		function[2][0] = x[1]*Math.sin(angleInner[1]) - dist[1]*Math.sin(x[6]);
		function[3][0] = x[2]*Math.sin(angleInner[1]) - dist[1]*Math.sin(x[5]);
		function[4][0] = x[2]*Math.sin(angleInner[2]) - dist[2]*Math.sin(x[8]);
		function[5][0] = x[0]*Math.sin(angleInner[2]) - dist[2]*Math.sin(x[7]);
		function[6][0] = Math.pow(dist[2],2) - Math.pow(dist[0],2) - Math.pow(dist[1],2) + 2*dist[0]*dist[1]*Math.cos(x[4]+x[5]);
		function[7][0] = Math.pow(dist[0],2) - Math.pow(dist[1],2) - Math.pow(dist[2],2) + 2*dist[1]*dist[2]*Math.cos(x[6]+x[7]);
		function[8][0] = Math.pow(dist[1],2) - Math.pow(dist[2],2) - Math.pow(dist[0],2) + 2*dist[2]*dist[0]*Math.cos(x[8]+x[3]);
		
		return new Matrix(function, 9, 1);
	}
	
	private double getMaxAbs(Matrix m) {
		double maxValue = Double.MIN_VALUE;
		for(int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				maxValue = Math.max(maxValue, Math.abs(m.get(i, j)));
			}
		}
		return maxValue;
	}
	
	
	// Finds the location of the unknown point given 3 points and a distance
	public Point2D trilaterate() {
		Point2D location = new Point2D.Double();
		double A = 2*(-p1.getX() + p2.getX());
		double B = 2*(-p1.getY() + p2.getY());
		double C = Math.pow(r1,2) - Math.pow(r2,2) - Math.pow(p1.getX(),2) + Math.pow(p2.getX(),2) - Math.pow(p1.getY(),2) + Math.pow(p2.getY(),2);
		double D = 2*(-p2.getX() + p3.getX());
		double E = 2*(-p2.getY() + p3.getY());
		double F = Math.pow(r2,2) - Math.pow(r3,2) - Math.pow(p2.getX(),2) + Math.pow(p3.getX(),2) - Math.pow(p2.getY(),2) + Math.pow(p3.getY(),2);
		double n = D/A;
		
		
		double yTemp = (F-n*C)/(E-n*B);
		location.setLocation((C/A) - (B/A)*yTemp, yTemp);

		return location;

	}
	
	// Method finds the angles between the LOS of the three points
	private double[] getInnerAngle(double[] ang) {
		double[] innerAng = new double[ang.length];
		
		for (int i = 0; i < ang.length; i++) {
			innerAng[i] = Math.abs(ang[i] - ang[(i+1) % ang.length]);
			if (innerAng[i] > 180) innerAng[i] = 360 - innerAng[i];
			innerAng[i] = Math.toRadians(innerAng[i]);
		}
		
		return innerAng;
	}
	
	public double[] getInnerAngle() {
		double[] ang = new double[3];
		ang[0] = ang1;
		ang[1] = ang2;
		ang[2] = ang3;
	
		return getInnerAngle(ang);
	}
	
	// Method finds the distance between each of the 3 points
	public double[] getDistance() {
		Point2D[] points = new Point2D.Double[3];
		points[0] = p1;
		points[1] = p2;
		points[2] = p3;
		
		return getDistance(points);
	}
	
	private double[] getDistance(Point2D[] points) {
		double[] distance = new double[points.length];
		for (int i = 0; i < points.length; i++) {
			distance[i] = Math.sqrt(Math.pow(points[i].getX()-points[(i+1)%points.length].getX(),2)+Math.pow(points[i].getY()-points[(i+1)%points.length].getY(),2));
		}
		
		return distance;
	}
	
	public static void main(String[] args) {
	}
	
	
}
