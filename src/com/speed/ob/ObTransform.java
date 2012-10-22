package com.speed.ob;

import org.apache.bcel.generic.ClassGen;

public abstract class ObTransform {
	protected final ClassGen cg;

	public ObTransform(ClassGen cg) {
		this.cg = cg;
	}

	public abstract void execute();
}
