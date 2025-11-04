package dev.lopyluna.slag.network.packets;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.ResourceFallbackGenerator;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SyncTypesS2C(List<MaterialType> materials, List<PartType> parts, List<ModularType> modulars) implements CustomPacketPayload {

    public static final Type<SyncTypesS2C> TYPE = new Type<>(SlagEmbers.loc("sync_types"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTypesS2C> CODEC = StreamCodec.composite(
            MaterialType.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncTypesS2C::materials,
            PartType.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncTypesS2C::parts,
            ModularType.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncTypesS2C::modulars,
            SyncTypesS2C::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTypesS2C packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var materials = packet.materials();
            var parts = packet.parts();
            SlagEmbers.LOGGER.info("Received {} materials, {} parts, and {} modulars from server", materials.size(), parts.size(), packet.modulars().size());
            AllDynamicTypes.syncFromServer(packet.materials(), packet.parts(), packet.modulars());
            ResourceFallbackGenerator.reloadModels(true);
        });
    }
}