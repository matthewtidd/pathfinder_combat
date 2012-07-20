package com.dmtprogramming.pathfindercombat.modifier;

public class Plus4Hit extends ModifierBase {

	@Override
	public String apply(String field, String value) {
		if (field.equals(_hit)) {
			int i = parseInt(value);
			i += 4;
			return String.valueOf(i);
		}
		return value;
	}

	@Override
	public String name() {
		return "+4 Hit";
	}
}