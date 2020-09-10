package com.a6raywa1cher.mucservingboxspring.model.file;

public enum ActionType {
	MANAGE_PERMISSIONS(0b100), WRITE(0b010), READ(0b001);
	public final int mask;

	ActionType(int mask) {
		this.mask = mask;
	}
}
