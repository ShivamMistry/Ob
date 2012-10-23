package com.speed.ob.transforms;

import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import com.speed.ob.NameGenerator;
import com.speed.ob.ObTransform;
import com.speed.ob.Obfuscate;

/**
 * Renames fields in a class, and any classes in the same JAR that reference it.
 * 
 * @author Shivam Mistry
 * 
 */
public class FieldRenamer extends ObTransform {

	public FieldRenamer(ClassGen cg) {
		super(cg);
	}

	public void execute() {
		NameGenerator nameGen = new NameGenerator();
		for (Field field : cg.getFields()) {
			ConstantPoolGen cpg = cg.getConstantPool();
			String originalName = field.getName();
			int index = field.getNameIndex();
			String newName = nameGen.next();
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
						if (nameIndex > -1) {
							ConstantUtf8 utf8 = (ConstantUtf8) cpg
									.getConstant(nameIndex);
							if (utf8 != null) {
								utf8.setBytes(newName);
							}

						}
					}
				}
			}
		}
	}
}
