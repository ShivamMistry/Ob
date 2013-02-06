package org.speed.test;

public class ClassA {
	public ClassA() {

	}
	
	public void print1() {
		System.out.println("woo successful pass stage 1!");
		new ClassA().print2();
	}
	
	private void print2() {
		System.out.println("woo successful pass stage 2!");

	}
	
	protected void print4(){
		System.out.println("woo successful pass stage 4!");
	}
}
