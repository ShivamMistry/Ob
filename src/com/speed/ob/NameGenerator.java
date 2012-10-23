package com.speed.ob;

/**
 * Generates names for classes, fields and methods.
 * 
 * @author Shivam Mistry
 * 
 */
public class NameGenerator {

	private int currentIndex;

	private static final String[] NAME_TABLE = new String[] { "a", "b", "c",
			"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
			"q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public NameGenerator() {
		reset();
	}

	public NameGenerator(int startIndex) {
		this.currentIndex = startIndex;
	}

	protected static String getName(int index) {
		int letters = index / NAME_TABLE.length;
		if (letters == 0
				|| (letters == 1 && index % NAME_TABLE.length - 1 == 0)) {
			return NAME_TABLE[index];
		} else {
			// we shall assume there are no more than 52^2 + 52 names that need
			// generating
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < NAME_TABLE.length; i++) {
				if ((index - NAME_TABLE.length - NAME_TABLE.length * i) >= 0
						&& (index - NAME_TABLE.length - NAME_TABLE.length * i) < NAME_TABLE.length) {
					sb.append(NAME_TABLE[i]);
					int secondIndex = (index - NAME_TABLE.length)
							- (NAME_TABLE.length * i);
					sb.append(NAME_TABLE[secondIndex]);
				}
			}
			return sb.toString();
		}
	}

	public void reset() {
		currentIndex = 0;
	}

	public String next() {
		return getName(currentIndex++);
	}

	public String current() {
		return currentIndex == 0 ? null : getName(currentIndex - 1);
	}

}
