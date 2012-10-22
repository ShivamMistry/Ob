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
		insertControlField();
		findBranches();
	}

	public void insertControlField() {
		while (cg.containsField(fieldName) != null) {
			fieldName += "A";
		}
		FieldGen fg = new FieldGen(
				Constants.ACC_STATIC | Constants.ACC_PRIVATE, Type.INT,
				fieldName, cg.getConstantPool());
		cg.addField(fg.getField());
	}

	public void findBranches() {
		Random random = new Random();
		for (Method m : cg.getMethods()) {
			if (m.isAbstract() || m.isNative())
				continue;
			MethodGen mg = new MethodGen(m, cg.getClassName(),
					cg.getConstantPool());
			int maxStack = mg.getMaxStack();
			for (InstructionHandle ih : mg.getInstructionList()
					.getInstructionHandles()) {
				if (ih.getInstruction() instanceof GOTO && random.nextBoolean()) {
					InstructionList list = mg.getInstructionList();
					InstructionFactory factory = new InstructionFactory(cg);
					list.append(list.append(ih.getPrev(), new ICONST(0)),
							factory.createFieldAccess(cg.getClassName(),
									fieldName, Type.INT, Constants.GETSTATIC));
					ih.setInstruction(InstructionFactory
							.createBranchInstruction(Constants.IF_ICMPEQ,
									((GOTO) ih.getInstruction()).getTarget()));
					// list.append(ih, new NOP());
				}
			}
			mg.setMaxLocals();
			mg.setMaxStack(maxStack);
			cg.replaceMethod(m, mg.getMethod());
		}
	}
}
