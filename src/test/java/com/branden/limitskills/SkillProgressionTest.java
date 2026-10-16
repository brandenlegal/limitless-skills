package com.branden.limitskills;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SkillProgressionTest {
	@Test
	void xpCurveKeepsExistingLevelBoundaries() {
		assertEquals(100.0, SkillManager.xpForLevel(0));
		assertEquals(135.0, SkillManager.xpForLevel(1), 1e-9);
		assertEquals(0, SkillManager.xpToLevel(99.99));
		assertEquals(1, SkillManager.xpToLevel(100.0));
		assertEquals(1, SkillManager.xpToLevel(234.99));
		assertEquals(2, SkillManager.xpToLevel(235.0));
	}

	@Test
	void clearingOnePlayersSessionDoesNotTouchAnother() {
		UUID first = UUID.randomUUID();
		UUID second = UUID.randomUUID();
		SkillData firstData = new SkillData();
		SkillData secondData = new SkillData();
		firstData.addXp(SkillType.MINING, 235.0);
		secondData.addXp(SkillType.COMBAT, 20.0);
		try {
			SkillStore.putCached(first, firstData);
			SkillStore.putCached(second, secondData);
			SkillStore.onPlayerDisconnect(first);
			assertNull(SkillStore.getCached(first));
			assertSame(secondData, SkillStore.getCached(second));
			assertEquals(20.0, SkillStore.getCached(second).getXp(SkillType.COMBAT));
		} finally {
			SkillStore.clear(first);
			SkillStore.clear(second);
		}
	}
}
