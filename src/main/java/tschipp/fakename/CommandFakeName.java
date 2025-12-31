package tschipp.fakename;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandFakeName {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("fakename")
                .then(
                        literal("real")
                                .then(
                                        CommandManager.argument("fakename", StringArgumentType.string())
                                                .suggests(CommandFakeName::suggestFakenames)
                                                .executes(cmd -> handleRealname(cmd.getSource(),
                                                        StringArgumentType.getString(cmd, "fakename")))))
                .then(
                        literal("clear")
                                .then(
                                        CommandManager.argument("target", EntityArgumentType.players())
                                                .requires(src -> src
                                                        .hasPermissionLevel(Config.getCommandPermissionLevelAll()))
                                                .executes(cmd -> handleClear(cmd.getSource(),
                                                        EntityArgumentType.getPlayers(cmd, "target"))))
                                .executes(cmd -> handleClear(cmd.getSource(),
                                        Collections.singleton(cmd.getSource().getPlayerOrThrow()))))
                .then(
                        literal("set")
                                .then(
                                        CommandManager.argument("fakename", StringArgumentType.string())
                                                .executes(cmd -> handleSetname(
                                                        cmd.getSource(),
                                                        Collections.singleton(cmd.getSource().getPlayerOrThrow()),
                                                        StringArgumentType.getString(cmd, "fakename"))))
                                .then(
                                        CommandManager.argument("target", EntityArgumentType.players())
                                                .requires(src -> src
                                                        .hasPermissionLevel(Config.getCommandPermissionLevelAll()))
                                                .then(
                                                        CommandManager.argument("fakename", StringArgumentType.string())
                                                                .executes(cmd -> handleSetname(
                                                                        cmd.getSource(),
                                                                        EntityArgumentType.getPlayers(cmd, "target"),
                                                                        StringArgumentType.getString(cmd,
                                                                                "fakename"))))));

        dispatcher.register(builder);
    }

    private static CompletableFuture<Suggestions> suggestFakenames(CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder) {
        ServerCommandSource source = context.getSource();
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            NbtCompound data = FakeNameData.getData(player);
            if (data.contains("fakename")) {
                String name = Formatting.strip(data.getString("fakename").orElse(""));
                if (name != null) {
                    name = name.contains(" ") ? ('"' + name + '"') : name;
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    }

    private static int handleSetname(ServerCommandSource source, Collection<ServerPlayerEntity> players,
            String string) {
        string = string.replace("&", "\u00a7") + "\u00a7r";

        for (ServerPlayerEntity player : players) {
            NbtCompound tag = FakeNameData.getData(player);
            tag.putString("fakename", string);
            source.sendMessage(Text.literal(player.getName().getString() + "'s name is now " + string));
            FakeName.sendPacket(player, string, 0);
        }

        return 1;
    }

    private static int handleClear(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            NbtCompound tag = FakeNameData.getData(player);
            tag.remove("fakename");
            source.sendMessage(Text.literal(player.getName().getString() + "'s fake name was cleared!"));
            FakeName.sendPacket(player, "", 1);
        }

        return 1;
    }

    private static int handleRealname(ServerCommandSource source, String string) {
        String copy = string;
        string = string.replace("&", "\u00a7") + "\u00a7r";
        string = Formatting.strip(string);

        boolean found = false;
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            NbtCompound data = FakeNameData.getData(player);
            if (data.contains("fakename")) {
                String fn = Formatting.strip(data.getString("fakename").orElse(""));
                if (fn != null && fn.equalsIgnoreCase(string)) {
                    source.sendMessage(Text.literal(copy + "'s real name is " + player.getGameProfile().name()));
                    found = true;
                }
            }
        }

        if (found) {
            return 1;
        }

        source.sendError(Text.literal("No player with that name was found!"));
        return 0;
    }
}
