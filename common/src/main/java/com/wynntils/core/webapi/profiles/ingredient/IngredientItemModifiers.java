/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.ingredient;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class IngredientItemModifiers {

    int durability = 0;
    int duration = 0;
    int charges = 0;
    int strength = 0;
    int dexterity = 0;
    int intelligence = 0;
    int defense = 0;
    int agility = 0;

    public int getAgility() {
        return agility;
    }

    public int getDefense() {
        return defense;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getStrength() {
        return strength;
    }

    public int getDurability() {
        return durability;
    }

    public int getDuration() {
        return duration;
    }

    public int getCharges() {
        return charges;
    }

    private static String getFormattedModifierText(String itemModifierName, int modifierValue) {
        if (itemModifierName.equals("Duration")
                || itemModifierName.equals("Charges")
                || itemModifierName.equals("Durability")) {
            return (modifierValue > 0 ? ChatFormatting.GREEN + "+" : ChatFormatting.RED.toString()) + modifierValue
                    + " " + itemModifierName;
        }
        return (modifierValue > 0 ? ChatFormatting.RED + "+" : ChatFormatting.GREEN.toString()) + modifierValue + " "
                + itemModifierName;
    }

    public boolean anyExists() {
        return durability != 0
                || duration != 0
                || charges != 0
                || strength != 0
                || dexterity != 0
                || intelligence != 0
                || defense != 0
                || agility != 0;
    }

    public List<TextComponent> getItemModifierLoreLines() {
        List<String> itemLore = new ArrayList<>();

        if (durability != 0 && duration != 0) {
            itemLore.add(
                    IngredientItemModifiers.getFormattedModifierText("Durability", durability) + ChatFormatting.GRAY
                            + " or " + IngredientItemModifiers.getFormattedModifierText("Duration", duration));
        } else if (durability != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Durability", durability));
        } else if (duration != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Duration", duration));
        }

        if (charges != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Charges", charges));
        }
        if (strength != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Strength Min.", strength));
        }
        if (dexterity != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Dexterity Min.", dexterity));
        }
        if (intelligence != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Intelligence Min.", intelligence));
        }
        if (defense != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Defense Min.", defense));
        }
        if (agility != 0) {
            itemLore.add(IngredientItemModifiers.getFormattedModifierText("Agility Min.", agility));
        }

        return itemLore.stream().map(TextComponent::new).toList();
    }
}
