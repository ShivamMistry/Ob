package A;

/**
 * Encrypts/decrypts strings
 * 
 * @author Shivam Mistry
 * 
 */
public final class A {
	public final static String decrypt(String encrypted) {
		StringBuilder builder = new StringBuilder();
		byte xor = (byte) (encrypted.length() & 0xFF);
		for (int pos = 0; pos < encrypted.length(); pos++) {
			builder.append((char) (encrypted.charAt(pos) ^ xor));
			xor ^= pos;
		}
		return builder.toString().intern();
	}
}
