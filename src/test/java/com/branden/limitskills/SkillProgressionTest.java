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

	@Test
	void passivePerkFormulasKeepTheirDocumentedCapsAndSteps() {
		assertEquals(0.25, Perks.doubleDropChance(25));
		assertEquals(0.25, Perks.doubleDropChance(100));
		assertEquals(0.20, Perks.critChanceBonus(25));
		assertEquals(3, Perks.bonusCropYield(30));
		assertEquals(0.30, Perks.damageReduction(100));
		assertEquals(24, Perks.bonusHealth(96));
		assertEquals(0.50, Perks.enchantCostReduction(25));
	}

	@Test
	void arcaneDiscountUsesRoundedCostAndNeverMakesAnEnchantmentFree() {
		assertEquals(3, Perks.discountedEnchantmentLevels(0, 3));
		assertEquals(2, Perks.discountedEnchantmentLevels(25, 3));
		assertEquals(1, Perks.discountedEnchantmentLevels(100, 1));
	}
}
