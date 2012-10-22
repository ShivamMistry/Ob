package com.speed.ob.transforms;

import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import com.speed.ob.ObTransform;
import com.speed.ob.Obfuscate;

/**
 * Renames fields in a class, and any classes in the same JAR that reference it.
 * 
 * @author Shivam Mistry
 * 
 */
public class FieldRenamer extends ObTransform {

	private static final String[] NAME_TABLE = new String[] { "a", "b", "c",
			"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
			"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

	public FieldRenamer(ClassGen cg) {
		super(cg);
	}

	public static String getName(int index) {
		int letters = index / 26;
		if (letters == 0 || (letters == 1 && index % 25 == 0)) {
			return NAME_TABLE[index];
		} else {
			// we shall assume there are no more than 26^2 + 26 fields in the
			// class
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

	public void execute() {
		int f = 0;
		for (Field field : cg.getFields()) {
			ConstantPoolGen cpg = cg.getConstantPool();
			String originalName = field.getName();
			int index = field.getNameIndex();
			String newName = getName(f++);
			int newIndex = cpg.addUtf8(newName);
			cpg.setConstant(index, cpg.getConstant(newIndex));
			System.out.println("Renaming " + originalName + " to " + newName);
			if (Obfuscate.isCurrentlyJar()) {
				for (ClassGen clazz : Obfuscate.classes) {
					int fieldRef = clazz.getConstantPool().lookupFieldref(
							cg.getClassName(), originalName,
							field.getSignature());
					if (fieldRef > -1) {
						ConstantFieldref ref = (ConstantFieldref) clazz
								.getConstantPool().getConstant(fieldRef);
						ConstantNameAndType type = (ConstantNameAndType) clazz
								.getConstantPool().getConstant(
										ref.getNameAndTypeIndex());
						int nameIndex = type.getNameIndex();
						int newname = clazz.getConstantPool().addUtf8(newName);
						clazz.getConstantPool().setConstant(nameIndex,
								clazz.getConstantPool().getConstant(newname));
					}
				}
			}
		}
	}
}
