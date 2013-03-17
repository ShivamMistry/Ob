package com.speed.ob.transforms;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;

import com.speed.ob.NameGenerator;
import com.speed.ob.ObTransform;
import com.speed.ob.Obfuscate;

import edu.umd.cs.findbugs.ba.generic.GenericSignatureParser;

public class MethodRenamer extends ObTransform {
	private NameGenerator names;

	public MethodRenamer(ClassGen cg) {
		super(cg);
	}

	public void execute() {
		names = new NameGenerator();
		Obfuscate.println("Starting method renamer on " + cg.getClassName());
		// lets rename abstract class methods and interface methods first
		for (Method m : cg.getMethods()) {
			if (m.isNative() || m.getName().equals("<clinit>") || m.getName().equals("<init>")
					|| m.getName().equals("main")) {
				// we dont want to mess with native methods, constructors or
				// static initialisers
				continue;
			}
			// look through hierarchy and do not rename any overriden methods
			if (lookThroughHierarchy(cg, m, "")) {
				Obfuscate.println("\tNot renaming: " + m.getName());
				continue;
			}
			// generate a new name
			String oldName = m.getName();
			String newName = names.next();
			Obfuscate.println("\tRenaming " + oldName + " to " + newName);
			// fix the references in this class first
			int utf8 = m.getNameIndex();
			if (utf8 > -1) {
				ConstantUtf8 utf = (ConstantUtf8) cg.getConstantPool().getConstant(utf8);
				// set the new name
				if (utf.getBytes().equals(oldName))
					utf.setBytes(newName);
			}
			fixClass(cg, cg.getClassName(), oldName, newName, m);
			// fix references to this method in the other classes
			for (ClassGen clazz : Obfuscate.classes) {
				fixClass(clazz, cg.getClassName(), oldName, newName, m);
			}
		}
	}

	private void fixClass(final ClassGen cg, String clazzName, final String oldName, final String newName,
			final Method m) {
		int index = cg.getConstantPool().lookupMethodref(clazzName, oldName, m.getSignature());
		// look for the method reference
		if (index > -1) {
			Obfuscate.println("\tUpdating method reference in " + cg.getClassName());
			ConstantMethodref ref = (ConstantMethodref) cg.getConstantPool().getConstant(index);
			// find the name reference
			int typeInd = ref.getNameAndTypeIndex();
			if (typeInd > -1) {
				ConstantNameAndType nameType = (ConstantNameAndType) cg.getConstantPool().getConstant(typeInd);
				int utf8 = nameType.getNameIndex();
				// find the utf8
				if (utf8 > -1) {
					ConstantUtf8 utf = (ConstantUtf8) cg.getConstantPool().getConstant(utf8);
					// set the new name
					if (utf.getBytes().equals(oldName))
						utf.setBytes(newName);
				}
			}
		}
	}

	private boolean lookThroughHierarchy(ClassGen cg, Method m, String previous) {
		if (cg.isAnnotation())
			return true;
		String superclass = cg.getSuperclassName();
		ClassGen sup = null;
		try {
			JavaClass clazz = Repository.lookupClass(superclass);
			sup = new ClassGen(clazz);
		} catch (ClassNotFoundException e) {
			sup = null;
		}
		if (sup == null) {
			return false;
		}

		// now look through interfaces
		for (String s : cg.getInterfaceNames()) {
			try {
				JavaClass jc = Repository.lookupClass(s);
				for (Method meth : jc.getMethods()) {
					boolean b = false;
					if (meth.getGenericSignature() != null) {
						String s1 = meth.getGenericSignature();
						b = GenericSignatureParser.compareSignatures(m.getSignature(), s1);
					}
					if (meth.getName().equals(m.getName()) && (meth.getSignature().equals(m.getSignature()) || b)) {
						return true;
					}
				}
			} catch (ClassNotFoundException e) {
				Obfuscate.println("\tCannot find interface: " + s);
			}
		}
		for (Method meth : sup.getMethods()) {
			// method is overriden, keep the name
			if (meth.getName().equals(m.getName()) && meth.getSignature().equals(m.getSignature())) {
				return true;
			}

		}
		if (sup.getSuperclassName() == null || previous.equals(sup.getSuperclassName()))
			return false;

		return lookThroughHierarchy(sup, m, superclass);
	}
}
