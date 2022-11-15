package com.example.JavaController;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MyFirstJavaControllerTests {
	
	MyFirstJavaController a = new MyFirstJavaController();
	
	@Test
	public void Test1() {
		//MyFirstController a = new MyFirstController();
		assertEquals("Addition = 100", a.Add(10, 90));
	}
	
	@Test
	public void Test2() {
		//MyFirstController a = new MyFirstController();
		assertEquals("Subtraction = 95", a.Subtract(100, 5));
	}
	
	@Test
	public void Test3() {
		//MyFirstController a = new MyFirstController();
		assertEquals("Multiplication = 500", a.Multiply(100, 5));
	}
	
	@Test
	public void Test4() {
		//MyFirstController a = new MyFirstController();
		assertEquals("Division = 9", a.Divide(90, 10));
	}

}
