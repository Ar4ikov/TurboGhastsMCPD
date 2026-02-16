package com.happyghast.gai.gui;

import com.happyghast.gai.data.GhastVehicleData;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParticleTrailManager {

    public record ParticlePreset(String id, String name, Item icon, boolean isDust) {}

    private static final Map<String, ParticlePreset> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("none",            new ParticlePreset("none",            "\u041D\u0435\u0442",           Items.BARRIER,           false));
        PRESETS.put("flame",           new ParticlePreset("flame",           "\u041E\u0433\u043E\u043D\u044C", Items.BLAZE_POWDER,    false));
        PRESETS.put("soul_fire_flame", new ParticlePreset("soul_fire_flame", "\u0414\u0443\u0448\u0435\u0432\u043D\u044B\u0439 \u043E\u0433\u043E\u043D\u044C", Items.SOUL_LANTERN, false));
        PRESETS.put("heart",           new ParticlePreset("heart",           "\u0421\u0435\u0440\u0434\u0446\u0430", Items.RED_DYE,   false));
        PRESETS.put("happy_villager",  new ParticlePreset("happy_villager",  "\u0418\u0441\u043A\u0440\u044B", Items.EMERALD,          false));
        PRESETS.put("end_rod",         new ParticlePreset("end_rod",         "\u0421\u0432\u0435\u0442\u043B\u044F\u0447\u043A\u0438", Items.END_ROD, false));
        PRESETS.put("smoke",           new ParticlePreset("smoke",           "\u0414\u044B\u043C",            Items.CAMPFIRE,          false));
        PRESETS.put("witch",           new ParticlePreset("witch",           "\u0412\u0435\u0434\u044C\u043C\u0438\u043D\u0430 \u043C\u0430\u0433\u0438\u044F",     Items.SPIDER_EYE,    false));
        PRESETS.put("note",            new ParticlePreset("note",            "\u041D\u043E\u0442\u044B",      Items.NOTE_BLOCK,        false));
        PRESETS.put("cherry_leaves",   new ParticlePreset("cherry_leaves",   "\u0421\u0430\u043A\u0443\u0440\u0430", Items.CHERRY_LEAVES, false));
        PRESETS.put("dust",            new ParticlePreset("dust",            "\u0426\u0432\u0435\u0442\u043D\u0430\u044F \u043F\u044B\u043B\u044C", Items.REDSTONE, true));
    }

    public static Map<String, ParticlePreset> getPresets() {
        return PRESETS;
    }

    public static ParticlePreset getPreset(String id) {
        return PRESETS.getOrDefault(id, PRESETS.get("none"));
    }

    public static void spawnTrail(ServerWorld world, Entity ghast, GhastVehicleData data) {
        String pid = data.getParticleId();
        if (pid == null || pid.equals("none")) return;

        ParticleEffect effect = getParticleEffect(pid, data.getParticleColor());
        if (effect == null) return;

        double x = ghast.getX();
        double y = ghast.getY() + 0.5;
        double z = ghast.getZ();

        world.spawnParticles(effect, x, y, z, 2, 0.8, 0.4, 0.8, 0.01);
    }

    private static ParticleEffect getParticleEffect(String particleId, int color) {
        return switch (particleId) {
            case "flame" -> ParticleTypes.FLAME;
            case "soul_fire_flame" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "heart" -> ParticleTypes.HEART;
            case "happy_villager" -> ParticleTypes.HAPPY_VILLAGER;
            case "end_rod" -> ParticleTypes.END_ROD;
            case "smoke" -> ParticleTypes.SMOKE;
            case "witch" -> ParticleTypes.WITCH;
            case "note" -> ParticleTypes.NOTE;
            case "cherry_leaves" -> ParticleTypes.CHERRY_LEAVES;
            case "dust" -> new DustParticleEffect(color, 1.2f);
            default -> null;
        };
    }

    public record DustColor(String name, int color, Item icon) {}

    private static final DustColor[] DUST_COLORS = {
            new DustColor("\u041A\u0440\u0430\u0441\u043D\u044B\u0439",        0xFF0000, Items.RED_DYE),
            new DustColor("\u041E\u0440\u0430\u043D\u0436\u0435\u0432\u044B\u0439",   0xFF8800, Items.ORANGE_DYE),
            new DustColor("\u0416\u0451\u043B\u0442\u044B\u0439",       0xFFFF00, Items.YELLOW_DYE),
            new DustColor("\u041B\u0430\u0439\u043C\u043E\u0432\u044B\u0439",      0x88FF00, Items.LIME_DYE),
            new DustColor("\u0417\u0435\u043B\u0451\u043D\u044B\u0439",     0x00AA00, Items.GREEN_DYE),
            new DustColor("\u0411\u0438\u0440\u044E\u0437\u043E\u0432\u044B\u0439",   0x00FFAA, Items.CYAN_DYE),
            new DustColor("\u0413\u043E\u043B\u0443\u0431\u043E\u0439",      0x00AAFF, Items.LIGHT_BLUE_DYE),
            new DustColor("\u0421\u0438\u043D\u0438\u0439",        0x0000FF, Items.BLUE_DYE),
            new DustColor("\u0424\u0438\u043E\u043B\u0435\u0442\u043E\u0432\u044B\u0439",  0x8800FF, Items.PURPLE_DYE),
            new DustColor("\u0420\u043E\u0437\u043E\u0432\u044B\u0439",      0xFF88CC, Items.PINK_DYE),
            new DustColor("\u0411\u0435\u043B\u044B\u0439",        0xFFFFFF, Items.WHITE_DYE),
            new DustColor("\u0421\u0435\u0440\u044B\u0439",        0x888888, Items.GRAY_DYE),
            new DustColor("\u0427\u0451\u0440\u043D\u044B\u0439",      0x222222, Items.BLACK_DYE),
            new DustColor("\u041A\u043E\u0440\u0438\u0447\u043D\u0435\u0432\u044B\u0439",  0x884400, Items.BROWN_DYE),
    };

    public static DustColor[] getDustColors() {
        return DUST_COLORS;
    }

    public static String getDustColorName(int color) {
        for (DustColor dc : DUST_COLORS) {
            if (dc.color() == color) return dc.name();
        }
        return String.format("#%06X", color);
    }
}
