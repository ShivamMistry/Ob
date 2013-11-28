package com.speed.ob.transforms;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;

import com.speed.ob.NameGenerator;
import com.speed.ob.ObTransform;
import com.speed.ob.Obfuscate;

/**
 * Renames classes.
 * 
 * @author Shivam Mistry
 * 
 */
public class ClassRenamer extends ObTransform {

	// this is static so each instance doesn't just name it's class A
	private static NameGenerator nameGen = new NameGenerator();

	public ClassRenamer(ClassGen cg) {
		super(cg);
		Obfuscate.println("Starting class renamer on class " + cg.getClassName());
	}

	public void execute() {
		if (cg.containsMethod("main", Type.getMethodSignature(Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) })) != null) {
			// we usually want to prevent classes with main methods from being
			// renamed, breaks too many things
			Obfuscate.println(cg.getClassName() + " not renamed as contains as main method");
			return;
		}
		// create the new name for the class, leaves the packages intact.
		String className = cg.getClassName();
		int ind = className.lastIndexOf('.');
		String newName;
		if (ind > -1) {
			newName = className.substring(0, className.lastIndexOf('.')) + '.' + nameGen.next();
		} else {
			newName = nameGen.next();
		}
		cg.setClassName(newName);
		// sets the class name in the actual class file
		String fileName = cg.getFileName();
		int ut = cg.getConstantPool().lookupUtf8(fileName);
		if (ut > -1) {
			// changes the source file attribute
			ConstantUtf8 c = (ConstantUtf8) cg.getConstantPool().getConstant(ut);
			Obfuscate.println("\trenamed source file: " + fileName + " to " + nameGen.current() + ".java");
			c.setBytes(nameGen.current() + ".java");
		}
		// fix references to the class
		fixConstantPool(cg, className, newName);
		Obfuscate.println("\t" + className + " renamed to " + newName);
		if (Obfuscate.isCurrentlyJar()) {
			for (ClassGen c : Obfuscate.classes) {
				ConstantPoolGen cpg = c.getConstantPool();
				fixConstantPool(c, className, newName);
				int index = cpg.lookupClass(className);
				if (index > -1) {
					ConstantClass con = (ConstantClass) cpg.getConstant(index);
					int utf = con.getNameIndex();
					ConstantUtf8 utf8 = (ConstantUtf8) cpg.getConstant(utf);
					utf8.setBytes(newName.replace(".", "/"));
					Obfuscate.println("\t" + className + " renamed to " + newName + " in class " + c.getClassName());
				}
			}
		}
	}

	private void fixConstantPool(ClassGen cg, String className, String newName) {
		ConstantPoolGen cpg = cg.getConstantPool();
		newName = newName.replace('.', '/');
		for (Constant c : cpg.getConstantPool().getConstantPool()) {
			if (c instanceof ConstantUtf8) {
				ConstantUtf8 con = (ConstantUtf8) c;
				String className1 = className.replace('.', '/');
				if (con.getBytes().contains("L" + className1 + ";")) {
					Obfuscate.println("\treplacing " + con.getBytes());
					String bytes = con.getBytes().replace('L' + className1 + ';', 'L' + newName + ';');
					con.setBytes(bytes);
				}
			}
		}
	}
}
