package triangulation;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

public class TriangulationTests {
	// Tests the method used to find the distances from the 3 known points. 
	@Test
	public void testGetDistance() {
		// Initialize 3 points
		Point2D p1 = new Point2D.Double(1.848,8.331);
		Point2D p2 = new Point2D.Double(10.241,9.463);
		Point2D p3 = new Point2D.Double(8.889,2.456);
		
		// Initialized the Triangulate object and load points and radii
		Triangulate test = new Triangulate();
		test.setPoints(p1, p2, p3);
		double err = 1e-4; 
		
		double[] dist = test.getDistance();
		double actual1 = 8.4690;
		double actual2 = 7.1362;
		double actual3 = 9.1701;
		
		assertEquals(actual1, dist[0], err);
		assertEquals(actual2, dist[1], err);
		assertEquals(actual3, dist[2], err);
	}
	
	// Tests getting the angle between the LOS of two points from wanted point
	@Test
	public void testGetInnerAngle() {
		double ang1 = 168.7810;
		double ang2 = 64.0945;
		double ang3 = 268.5473;
		
		// Set the angles for testing
		Triangulate test = new Triangulate();
		test.setAngles(ang1, ang2, ang3);
		double err = 1e-4; 
		
		double[] innerAng = test.getInnerAngle();
		double actual1 = Math.toRadians(104.6865);
		double actual2 = Math.toRadians(155.5472);
		double actual3 = Math.toRadians(99.7663);
		
		double sum = 0;
		for(int i = 0; i < innerAng.length; i++) sum += innerAng[i];
		
		assertEquals(actual1, innerAng[0], err);
		assertEquals(actual2, innerAng[1], err);
		assertEquals(actual3, innerAng[2], err);
		assertEquals(2*Math.PI, sum, err);
		
	}
	
	// Tests the localizing algorithm when 3 points known with 3 distances from those points
		@Test
		public void testTrilateration() {
			// Initialize 3 points
			Point2D p1 = new Point2D.Double(1,1);
			Point2D p2 = new Point2D.Double(6,3);
			Point2D p3 = new Point2D.Double(0,9);
			
			// Initialize 3 radius. r1 associated with p1 and so on.
			double r1 = 7.2111;
			double r2 = 4.1231;
			double r3 = 5.3852;
			
			// Initialized the Triangulate object and load points and radii
			Triangulate test = new Triangulate();
			test.setPoints(p1, p2, p3);
			test.setRadius(r1, r2, r3);
			
			// Set the given and actual location with the error we have
			Point2D loc = test.trilaterate();
			Point2D actual = new Point2D.Double(5, 7);
			double err = 1e-4; 
			
			// Test x and y coordinates  within a certain error
			assertEquals(actual.getX(), loc.getX(), err);
			assertEquals(actual.getY(), loc.getY(), err);

		}
		
		// Tests the final version of triangulation. Tests if location is found using angles and points
		@Test
		public void testFindLocation() {
			// Initialize 3 points
			Point2D p1 = new Point2D.Double(1.848,8.331);
			Point2D p2 = new Point2D.Double(10.241,9.463);
			Point2D p3 = new Point2D.Double(8.889,2.456);
			
			// Initialize 3 angles in degrees.
			double ang1 = 168.7810;
			double ang2 = 64.0945;
			double ang3 = 268.5473;
			
			// Initialized the Triangulate object and load points and radii
			Triangulate test = new Triangulate();
			test.setPoints(p1, p2, p3);
			test.setAngles(ang1, ang2, ang3);	
			// Set the given and actual location with the error we have
			Point2D loc = test.findLocation();
			Point2D actual = new Point2D.Double(9.002, 6.912);
			double err = 1e-4; 
			
			// Test x and y coordinates  within a certain error
			assertEquals(actual.getX(), loc.getX(), err);
			assertEquals(actual.getY(), loc.getY(), err);

		}

}
