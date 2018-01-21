package triangulation;

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
	private double a;
	private double b;
	private double c;
	
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
	public void setAngles(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public Point2D findLocation() {
		
		return new Point2D.Double(0,0);
	}
	
	public static void main(String[] args) {
		
	}
	
	
}
