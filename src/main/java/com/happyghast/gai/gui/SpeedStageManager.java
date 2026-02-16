package com.happyghast.gai.gui;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class SpeedStageManager {

    public static final double STAGE_1_SPEED = 0.12;
    public static final double STAGE_2_SPEED = 0.14;
    public static final double STAGE_3_SPEED = 0.16;

    public static double getSpeedForStage(int stage) {
        return switch (stage) {
            case 1 -> STAGE_1_SPEED;
            case 2 -> STAGE_2_SPEED;
            case 3 -> STAGE_3_SPEED;
            default -> STAGE_1_SPEED;
        };
    }

    public static void applyStageSpeed(HappyGhastEntity ghast, int stage) {
        EntityAttributeInstance attr = ghast.getAttributeInstance(EntityAttributes.FLYING_SPEED);
        if (attr != null) {
            attr.setBaseValue(getSpeedForStage(stage));
        }
    }

    public static String getStageName(int stage) {
        return switch (stage) {
            case 1 -> "\u0421\u0442\u0435\u0439\u0434\u0436 1 (\u0431\u0430\u0437\u043E\u0432\u044B\u0439)";
            case 2 -> "\u0421\u0442\u0435\u0439\u0434\u0436 2";
            case 3 -> "\u0421\u0442\u0435\u0439\u0434\u0436 3 (MAX)";
            default -> "\u041D\u0435\u0442";
        };
    }

    public static Item getStageIcon(int stage) {
        return switch (stage) {
            case 1 -> Items.LEATHER_HORSE_ARMOR;
            case 2 -> Items.IRON_HORSE_ARMOR;
            case 3 -> Items.DIAMOND_HORSE_ARMOR;
            default -> Items.BARRIER;
        };
    }

    public static String getStageColor(int stage) {
        return switch (stage) {
            case 1 -> "\u00a77";
            case 2 -> "\u00a7e";
            case 3 -> "\u00a7b";
            default -> "\u00a78";
        };
    }
}
