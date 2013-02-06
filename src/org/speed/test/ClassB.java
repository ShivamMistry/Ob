package org.speed.test;

public class ClassB {

	public static void print3() {
		System.out.println("woo successful pass stage 3!");
		new ClassA().print4();
	}

}
