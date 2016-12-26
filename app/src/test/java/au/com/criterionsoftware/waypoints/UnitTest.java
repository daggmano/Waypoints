package au.com.criterionsoftware.waypoints;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnitTest {

	@Test
	public void calculatePoints_isCorrect_1() throws Exception {
		CalculatePointsResult result = DistanceBlipCalculator.calculatePoints(3.4, 0);

		assertEquals(0.6, result.remainder, 0.0001);
		assertEquals(3, result.points.length);

		double expected[] = {1, 2, 3};
		assertArrayEquals(expected, result.points, 0.0001);
	}

	@Test
	public void calculatePoints_isCorrect_2() throws Exception {
		CalculatePointsResult result = DistanceBlipCalculator.calculatePoints(3.4, 0.5);

		assertEquals(0.1, result.remainder, 0.0001);
		assertEquals(3, result.points.length);

		double expected[] = {0.5, 1.5, 2.5};
		assertArrayEquals(expected, result.points, 0.0001);
	}

	@Test
	public void calculatePoints_isCorrect_3() throws Exception {
		CalculatePointsResult result = DistanceBlipCalculator.calculatePoints(3.4, 0.3);

		assertEquals(0.9, result.remainder, 0.0001);
		assertEquals(4, result.points.length);

		double expected[] = {0.3, 1.3, 2.3, 3.3};
		assertArrayEquals(expected, result.points, 0.0001);
	}

	@Test
	public void calculatePoints_isCorrect_4() throws Exception {
		CalculatePointsResult result = DistanceBlipCalculator.calculatePoints(3, 0);

		assertEquals(0, result.remainder, 0.0001);
		assertEquals(3, result.points.length);

		double expected[] = {1, 2, 3};
		assertArrayEquals(expected, result.points, 0.0001);
	}

	@Test
	public void calculatePoints_isCorrect_5() throws Exception {
		CalculatePointsResult result = DistanceBlipCalculator.calculatePoints(0.2, 0.8);

		assertEquals(0.6, result.remainder, 0.0001);
		assertEquals(0, result.points.length);
	}

}
