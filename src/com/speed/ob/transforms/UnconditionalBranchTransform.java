package com.speed.ob.transforms;

import java.util.Random;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import com.speed.ob.ObTransform;

/**
 * Finds unconditional branches and adds a condition to them. This results in
 * loop obfuscation as many loops will have unconditional branches. Obstructs
 * the control flow.
 * 
 * @author Shivam Mistry
 * 
 */
public class UnconditionalBranchTransform extends ObTransform {
	private Random random;
	private String fieldName;

	public UnconditionalBranchTransform(ClassGen cg) {
		super(cg);
		fieldName = "controlField";
	}

	public void execute() {
		if (cg.isInterface()) {
			return;
		}
		random = new Random();
		insertControlField();
		findUnconditionalBranches();
	}

	private void insertControlField() {
		while (cg.containsField(fieldName) != null) {
			// create a control field with a name, this should be name
			// obfuscated anyway
			fieldName += "A";
		}
		FieldGen fg = new FieldGen(
				Constants.ACC_PRIVATE | Constants.ACC_STATIC, Type.INT,
				fieldName, cg.getConstantPool());
		// add the field to the class
		cg.addField(fg.getField());
	}

	private void findUnconditionalBranches() {
		// Random random = new Random();
		for (Method m : cg.getMethods()) {
			if (m.isAbstract() || m.isNative() || m.getName().equals("<init>"))
				continue;
			MethodGen mg = new MethodGen(m, cg.getClassName(),
					cg.getConstantPool());
			for (InstructionHandle ih : mg.getInstructionList()
					.getInstructionHandles()) {
				// find unconditional branches, add conditions to them
				if (ih.getInstruction() instanceof GOTO) {
					InstructionList list = mg.getInstructionList();
					InstructionFactory factory = new InstructionFactory(cg);
					// push zero on to the stack
					InstructionHandle zero = list.append(ih.getPrev(),
							new ICONST(0));
					// get the value of the 'control' field
					list.append(zero, factory.createFieldAccess(
							cg.getClassName(), fieldName, Type.INT,
							Constants.GETSTATIC));
					// compare integers, check if control is zero, complete the
					// jump if it is
					InstructionHandle target = ((GOTO) ih.getInstruction())
							.getTarget();
					// random between not equal to and equal to, doesn't matter
					// as the goto will jump to the target anyway
					ih.setInstruction(InstructionFactory
							.createBranchInstruction(
									random.nextBoolean() ? Constants.IF_ICMPNE
											: Constants.IF_ICMPEQ, ((GOTO) ih
											.getInstruction()).getTarget()));
					// go to the target anyway
					list.append(ih, new GOTO(target));
				}
			}
			mg.setMaxLocals();
			mg.setMaxStack();
			cg.replaceMethod(m, mg.getMethod());
		}
	}
}
