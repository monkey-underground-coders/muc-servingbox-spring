package com.a6raywa1cher.mucservingboxspring.model.file;

import java.util.ArrayList;
import java.util.List;

public enum ActionType {
	MANAGE_PERMISSIONS(0b100), WRITE(0b010), READ(0b001);
	public final int mask;
	public final List<Integer> allMasks;

	ActionType(int mask) {
		this.mask = mask;
		this.allMasks = new ArrayList<>();
		for (int i = 0; i <= 0b111; i++) {
			if ((mask & i) > 0) {
				allMasks.add(mask);
			}
		}
	}
}
