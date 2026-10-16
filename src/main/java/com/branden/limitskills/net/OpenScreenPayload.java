package com.branden.limitskills.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> Client: tell the client to open the skills screen.
 * Used by the /skills server command as a reliable fallback.
 */
public record OpenScreenPayload() implements CustomPacketPayload {
	public static final Type<OpenScreenPayload> TYPE = new Type<>(
		Identifier.fromNamespaceAndPath("limitskills", "open_screen")
	);

	public static final StreamCodec<FriendlyByteBuf, OpenScreenPayload> CODEC = StreamCodec.of(
		(buf, payload) -> {},
		buf -> new OpenScreenPayload()
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
