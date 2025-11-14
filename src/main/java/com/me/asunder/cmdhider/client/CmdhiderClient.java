package com.me.asunder.cmdhider.client;

import com.me.asunder.cmdhider.config.Config;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class CmdhiderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Config.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("cmdhider")
                .then(ClientCommandManager.literal("reload").executes(ctx -> {
                    boolean ok = Config.reload();
                    FabricClientCommandSource source = ctx.getSource();
                    source.sendFeedback(Text.literal(ok ? "CmdHider: config reloaded" : "CmdHider: failed to reload config"));
                    return ok ? Command.SINGLE_SUCCESS : 0;
                }))
        ));
    }
}