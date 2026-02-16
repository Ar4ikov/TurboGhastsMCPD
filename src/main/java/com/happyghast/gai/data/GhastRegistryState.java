package com.happyghast.gai.data;

import com.happyghast.gai.HappyGhastGaiMod;
import com.happyghast.gai.config.GaiConfig;
import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GhastRegistryState extends PersistentState {

    private final Map<UUID, GhastVehicleData> ghastMap = new HashMap<>();
    private final Set<String> usedPlateNumbers = new HashSet<>();
    private GaiConfig config = new GaiConfig();

    private static final String STATE_KEY = "happy_ghast_gai";

    public static final Codec<GhastRegistryState> CODEC = NbtCompound.CODEC.xmap(
            GhastRegistryState::fromNbtCompound,
            GhastRegistryState::toNbtCompound
    );

    private static final PersistentStateType<GhastRegistryState> TYPE = new PersistentStateType<>(
            STATE_KEY,
            GhastRegistryState::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static GhastRegistryState get(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    public NbtCompound toNbtCompound() {
        NbtCompound nbt = new NbtCompound();
        NbtList ghastList = new NbtList();
        for (GhastVehicleData data : ghastMap.values()) {
            ghastList.add(data.toNbt());
        }
        nbt.put("Ghasts", ghastList);
        nbt.put("Config", config.toNbt());
        return nbt;
    }

    public static GhastRegistryState fromNbtCompound(NbtCompound nbt) {
        GhastRegistryState state = new GhastRegistryState();
        NbtList ghastList = nbt.getListOrEmpty("Ghasts");
        for (int i = 0; i < ghastList.size(); i++) {
            NbtCompound ghastNbt = ghastList.getCompoundOrEmpty(i);
            GhastVehicleData data = GhastVehicleData.fromNbt(ghastNbt);
            state.ghastMap.put(data.getGhastUuid(), data);
            if (!data.getPlateNumber().isEmpty()) {
                state.usedPlateNumbers.add(data.getPlateNumber());
            }
        }
        if (nbt.contains("Config")) {
            state.config = GaiConfig.fromNbt(nbt.getCompoundOrEmpty("Config"));
        }
        return state;
    }

    public boolean hasGhast(UUID ghastUuid) { return ghastMap.containsKey(ghastUuid); }

    @Nullable
    public GhastVehicleData getGhast(UUID ghastUuid) { return ghastMap.get(ghastUuid); }

    public void addGhast(GhastVehicleData data) {
        ghastMap.put(data.getGhastUuid(), data);
        if (!data.getPlateNumber().isEmpty()) usedPlateNumbers.add(data.getPlateNumber());
        markDirty();
    }

    public void removeGhast(UUID ghastUuid) {
        GhastVehicleData removed = ghastMap.remove(ghastUuid);
        if (removed != null && !removed.getPlateNumber().isEmpty()) {
            usedPlateNumbers.remove(removed.getPlateNumber());
        }
        markDirty();
    }

    public String registerGhast(UUID ghastUuid, UUID ownerUuid, String customName, int harnessColor, String plateNumber) {
        GhastVehicleData data = ghastMap.get(ghastUuid);
        if (data == null) {
            HappyGhastGaiMod.LOGGER.error("[GAI] Tried to register non-existent ghast: {}", ghastUuid);
            return "";
        }
        data.setOwnerUuid(ownerUuid);
        data.setCustomName(customName);
        data.setPlateNumber(plateNumber);
        data.setHarnessColor(harnessColor);
        data.setRegistered(true);
        data.setStage(1);
        data.setMaxStage(1);
        usedPlateNumbers.add(plateNumber);
        markDirty();
        return plateNumber;
    }

    @Nullable
    public GhastVehicleData findByPlateNumber(String plateNumber) {
        for (GhastVehicleData data : ghastMap.values()) {
            if (data.getPlateNumber().equals(plateNumber)) return data;
        }
        return null;
    }

    public Collection<GhastVehicleData> getAllGhasts() {
        return Collections.unmodifiableCollection(ghastMap.values());
    }

    public List<GhastVehicleData> getRegisteredGhasts() {
        return ghastMap.values().stream().filter(GhastVehicleData::isRegistered).collect(Collectors.toList());
    }

    public List<GhastVehicleData> getUnregisteredGhasts() {
        return ghastMap.values().stream().filter(d -> !d.isRegistered()).collect(Collectors.toList());
    }

    public List<GhastVehicleData> getImpoundedGhasts() {
        return ghastMap.values().stream().filter(GhastVehicleData::isImpounded).collect(Collectors.toList());
    }

    public List<GhastVehicleData> getGhastsByOwner(UUID ownerUuid) {
        return ghastMap.values().stream().filter(d -> ownerUuid.equals(d.getOwnerUuid())).collect(Collectors.toList());
    }

    public Set<String> getUsedPlateNumbers() { return Collections.unmodifiableSet(usedPlateNumbers); }

    public GaiConfig getConfig() { return config; }

    public void saveConfig() { markDirty(); }
}
