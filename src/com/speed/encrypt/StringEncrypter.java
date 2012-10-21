package com.speed.encrypt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 * Encrypts strings.
 * 
 * @author Shivam Mistry
 * 
 */
public class StringEncrypter {
	private final ClassGen cg;
	private String methodName;
	private static MethodGen decryptMethod;
	private int callsChanged;
	private List<String> encryptedStrings;

	static {
		ClassParser cp = new ClassParser("resources/A/A.class");
		try {
			JavaClass jc = cp.parse();
			ClassGen clg = new ClassGen(jc);
			for (Method m : clg.getMethods()) {
				if (m.getName().equals("decrypt")) {
					decryptMethod = new MethodGen(m, clg.getClassName(),
							clg.getConstantPool());
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StringEncrypter(final ClassGen cg) {
		this.cg = cg;
		methodName = "aA";
		while (cg.containsMethod(methodName, Type.getMethodSignature(
				Type.STRING, new Type[] { Type.STRING })) != null) {
			methodName = methodName + "A";
		}
		encryptedStrings = new ArrayList<String>();
		System.out.printf(
				"Loading encrypter for %s, decrypt method name: %s\n",
				cg.getClassName(), methodName);
	}

	public void execute() {
		System.out.println("Starting encryption on class " + cg.getClassName());
		changeCalls();
		System.out.println("Changed " + callsChanged + " LDCs");
		insertMethod();
		System.out.println("Inserted method " + methodName);
	}

	private void insertMethod() {
		MethodGen newMg = decryptMethod.copy(cg.getClassName(),
				cg.getConstantPool());
		newMg.setName(methodName);
		cg.getConstantPool().addMethodref(newMg);
		cg.addMethod(newMg.getMethod());
	}

	private void changeCalls() {
		InvokeInstruction invoke = new InstructionFactory(cg).createInvoke(
				cg.getClassName(), methodName, Type.STRING,
				new Type[] { Type.STRING }, Constants.INVOKESTATIC);

		for (Method m : cg.getMethods()) {
			if (m.isAbstract() || m.isNative())
				continue;
			ConstantPoolGen cpg = cg.getConstantPool();
			MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
			InstructionList il = mg.getInstructionList();
			for (InstructionHandle handle : il.getInstructionHandles()) {
				if (handle.getInstruction() instanceof LDC) {
					LDC ldc = (LDC) handle.getInstruction();
					if (ldc.getType(cpg).equals(Type.STRING)) {
						int cpIndex = ldc.getIndex();
						String original = ldc.getValue(cpg).toString();
						//if (!encryptedStrings.contains(original)) {
							String encrypted = A.A.decrypt(original);
							int strIndex = cpg.addString(encrypted);
							cpg.setConstant(cpIndex, cpg.getConstant(strIndex));
							int utf8 = cpg.lookupUtf8(original);
							int utf8new = cpg.lookupUtf8(encrypted);
							cpg.setConstant(utf8, cpg.getConstant(utf8new));
							encryptedStrings.add(encrypted);
						//}
						il.insert(handle.getNext(), invoke);
						callsChanged++;
					}
				}
			}
			mg.setMaxLocals();
			mg.setMaxStack();
			cg.replaceMethod(m, mg.getMethod());

		}

	}

}
