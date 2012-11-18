package com.speed.ob.test;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;

/**
 * Searches classes for a given string.
 * 
 * @author Shivam Mistry
 * 
 */
public class ClassSearcher {

	public ClassSearcher(String jarFile, String searchTerm) throws IOException {
		JarFile file = new JarFile(jarFile);
		Enumeration<JarEntry> entries = file.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".class")) {
				ClassParser parser = new ClassParser(
						file.getInputStream(entry), entry.getName());
				JavaClass jc = parser.parse();
				ConstantPool cp = jc.getConstantPool();
				for (Constant c : cp.getConstantPool()) {
					if (c != null && c.getTag() == Constants.CONSTANT_Utf8) {
						ConstantUtf8 utf8 = (ConstantUtf8) c;
						if (utf8.getBytes().contains(searchTerm)) {
							System.out.println(jc.getClassName() + " - "
									+ utf8.getBytes());
						}
					}
				}
			}
		}
		file.close();
	}

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				Scanner scanner = new Scanner(System.in);
				String a = scanner.nextLine();
				String b = scanner.nextLine();
				new ClassSearcher(a,b);
			}
			new ClassSearcher(args[0], args[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
