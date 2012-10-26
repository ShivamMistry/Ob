package com.speed.ob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

import com.speed.ob.transforms.ClassRenamer;
import com.speed.ob.transforms.FieldRenamer;
import com.speed.ob.transforms.StringEncrypter;

/**
 * Runs the obfuscation transforms on code.
 * 
 * @author Shivam Mistry
 * 
 */
public class Obfuscate {

	public static List<ClassGen> classes;
	public static List<JarEntry> entries;
	private static final Class<?>[] TRANSFORMS = new Class<?>[] {
			StringEncrypter.class, /* ControlFlowTransform.class, */
			FieldRenamer.class, ClassRenamer.class };
	private static boolean currentlyJar;

	public static boolean isCurrentlyJar() {
		return currentlyJar;
	}

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
						currentlyJar = false;
						transformClass(fileName);
					} else if (fileName.endsWith(".jar")) {
						try {
							currentlyJar = true;
							classes = new ArrayList<ClassGen>();
							entries = new ArrayList<JarEntry>();
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
					.replace(".jar", "-ob.jar")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (en.hasMoreElements()) {
			JarEntry entry = en.nextElement();
			try {
				JarEntry newEntry = new JarEntry(entry.getName());
				if (entry.getName().endsWith(".class")) {
					ClassParser cp = new ClassParser(
							jarFile.getInputStream(entry), entry.getName());
					JavaClass jc = cp.parse();
					ClassGen cg = new ClassGen(jc);
					entries.add(newEntry);
					classes.add(cg);
				} else {
					out.putNextEntry(newEntry);
					byte[] buffer = new byte[1024];
					InputStream in = jarFile.getInputStream(entry);
					int read;
					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
					out.flush();
					out.closeEntry();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < classes.size(); i++) {
			ClassGen cg = classes.get(i);
			for (Class<?> cl : TRANSFORMS) {
				try {
					ObTransform transform = (ObTransform) cl.getConstructor(
							ClassGen.class).newInstance(cg);
					transform.execute();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(cl.getCanonicalName()
							+ " failed to transform class: "
							+ cg.getClassName());
				}

			}

			// new ControlFlowTransform(cg).execute();
		}
		for (int i = 0; i < classes.size(); i++) {
			ClassGen cg = classes.get(i);
			// JarEntry newEntry = entries.get(i);
			String name = cg.getClassName().replace('.', '/').concat(".class");
			JarEntry entry2 = new JarEntry(name);
			try {
				out.putNextEntry(entry2);
				out.write(cg.getJavaClass().getBytes());
				out.flush();
				out.closeEntry();
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
			for (Class<?> clazz : TRANSFORMS) {
				ObTransform transform = (ObTransform) clazz.getConstructor(
						ClassGen.class).newInstance(cg);
				transform.execute();
			}
			// new ControlFlowTransform(cg).execute();
			new File(fileName).renameTo(new File(fileName.replace(".class",
					"_bak.class")));
			cg.getJavaClass().dump(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
