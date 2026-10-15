package com.branden.limitskills.net;

import com.branden.limitskills.SkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> Client: full skill data sync.
 */
public record SkillSyncPayload(double[] xp, int[] levels) implements CustomPacketPayload {
	public static final Type<SkillSyncPayload> TYPE = new Type<>(ResourceLocation.parse("limitskills:skill_sync"));

	public static final StreamCodec<FriendlyByteBuf, SkillSyncPayload> CODEC = StreamCodec.of(
		(buf, payload) -> {
			for (int i = 0; i < SkillType.values().length; i++) {
				buf.writeDouble(payload.xp[i]);
				buf.writeVarInt(payload.levels[i]);
			}
		},
		buf -> {
			double[] xp = new double[SkillType.values().length];
			int[] levels = new int[SkillType.values().length];
			for (int i = 0; i < SkillType.values().length; i++) {
				xp[i] = buf.readDouble();
				levels[i] = buf.readVarInt();
			}
			return new SkillSyncPayload(xp, levels);
		}
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
