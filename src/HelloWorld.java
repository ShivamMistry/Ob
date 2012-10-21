/**
 * A simple HelloWorld application used for testing the obfuscator.
 * 
 * @author Shivam Mistry
 * 
 */
public class HelloWorld {

	public static int x = 0;
	
	public static void main(String[] args) {
		System.out.println("Hello, world!");
		wat();
	}
	
	public static void wat() {
		x++;
		System.out.println("wat.");
		lol();
	}

	private static void lol() {
		x++;
		System.out.println("wat.");
		System.out.println("lol.");
	}

}
