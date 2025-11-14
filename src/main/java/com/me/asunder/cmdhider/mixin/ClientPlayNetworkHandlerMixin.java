package com.me.asunder.cmdhider.mixin;

import com.me.asunder.cmdhider.config.Config;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientCommonPacketListener {

    @Shadow private CommandDispatcher<CommandSource> commandDispatcher;

    @Shadow @Final private DynamicRegistryManager.Immutable combinedDynamicRegistries;

    @Shadow @Final private FeatureSet enabledFeatures;

    @Inject(method = "onCommandTree", at = @At("HEAD"), cancellable = true)
    private void cmdHider$onCommandTree(CommandTreeS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        NetworkThreadUtils.forceMainThread(packet, client.getNetworkHandler(), client);

        CommandDispatcher<CommandSource> vanillaDispatcher = new CommandDispatcher<>(
                packet.getCommandTree(CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures))
        );

        CommandDispatcher<CommandSource> filteredDispatcher = new CommandDispatcher<>();
        RootCommandNode<CommandSource> filteredRoot = filteredDispatcher.getRoot();

        Config config = Config.get();
        RootCommandNode<CommandSource> vanillaRoot = vanillaDispatcher.getRoot();
        for (CommandNode<CommandSource> child : vanillaRoot.getChildren()) {
            if (!config.shouldHideCommand(child.getName())) {
                filteredRoot.addChild(child);
            }
        }

        this.commandDispatcher = filteredDispatcher;
        ci.cancel();
    }

}