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
	}

	public void execute() {
		if (cg.containsMethod("main", Type.getMethodSignature(Type.VOID,
				new Type[] { new ArrayType(Type.STRING, 1) })) != null) {
			System.out.println(cg.getClassName()
					+ " not renamed as contains as main method");
			return;
		}
		String className = cg.getClassName();
		int ind = className.lastIndexOf('.');
		String newName;
		if (ind > -1) {
			newName = className.substring(0, className.lastIndexOf('.')) + '.'
					+ nameGen.next();
		} else {
			newName = nameGen.next();
		}
		cg.setClassName(newName);
		fixConstantPool(cg, className, newName);
		System.out.println("\t" + className + " renamed to " + newName);
		if (Obfuscate.isCurrentlyJar())
			for (ClassGen c : Obfuscate.classes) {
				ConstantPoolGen cpg = c.getConstantPool();
				fixConstantPool(c, className, newName);
				int index = cpg.lookupClass(className);
				if (index > -1) {
					ConstantClass con = (ConstantClass) cpg.getConstant(index);
					int utf = con.getNameIndex();
					ConstantUtf8 utf8 = (ConstantUtf8) cpg.getConstant(utf);
					utf8.setBytes(newName);
					System.out.println("\t" + className + " renamed to "
							+ newName + " in class " + c.getClassName());
				}
			}

	}

	private void fixConstantPool(ClassGen cg, String className, String newName) {
		ConstantPoolGen cpg = cg.getConstantPool();
		for (Constant c : cpg.getConstantPool().getConstantPool()) {
			if (c instanceof ConstantUtf8) {
				ConstantUtf8 con = (ConstantUtf8) c;
				String className1 = className.replace('.', '/');
				if (con.getBytes().contains("L" + className1 + ";")) {
					System.out.println("\treplacing " + con.getBytes());
					String bytes = con.getBytes().replace(
							'L' + className1 + ';',
							'L' + newName.replace('.', '/') + ';');
					con.setBytes(bytes);
				} else if (con.getBytes().contains(className1)) {
					System.out.println("\treplacing " + con.getBytes());
					String bytes = con.getBytes().replace(className1,
							newName.replace('.', '/'));
					con.setBytes(bytes);
				}
			}
		}
	}
}
