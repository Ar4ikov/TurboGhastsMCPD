package com.happyghast.gai.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GaiConfig {

    private int registrationCostEmeralds = 5;
    private int registrationCostDiamonds = 1;
    private int impoundReleaseCostEmeralds = 10;
    private int impoundReleaseCostDiamonds = 2;
    private int renameCostEmeralds = 3;
    private int renameCostDiamonds = 0;
    private int stage2CostEmeralds = 5;
    private int stage2CostDiamonds = 2;
    private int stage3CostEmeralds = 10;
    private int stage3CostDiamonds = 5;
    private int particleCostEmeralds = 3;
    private int particleCostDiamonds = 1;
    private long registrationPeriodMs = 86400000L;
    private long gracePeriodMs = 172800000L;
    @Nullable private BlockPos impoundCorner1 = null;
    @Nullable private BlockPos impoundCorner2 = null;
    private String impoundDimension = "minecraft:overworld";
    private boolean graceActive = true;
    private long graceStartTimestamp = 0;

    private int statRegEmeralds = 0;
    private int statRegDiamonds = 0;
    private int statRegCount = 0;
    private int statRenameEmeralds = 0;
    private int statRenameDiamonds = 0;
    private int statRenameCount = 0;
    private int statStageEmeralds = 0;
    private int statStageDiamonds = 0;
    private int statStageCount = 0;
    private int statParticleEmeralds = 0;
    private int statParticleDiamonds = 0;
    private int statParticleCount = 0;
    private int statReleaseEmeralds = 0;
    private int statReleaseDiamonds = 0;
    private int statReleaseCount = 0;

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("RegCostEmeralds", registrationCostEmeralds);
        nbt.putInt("RegCostDiamonds", registrationCostDiamonds);
        nbt.putInt("ReleaseCostEmeralds", impoundReleaseCostEmeralds);
        nbt.putInt("ReleaseCostDiamonds", impoundReleaseCostDiamonds);
        nbt.putInt("RenameCostEmeralds", renameCostEmeralds);
        nbt.putInt("RenameCostDiamonds", renameCostDiamonds);
        nbt.putInt("Stage2CostEmeralds", stage2CostEmeralds);
        nbt.putInt("Stage2CostDiamonds", stage2CostDiamonds);
        nbt.putInt("Stage3CostEmeralds", stage3CostEmeralds);
        nbt.putInt("Stage3CostDiamonds", stage3CostDiamonds);
        nbt.putInt("ParticleCostEmeralds", particleCostEmeralds);
        nbt.putInt("ParticleCostDiamonds", particleCostDiamonds);
        nbt.putLong("RegPeriodMs", registrationPeriodMs);
        nbt.putLong("GracePeriodMs", gracePeriodMs);
        if (impoundCorner1 != null) {
            nbt.putInt("IC1X", impoundCorner1.getX());
            nbt.putInt("IC1Y", impoundCorner1.getY());
            nbt.putInt("IC1Z", impoundCorner1.getZ());
            nbt.putBoolean("IC1Set", true);
        }
        if (impoundCorner2 != null) {
            nbt.putInt("IC2X", impoundCorner2.getX());
            nbt.putInt("IC2Y", impoundCorner2.getY());
            nbt.putInt("IC2Z", impoundCorner2.getZ());
            nbt.putBoolean("IC2Set", true);
        }
        nbt.putString("ImpoundDim", impoundDimension);
        nbt.putBoolean("GraceActive", graceActive);
        nbt.putLong("GraceStartTs", graceStartTimestamp);
        nbt.putInt("StatRegE", statRegEmeralds);
        nbt.putInt("StatRegD", statRegDiamonds);
        nbt.putInt("StatRegC", statRegCount);
        nbt.putInt("StatRenE", statRenameEmeralds);
        nbt.putInt("StatRenD", statRenameDiamonds);
        nbt.putInt("StatRenC", statRenameCount);
        nbt.putInt("StatStgE", statStageEmeralds);
        nbt.putInt("StatStgD", statStageDiamonds);
        nbt.putInt("StatStgC", statStageCount);
        nbt.putInt("StatPrtE", statParticleEmeralds);
        nbt.putInt("StatPrtD", statParticleDiamonds);
        nbt.putInt("StatPrtC", statParticleCount);
        nbt.putInt("StatRelE", statReleaseEmeralds);
        nbt.putInt("StatRelD", statReleaseDiamonds);
        nbt.putInt("StatRelC", statReleaseCount);
        return nbt;
    }

    public static GaiConfig fromNbt(NbtCompound nbt) {
        GaiConfig config = new GaiConfig();
        config.registrationCostEmeralds = nbt.getInt("RegCostEmeralds", 5);
        config.registrationCostDiamonds = nbt.getInt("RegCostDiamonds", 1);
        config.impoundReleaseCostEmeralds = nbt.getInt("ReleaseCostEmeralds", 10);
        config.impoundReleaseCostDiamonds = nbt.getInt("ReleaseCostDiamonds", 2);
        config.renameCostEmeralds = nbt.getInt("RenameCostEmeralds", 3);
        config.renameCostDiamonds = nbt.getInt("RenameCostDiamonds", 0);
        config.stage2CostEmeralds = nbt.getInt("Stage2CostEmeralds", 5);
        config.stage2CostDiamonds = nbt.getInt("Stage2CostDiamonds", 2);
        config.stage3CostEmeralds = nbt.getInt("Stage3CostEmeralds", 10);
        config.stage3CostDiamonds = nbt.getInt("Stage3CostDiamonds", 5);
        config.particleCostEmeralds = nbt.getInt("ParticleCostEmeralds", 3);
        config.particleCostDiamonds = nbt.getInt("ParticleCostDiamonds", 1);
        config.registrationPeriodMs = nbt.getLong("RegPeriodMs", 86400000L);
        config.gracePeriodMs = nbt.getLong("GracePeriodMs", 172800000L);
        if (nbt.getBoolean("IC1Set", false)) {
            config.impoundCorner1 = new BlockPos(nbt.getInt("IC1X", 0), nbt.getInt("IC1Y", 0), nbt.getInt("IC1Z", 0));
        }
        if (nbt.getBoolean("IC2Set", false)) {
            config.impoundCorner2 = new BlockPos(nbt.getInt("IC2X", 0), nbt.getInt("IC2Y", 0), nbt.getInt("IC2Z", 0));
        }
        config.impoundDimension = nbt.getString("ImpoundDim", "minecraft:overworld");
        config.graceActive = nbt.getBoolean("GraceActive", true);
        config.graceStartTimestamp = nbt.getLong("GraceStartTs", 0L);
        config.statRegEmeralds = nbt.getInt("StatRegE", 0);
        config.statRegDiamonds = nbt.getInt("StatRegD", 0);
        config.statRegCount = nbt.getInt("StatRegC", 0);
        config.statRenameEmeralds = nbt.getInt("StatRenE", 0);
        config.statRenameDiamonds = nbt.getInt("StatRenD", 0);
        config.statRenameCount = nbt.getInt("StatRenC", 0);
        config.statStageEmeralds = nbt.getInt("StatStgE", 0);
        config.statStageDiamonds = nbt.getInt("StatStgD", 0);
        config.statStageCount = nbt.getInt("StatStgC", 0);
        config.statParticleEmeralds = nbt.getInt("StatPrtE", 0);
        config.statParticleDiamonds = nbt.getInt("StatPrtD", 0);
        config.statParticleCount = nbt.getInt("StatPrtC", 0);
        config.statReleaseEmeralds = nbt.getInt("StatRelE", 0);
        config.statReleaseDiamonds = nbt.getInt("StatRelD", 0);
        config.statReleaseCount = nbt.getInt("StatRelC", 0);
        return config;
    }

    public int getRegistrationCostEmeralds() { return registrationCostEmeralds; }
    public void setRegistrationCostEmeralds(int cost) { this.registrationCostEmeralds = cost; }
    public int getRegistrationCostDiamonds() { return registrationCostDiamonds; }
    public void setRegistrationCostDiamonds(int cost) { this.registrationCostDiamonds = cost; }
    public int getImpoundReleaseCostEmeralds() { return impoundReleaseCostEmeralds; }
    public void setImpoundReleaseCostEmeralds(int cost) { this.impoundReleaseCostEmeralds = cost; }
    public int getImpoundReleaseCostDiamonds() { return impoundReleaseCostDiamonds; }
    public void setImpoundReleaseCostDiamonds(int cost) { this.impoundReleaseCostDiamonds = cost; }
    public long getRegistrationPeriodMs() { return registrationPeriodMs; }
    public void setRegistrationPeriodMs(long ms) { this.registrationPeriodMs = ms; }
    public long getGracePeriodMs() { return gracePeriodMs; }
    public void setGracePeriodMs(long ms) { this.gracePeriodMs = ms; }
    @Nullable public BlockPos getImpoundCorner1() { return impoundCorner1; }
    public void setImpoundCorner1(@Nullable BlockPos corner) { this.impoundCorner1 = corner; }
    @Nullable public BlockPos getImpoundCorner2() { return impoundCorner2; }
    public void setImpoundCorner2(@Nullable BlockPos corner) { this.impoundCorner2 = corner; }
    public String getImpoundDimension() { return impoundDimension; }
    public void setImpoundDimension(String dim) { this.impoundDimension = dim; }
    public boolean isGraceActive() { return graceActive; }
    public void setGraceActive(boolean active) { this.graceActive = active; }
    public long getGraceStartTimestamp() { return graceStartTimestamp; }
    public void setGraceStartTimestamp(long ts) { this.graceStartTimestamp = ts; }

    public int getRenameCostEmeralds() { return renameCostEmeralds; }
    public void setRenameCostEmeralds(int cost) { this.renameCostEmeralds = cost; }
    public int getRenameCostDiamonds() { return renameCostDiamonds; }
    public void setRenameCostDiamonds(int cost) { this.renameCostDiamonds = cost; }

    public int getStage2CostEmeralds() { return stage2CostEmeralds; }
    public void setStage2CostEmeralds(int cost) { this.stage2CostEmeralds = cost; }
    public int getStage2CostDiamonds() { return stage2CostDiamonds; }
    public void setStage2CostDiamonds(int cost) { this.stage2CostDiamonds = cost; }
    public int getStage3CostEmeralds() { return stage3CostEmeralds; }
    public void setStage3CostEmeralds(int cost) { this.stage3CostEmeralds = cost; }
    public int getStage3CostDiamonds() { return stage3CostDiamonds; }
    public void setStage3CostDiamonds(int cost) { this.stage3CostDiamonds = cost; }
    public int getParticleCostEmeralds() { return particleCostEmeralds; }
    public void setParticleCostEmeralds(int cost) { this.particleCostEmeralds = cost; }
    public int getParticleCostDiamonds() { return particleCostDiamonds; }
    public void setParticleCostDiamonds(int cost) { this.particleCostDiamonds = cost; }

    public boolean isImpoundZoneConfigured() {
        return impoundCorner1 != null && impoundCorner2 != null;
    }

    public void recordTransaction(String category, int emeralds, int diamonds) {
        switch (category) {
            case "registration" -> { statRegEmeralds += emeralds; statRegDiamonds += diamonds; statRegCount++; }
            case "rename" -> { statRenameEmeralds += emeralds; statRenameDiamonds += diamonds; statRenameCount++; }
            case "stage" -> { statStageEmeralds += emeralds; statStageDiamonds += diamonds; statStageCount++; }
            case "particle" -> { statParticleEmeralds += emeralds; statParticleDiamonds += diamonds; statParticleCount++; }
            case "release" -> { statReleaseEmeralds += emeralds; statReleaseDiamonds += diamonds; statReleaseCount++; }
        }
    }

    public int getTotalEmeraldsSpent() {
        return statRegEmeralds + statRenameEmeralds + statStageEmeralds + statParticleEmeralds + statReleaseEmeralds;
    }
    public int getTotalDiamondsSpent() {
        return statRegDiamonds + statRenameDiamonds + statStageDiamonds + statParticleDiamonds + statReleaseDiamonds;
    }
    public int getTotalTransactions() {
        return statRegCount + statRenameCount + statStageCount + statParticleCount + statReleaseCount;
    }
    public int getStatRegEmeralds() { return statRegEmeralds; }
    public int getStatRegDiamonds() { return statRegDiamonds; }
    public int getStatRegCount() { return statRegCount; }
    public int getStatRenameEmeralds() { return statRenameEmeralds; }
    public int getStatRenameDiamonds() { return statRenameDiamonds; }
    public int getStatRenameCount() { return statRenameCount; }
    public int getStatStageEmeralds() { return statStageEmeralds; }
    public int getStatStageDiamonds() { return statStageDiamonds; }
    public int getStatStageCount() { return statStageCount; }
    public int getStatParticleEmeralds() { return statParticleEmeralds; }
    public int getStatParticleDiamonds() { return statParticleDiamonds; }
    public int getStatParticleCount() { return statParticleCount; }
    public int getStatReleaseEmeralds() { return statReleaseEmeralds; }
    public int getStatReleaseDiamonds() { return statReleaseDiamonds; }
    public int getStatReleaseCount() { return statReleaseCount; }

    public long getDeadlinePeriodForNewGhast() {
        if (graceActive) {
            long graceEnd = graceStartTimestamp + gracePeriodMs;
            long remaining = graceEnd - System.currentTimeMillis();
            if (remaining > 0) {
                return Math.max(remaining, registrationPeriodMs);
            }
        }
        return registrationPeriodMs;
    }
}
