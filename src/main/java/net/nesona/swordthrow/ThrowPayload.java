package net.nesona.swordthrow;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "throw the sword in my main hand". */
public record ThrowPayload() implements CustomPayload {
    public static final CustomPayload.Id<ThrowPayload> ID =
            new CustomPayload.Id<>(Identifier.of(SwordThrowMod.MOD_ID, "throw"));
    public static final PacketCodec<RegistryByteBuf, ThrowPayload> CODEC =
            PacketCodec.unit(new ThrowPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
