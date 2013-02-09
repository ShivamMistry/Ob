package com.speed.ob.transforms;

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
 * Obstructs the control flow. Currently in development and NOT WORKING.
 * 
 * @author Shivam Mistry
 * 
 */
public class ControlFlowTransform extends ObTransform {

	private String fieldName;

	public ControlFlowTransform(ClassGen cg) {
		super(cg);
		fieldName = "controlField";
	}

	public void execute() {
		if (cg.isInterface()) {
			return;
		}
		insertControlField();
		findBranches();
	}

	public void insertControlField() {
		while (cg.containsField(fieldName) != null) {
			fieldName += "A";
		}
		FieldGen fg = new FieldGen(
				Constants.ACC_PRIVATE | Constants.ACC_STATIC, Type.INT,
				fieldName, cg.getConstantPool());
		cg.addField(fg.getField());
	}

	public void findBranches() {
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
					ih.setInstruction(InstructionFactory
							.createBranchInstruction(Constants.IF_ICMPEQ,
									((GOTO) ih.getInstruction()).getTarget()));
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
