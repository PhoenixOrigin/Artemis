/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands.wynntils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.FunctionManager;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.time.DurationFormatUtils;

public final class WynntilsFunctionCommand {
    private static final SuggestionProvider<CommandSourceStack> functionSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FunctionManager.getFunctions().stream().map(Function::getName), builder);

    private static final SuggestionProvider<CommandSourceStack> activeFunctionSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FunctionManager.getFunctions().stream()
                            .filter(function -> function instanceof ActiveFunction<?>)
                            .map(Function::getName),
                    builder);

    public static LiteralCommandNode<CommandSourceStack> buildListNode() {
        return Commands.literal("list")
                .executes(WynntilsFunctionCommand::listFunctions)
                .build();
    }

    private static int listFunctions(CommandContext<CommandSourceStack> context) {
        List<Function<?>> functions = FunctionManager.getFunctions().stream().collect(Collectors.toList());
        functions.sort(Comparator.comparing(o -> o.getName()));

        MutableComponent response = new TextComponent("Currently registered functions:").withStyle(ChatFormatting.AQUA);

        for (Function<?> function : functions) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(function.getName()).withStyle(ChatFormatting.YELLOW));
            if (!function.getAliases().isEmpty()) {
                String aliasList = String.join(", ", function.getAliases());

                response.append(new TextComponent(" [alias: ").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(aliasList).withStyle(ChatFormatting.WHITE))
                        .append(new TextComponent("]").withStyle(ChatFormatting.GRAY));
            }
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildEnableNode() {
        return Commands.literal("enable")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(activeFunctionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::enableFunction))
                .build();
    }

    private static int enableFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function<?>> functionOptional = FunctionManager.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function<?> function = functionOptional.get();
        if (!(function instanceof ActiveFunction<?> activeFunction)) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Function does not need to be enabled").withStyle(ChatFormatting.RED));
            return 0;
        }

        boolean success = FunctionManager.enableFunction(activeFunction);

        if (!success) {
            context.getSource()
                    .sendFailure(new TextComponent("Function could not be enabled").withStyle(ChatFormatting.RED));
            return 0;
        }

        Component response = new TextComponent(function.getName())
                .withStyle(ChatFormatting.AQUA)
                .append(new TextComponent(" is now enabled").withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildDisableNode() {
        return Commands.literal("disable")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(activeFunctionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::disableFunction))
                .build();
    }

    private static int disableFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function<?>> functionOptional = FunctionManager.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function<?> function = functionOptional.get();
        if (!(function instanceof ActiveFunction<?> activeFunction)) {
            context.getSource()
                    .sendFailure(new TextComponent("Function can not be disabled").withStyle(ChatFormatting.RED));
            return 0;
        }

        FunctionManager.disableFunction(activeFunction);

        Component response = new TextComponent(function.getName())
                .withStyle(ChatFormatting.AQUA)
                .append(new TextComponent(" is now disabled").withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildGetValueNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::getValue))
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> buildGetValueWithArgumentNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .then(Commands.argument("argument", StringArgumentType.greedyString())
                                .executes(WynntilsFunctionCommand::getValue)))
                .build();
    }

    private static int getValue(CommandContext<CommandSourceStack> context) {
        Component argument;
        try {
            argument = new TextComponent(StringArgumentType.getString(context, "argument"));
        } catch (IllegalArgumentException e) {
            argument = new TextComponent("");
        }

        String functionName = context.getArgument("function", String.class);
        Optional<Function<?>> functionOptional = FunctionManager.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found").withStyle(ChatFormatting.RED));
            return 0;
        }
        Function<?> function = functionOptional.get();

        String extraInfo = "";
        if (function instanceof ActiveFunction<?> activeFunction) {
            StringBuilder activeInfo = new StringBuilder(" [");
            if (!FunctionManager.isEnabled(activeFunction)) {
                activeInfo.append("not enabled; ");
            }
            long updateDelay = System.currentTimeMillis() - activeFunction.lastUpdateTime();
            String updateDelayString = DurationFormatUtils.formatDurationWords(updateDelay, true, true);
            activeInfo.append("last updated ");
            activeInfo.append(updateDelayString);
            activeInfo.append(" ago]");
            extraInfo = activeInfo.toString();
        }

        MutableComponent result = new TextComponent("");
        result.append(
                FunctionManager.getSimpleValueString(function, argument.getString(), ChatFormatting.YELLOW, true));
        if (!extraInfo.isEmpty()) {
            result.append(new TextComponent(extraInfo).withStyle(ChatFormatting.GRAY));
        }
        context.getSource().sendSuccess(result, false);
        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildHelpNode() {
        return Commands.literal("help")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::helpForFunction))
                .build();
    }

    private static int helpForFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function<?>> functionOptional = FunctionManager.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function<?> function = functionOptional.get();

        String helpText = function.getDescription();

        Component response = new TextComponent(function.getName() + ": ")
                .withStyle(ChatFormatting.AQUA)
                .append(new TextComponent(helpText).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }
}