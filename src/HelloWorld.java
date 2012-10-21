/**
 * A simple HelloWorld application used for testing the obfuscator.
 * 
 * @author Shivam Mistry
 * 
 */
public class HelloWorld {

	public int x = 0;
	private static final String HELLOW_ORLD = "Foo Bar";
	public static void main(String[] args) {
		System.out.println(HELLOW_ORLD);
		System.out.println("Hello, World!");
		System.out.println("This file shall be obfuscated");
		System.out.println(HELLOW_ORLD);
	}

}
