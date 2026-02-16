package com.happyghast.gai.gui;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HarnessManager {

    private static final Item[] HARNESS_BY_COLOR = {
            Items.WHITE_HARNESS, Items.ORANGE_HARNESS, Items.MAGENTA_HARNESS,
            Items.LIGHT_BLUE_HARNESS, Items.YELLOW_HARNESS, Items.LIME_HARNESS,
            Items.PINK_HARNESS, Items.GRAY_HARNESS, Items.LIGHT_GRAY_HARNESS,
            Items.CYAN_HARNESS, Items.PURPLE_HARNESS, Items.BLUE_HARNESS,
            Items.BROWN_HARNESS, Items.GREEN_HARNESS, Items.RED_HARNESS,
            Items.BLACK_HARNESS,
    };

    public static Item getHarnessItem(int colorIndex) {
        if (colorIndex < 0 || colorIndex >= HARNESS_BY_COLOR.length) return Items.WHITE_HARNESS;
        return HARNESS_BY_COLOR[colorIndex];
    }

    public static void equipHarness(HappyGhastEntity ghast, int colorIndex) {
        Item harnessItem = getHarnessItem(colorIndex);
        ItemStack harnessStack = new ItemStack(harnessItem);
        ghast.equipStack(EquipmentSlot.BODY, harnessStack);
    }

    public static String getColorName(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> "\u0411\u0435\u043B\u044B\u0439";
            case 1 -> "\u041E\u0440\u0430\u043D\u0436\u0435\u0432\u044B\u0439";
            case 2 -> "\u041F\u0443\u0440\u043F\u0443\u0440\u043D\u044B\u0439";
            case 3 -> "\u0413\u043E\u043B\u0443\u0431\u043E\u0439";
            case 4 -> "\u0416\u0451\u043B\u0442\u044B\u0439";
            case 5 -> "\u041B\u0430\u0439\u043C\u043E\u0432\u044B\u0439";
            case 6 -> "\u0420\u043E\u0437\u043E\u0432\u044B\u0439";
            case 7 -> "\u0421\u0435\u0440\u044B\u0439";
            case 8 -> "\u0421\u0432\u0435\u0442\u043B\u043E-\u0441\u0435\u0440\u044B\u0439";
            case 9 -> "\u0411\u0438\u0440\u044E\u0437\u043E\u0432\u044B\u0439";
            case 10 -> "\u0424\u0438\u043E\u043B\u0435\u0442\u043E\u0432\u044B\u0439";
            case 11 -> "\u0421\u0438\u043D\u0438\u0439";
            case 12 -> "\u041A\u043E\u0440\u0438\u0447\u043D\u0435\u0432\u044B\u0439";
            case 13 -> "\u0417\u0435\u043B\u0451\u043D\u044B\u0439";
            case 14 -> "\u041A\u0440\u0430\u0441\u043D\u044B\u0439";
            case 15 -> "\u0427\u0451\u0440\u043D\u044B\u0439";
            default -> "\u0411\u0435\u043B\u044B\u0439";
        };
    }

    public static Item getWoolItem(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> Items.WHITE_WOOL; case 1 -> Items.ORANGE_WOOL;
            case 2 -> Items.MAGENTA_WOOL; case 3 -> Items.LIGHT_BLUE_WOOL;
            case 4 -> Items.YELLOW_WOOL; case 5 -> Items.LIME_WOOL;
            case 6 -> Items.PINK_WOOL; case 7 -> Items.GRAY_WOOL;
            case 8 -> Items.LIGHT_GRAY_WOOL; case 9 -> Items.CYAN_WOOL;
            case 10 -> Items.PURPLE_WOOL; case 11 -> Items.BLUE_WOOL;
            case 12 -> Items.BROWN_WOOL; case 13 -> Items.GREEN_WOOL;
            case 14 -> Items.RED_WOOL; case 15 -> Items.BLACK_WOOL;
            default -> Items.WHITE_WOOL;
        };
    }
}
