package org.speed.test;

public class HelloWorld {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Woo starting up");
		System.out.println("Loading and creating instance of A");
		new ClassA().print1();
		ClassB.print3();
	}

}
