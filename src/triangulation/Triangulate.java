package triangulation;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;

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
		
		//Function f = new F
		
		
		return new Point2D.Double(0,0);
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
	public double[] getInnerAngle(double[] ang) {
		double[] innerAng = new double[ang.length];
		
		for (int i = 0; i < ang.length; i++) {
			innerAng[i] = Math.abs(ang[i] - ang[(i+1) % ang.length]);
			if (innerAng[i] > 180) innerAng[i] = 360 - innerAng[i];
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
	
	public double[] getDistance(Point2D[] points) {
		double[] distance = new double[points.length];
		for (int i = 0; i < points.length; i++) {
			distance[i] = Math.sqrt(Math.pow(points[i].getX()-points[(i+1)%points.length].getX(),2)+Math.pow(points[i].getY()-points[(i+1)%points.length].getY(),2));
		}
		
		return distance;
	}
	
	public static void main(String[] args) {

	}
	
	
}
