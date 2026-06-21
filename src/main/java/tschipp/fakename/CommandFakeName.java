package tschipp.fakename;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class CommandFakeName {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("fakename")
                .then(Commands.literal("real")
                        .then(Commands.argument("fakename", StringArgumentType.string())
                                .suggests(CommandFakeName::suggestFakenames)
                                .executes(cmd -> handleRealname(cmd.getSource(),
                                        StringArgumentType.getString(cmd, "fakename")))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("target", EntityArgument.players())
                                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                                .executes(cmd -> handleClear(cmd.getSource(),
                                        EntityArgument.getPlayers(cmd, "target"))))
                        .executes(cmd -> handleClear(cmd.getSource(),
                                Collections.singleton(cmd.getSource().getPlayerOrException()))))
                .then(Commands.literal("set")
                        .then(Commands.argument("fakename", StringArgumentType.string())
                                .executes(cmd -> handleSetname(cmd.getSource(),
                                        Collections.singleton(cmd.getSource().getPlayerOrException()),
                                        StringArgumentType.getString(cmd, "fakename"))))
                        .then(Commands.argument("target", EntityArgument.players())
                                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("fakename", StringArgumentType.string())
                                        .executes(cmd -> handleSetname(cmd.getSource(),
                                                EntityArgument.getPlayers(cmd, "target"),
                                                StringArgumentType.getString(cmd, "fakename"))))));

        dispatcher.register(builder);
    }

    private static CompletableFuture<Suggestions> suggestFakenames(CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            CompoundTag data = FakeNameData.getData(player);
            if (data.contains("fakename")) {
                String name = ChatFormatting.stripFormatting(data.getStringOr("fakename", ""));
                if (name != null) {
                    name = name.contains(" ") ? ('"' + name + '"') : name;
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    }

    private static int handleSetname(CommandSourceStack source, Collection<ServerPlayer> players, String string) {
        string = string.replace("&", "\u00a7") + "\u00a7r";
        for (ServerPlayer player : players) {
            CompoundTag tag = FakeNameData.getData(player);
            tag.putString("fakename", string);
            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s name is now " + string));
            FakeName.sendPacket(player, string, 0);
        }
        return 1;
    }

    private static int handleClear(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            CompoundTag tag = FakeNameData.getData(player);
            tag.remove("fakename");
            source.sendSystemMessage(Component.literal(player.getName().getString() + "'s fake name was cleared!"));
            FakeName.sendPacket(player, "", 1);
        }
        return 1;
    }

    private static int handleRealname(CommandSourceStack source, String string) {
        String copy = string;
        string = string.replace("&", "\u00a7") + "\u00a7r";
        string = ChatFormatting.stripFormatting(string);

        boolean found = false;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            CompoundTag data = FakeNameData.getData(player);
            if (data.contains("fakename")) {
                String fn = ChatFormatting.stripFormatting(data.getStringOr("fakename", ""));
                if (fn != null && fn.equalsIgnoreCase(string)) {
                    source.sendSystemMessage(Component.literal(copy + "'s real name is " + player.getGameProfile().name()));
                    found = true;
                }
            }
        }

        if (found) return 1;
        source.sendFailure(Component.literal("No player with that name was found!"));
        return 0;
    }
}
