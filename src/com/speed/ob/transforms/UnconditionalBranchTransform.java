package com.speed.ob.transforms;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import com.speed.ob.ObTransform;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Finds unconditional branches and adds a condition to them. This results in
 * loop obfuscation as many loops will have unconditional branches. Obstructs
 * the control flow.
 *
 * @author Shivam Mistry
 */
public class UnconditionalBranchTransform extends ObTransform {
    private String fieldName;

    public UnconditionalBranchTransform(ClassGen cg) {
        super(cg);
        fieldName = "controlField";
    }

    public void execute() {
        if (cg.isInterface()) {
            return;
        }
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

    private InstructionHandle insertInvalidCode(InstructionList list, InstructionFactory factory, MethodGen mg) {
        InstructionHandle end = list.getEnd();
        //TODO: add some sort of variation
        InstructionList dead = new InstructionList();
        Random random = new SecureRandom();
        dead.append(new ICONST(random.nextInt(6)));
        dead.append(new BIPUSH((byte) random.nextInt(128)));
        dead.append(new DUP_X1());
        dead.append(new SWAP());
        dead.append(new POP());
        dead.append(new POP());
        dead.append(new POP());
        if(mg.getType() == Type.INT || mg.getType() == Type.BOOLEAN || mg.getType() == Type.CHAR || mg.getType() == Type.SHORT
                || mg.getType() == Type.LONG || mg.getType() == Type.BYTE) {
            dead.append(new ICONST(0));
            dead.append(new POP());

        } else if(!mg.getReturnType().equals(Type.VOID)){
            dead.append(new ACONST_NULL());
            dead.append(new POP());

        }
        dead.append(new GOTO(dead.getStart()));
        return list.append(end, dead);
    }

    private void findUnconditionalBranches() {
        // Random random = new Random();
        for (Method m : cg.getMethods()) {
            if (m.isAbstract() || m.isNative() || m.getName().equals("<init>"))
                continue;
            MethodGen mg = new MethodGen(m, cg.getClassName(),
                    cg.getConstantPool());
            InstructionHandle deadCodeStart = null;
            for (InstructionHandle ih : mg.getInstructionList()
                    .getInstructionHandles()) {
                // find unconditional branches, add conditions to them
                if (ih.getInstruction() instanceof GOTO) {
                    InstructionList list = mg.getInstructionList();
                    InstructionFactory factory = new InstructionFactory(cg);

                    if (deadCodeStart == null) {
                        deadCodeStart = insertInvalidCode(list, factory, mg);
                    }
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
                            .createBranchInstruction(Constants.IF_ICMPEQ,
                                    target));
                    // go to the invalid code added at the end of the method
                    list.append(ih, new GOTO(deadCodeStart));
                }
            }
            mg.setMaxLocals();
            mg.setMaxStack();
            cg.replaceMethod(m, mg.getMethod());
        }
    }
}
