package com.speed.ob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

import com.speed.encrypt.StringEncrypter;

/**
 * Runs the obfuscation transforms on code.
 * 
 * @author Shivam Mistry
 * 
 */
public class Obfuscate {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java com.speed.ob.Obfuscate [files...]");
		} else {
			for (int i = 0; i < args.length; i++) {
				String fileName = args[i];
				File file = new File(fileName);
				if (!file.exists()) {
					System.out.println(fileName + " doesn't exist!");
				} else {
					if (fileName.endsWith(".class")) {
						transformClass(fileName);
					} else if (fileName.endsWith(".jar")) {
						try {
							transformJar(new JarFile(file));
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println(fileName + " not supported.");
					}
				}
			}
		}
	}

	private static void transformJar(JarFile jarFile) {
		Enumeration<JarEntry> en = jarFile.entries();
		JarOutputStream out = null;
		try {
			out = new JarOutputStream(new FileOutputStream(jarFile.getName()
					.replace(".jar", "-ob.jar")), jarFile.getManifest());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (en.hasMoreElements()) {
			JarEntry entry = en.nextElement();
			try {
				JarEntry newEntry = new JarEntry(entry.getName());
				out.putNextEntry(newEntry);
				ClassParser cp = new ClassParser(jarFile.getInputStream(entry),
						entry.getName());
				JavaClass jc = cp.parse();
				ClassGen cg = new ClassGen(jc);
				new StringEncrypter(cg).execute();
				out.write(cg.getJavaClass().getBytes());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void transformClass(String fileName) {
		ClassParser cp = new ClassParser(fileName);
		try {
			JavaClass jc = cp.parse();
			ClassGen cg = new ClassGen(jc);
			new StringEncrypter(cg).execute();
			new File(fileName).renameTo(new File(fileName.replace(".class",
					"_bak.class")));
			cg.getJavaClass().dump(fileName);
		} catch (ClassFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
