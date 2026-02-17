package com.happyghast.gai.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GhastVehicleData {

    private UUID ghastUuid;
    @Nullable private UUID ownerUuid;
    private String customName;
    private String plateNumber;
    private int harnessColor;
    private BlockPos homePos;
    private String homeDimension;
    private long firstSeenTimestamp;
    private long registrationDeadline;
    private boolean registered;
    private boolean impounded;
    @Nullable private UUID frontPlateEntityUuid;
    @Nullable private UUID backPlateEntityUuid;
    private int stage;
    private int maxStage;
    private String particleId;
    private int particleColor;
    private double mileage;
    private boolean gaiMode;
    @Nullable private UUID sirenRedUuid;
    @Nullable private UUID sirenBlueUuid;

    public GhastVehicleData(UUID ghastUuid, BlockPos homePos, String homeDimension,
                            long firstSeenTimestamp, long registrationDeadline) {
        this.ghastUuid = ghastUuid;
        this.ownerUuid = null;
        this.customName = "";
        this.plateNumber = "";
        this.harnessColor = -1;
        this.homePos = homePos;
        this.homeDimension = homeDimension;
        this.firstSeenTimestamp = firstSeenTimestamp;
        this.registrationDeadline = registrationDeadline;
        this.registered = false;
        this.impounded = false;
        this.frontPlateEntityUuid = null;
        this.backPlateEntityUuid = null;
        this.stage = 0;
        this.maxStage = 0;
        this.particleId = "none";
        this.particleColor = 0xFF0000;
        this.mileage = 0.0;
        this.gaiMode = false;
        this.sirenRedUuid = null;
        this.sirenBlueUuid = null;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("GhastUuid", ghastUuid.toString());
        if (ownerUuid != null) {
            nbt.putString("OwnerUuid", ownerUuid.toString());
        }
        nbt.putString("CustomName", customName);
        nbt.putString("PlateNumber", plateNumber);
        nbt.putInt("HarnessColor", harnessColor);
        nbt.putInt("HomePosX", homePos.getX());
        nbt.putInt("HomePosY", homePos.getY());
        nbt.putInt("HomePosZ", homePos.getZ());
        nbt.putString("HomeDimension", homeDimension);
        nbt.putLong("FirstSeenTimestamp", firstSeenTimestamp);
        nbt.putLong("RegistrationDeadline", registrationDeadline);
        nbt.putBoolean("Registered", registered);
        nbt.putBoolean("Impounded", impounded);
        if (frontPlateEntityUuid != null) {
            nbt.putString("FrontPlateUuid", frontPlateEntityUuid.toString());
        }
        if (backPlateEntityUuid != null) {
            nbt.putString("BackPlateUuid", backPlateEntityUuid.toString());
        }
        nbt.putInt("Stage", stage);
        nbt.putInt("MaxStage", maxStage);
        nbt.putString("ParticleId", particleId);
        nbt.putInt("ParticleColor", particleColor);
        nbt.putDouble("Mileage", mileage);
        nbt.putBoolean("GaiMode", gaiMode);
        if (sirenRedUuid != null) nbt.putString("SirenRedUuid", sirenRedUuid.toString());
        if (sirenBlueUuid != null) nbt.putString("SirenBlueUuid", sirenBlueUuid.toString());
        return nbt;
    }

    public static GhastVehicleData fromNbt(NbtCompound nbt) {
        UUID ghastUuid = UUID.fromString(nbt.getString("GhastUuid", "00000000-0000-0000-0000-000000000000"));
        BlockPos homePos = new BlockPos(
                nbt.getInt("HomePosX", 0),
                nbt.getInt("HomePosY", 0),
                nbt.getInt("HomePosZ", 0)
        );
        String homeDimension = nbt.getString("HomeDimension", "minecraft:overworld");
        long firstSeen = nbt.getLong("FirstSeenTimestamp", 0L);
        long deadline = nbt.getLong("RegistrationDeadline", 0L);

        GhastVehicleData data = new GhastVehicleData(ghastUuid, homePos, homeDimension, firstSeen, deadline);

        String ownerStr = nbt.getString("OwnerUuid", "");
        if (!ownerStr.isEmpty()) {
            data.ownerUuid = UUID.fromString(ownerStr);
        }
        data.customName = nbt.getString("CustomName", "");
        data.plateNumber = nbt.getString("PlateNumber", "");
        data.harnessColor = nbt.getInt("HarnessColor", -1);
        data.registered = nbt.getBoolean("Registered", false);
        data.impounded = nbt.getBoolean("Impounded", false);

        String frontStr = nbt.getString("FrontPlateUuid", "");
        if (!frontStr.isEmpty()) {
            data.frontPlateEntityUuid = UUID.fromString(frontStr);
        }
        String backStr = nbt.getString("BackPlateUuid", "");
        if (!backStr.isEmpty()) {
            data.backPlateEntityUuid = UUID.fromString(backStr);
        }
        data.stage = nbt.getInt("Stage", 0);
        data.maxStage = nbt.getInt("MaxStage", data.stage);
        data.particleId = nbt.getString("ParticleId", "none");
        data.particleColor = nbt.getInt("ParticleColor", 0xFF0000);
        data.mileage = nbt.getDouble("Mileage", 0.0);
        data.gaiMode = nbt.getBoolean("GaiMode", false);
        String sirenRedStr = nbt.getString("SirenRedUuid", "");
        if (!sirenRedStr.isEmpty()) data.sirenRedUuid = UUID.fromString(sirenRedStr);
        String sirenBlueStr = nbt.getString("SirenBlueUuid", "");
        if (!sirenBlueStr.isEmpty()) data.sirenBlueUuid = UUID.fromString(sirenBlueStr);
        return data;
    }

    public UUID getGhastUuid() { return ghastUuid; }
    @Nullable public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(@Nullable UUID ownerUuid) { this.ownerUuid = ownerUuid; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public int getHarnessColor() { return harnessColor; }
    public void setHarnessColor(int harnessColor) { this.harnessColor = harnessColor; }
    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos homePos) { this.homePos = homePos; }
    public String getHomeDimension() { return homeDimension; }
    public long getFirstSeenTimestamp() { return firstSeenTimestamp; }
    public long getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(long registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
    public boolean isImpounded() { return impounded; }
    public void setImpounded(boolean impounded) { this.impounded = impounded; }
    @Nullable public UUID getFrontPlateEntityUuid() { return frontPlateEntityUuid; }
    public void setFrontPlateEntityUuid(@Nullable UUID u) { this.frontPlateEntityUuid = u; }
    @Nullable public UUID getBackPlateEntityUuid() { return backPlateEntityUuid; }
    public void setBackPlateEntityUuid(@Nullable UUID u) { this.backPlateEntityUuid = u; }
    public int getStage() { return stage; }
    public void setStage(int stage) { this.stage = stage; }
    public int getMaxStage() { return maxStage; }
    public void setMaxStage(int maxStage) { this.maxStage = maxStage; }
    public String getParticleId() { return particleId; }
    public void setParticleId(String particleId) { this.particleId = particleId; }
    public int getParticleColor() { return particleColor; }
    public void setParticleColor(int particleColor) { this.particleColor = particleColor; }
    public double getMileage() { return mileage; }
    public void setMileage(double mileage) { this.mileage = mileage; }
    public void addMileage(double distance) { this.mileage += distance; }
    public boolean isGaiMode() { return gaiMode; }
    public void setGaiMode(boolean gaiMode) { this.gaiMode = gaiMode; }
    @Nullable public UUID getSirenRedUuid() { return sirenRedUuid; }
    public void setSirenRedUuid(@Nullable UUID u) { this.sirenRedUuid = u; }
    @Nullable public UUID getSirenBlueUuid() { return sirenBlueUuid; }
    public void setSirenBlueUuid(@Nullable UUID u) { this.sirenBlueUuid = u; }

    public long getRemainingTimeMs() {
        return registrationDeadline - System.currentTimeMillis();
    }

    public String getFormattedRemainingTime() {
        long remaining = getRemainingTimeMs();
        if (remaining <= 0) return "\u041F\u0440\u043E\u0441\u0440\u043E\u0447\u0435\u043D\u043E";
        long seconds = remaining / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
