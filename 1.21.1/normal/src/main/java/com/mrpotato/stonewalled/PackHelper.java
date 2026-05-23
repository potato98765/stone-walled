package com.mrpotato.stonewalled;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class PackHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Pack createSecurityPack() {
        try {
            // Find security_assets directory inside the mod jar
            Path assetsPath = ModList.get().getModContainerById(StoneWalled.MODID)
                    .map(container -> container.getModInfo().getOwningFile().getFile().findResource("security_assets"))
                    .orElse(null);

            if (assetsPath == null || !Files.exists(assetsPath)) {
                LOGGER.error("[Stonewalled] Could not find security_assets folder in mod jar");
                return null;
            }

            LOGGER.info("[Stonewalled] Found security_assets at: {}", assetsPath);

            PackLocationInfo locationInfo = new PackLocationInfo(
                    "stonewalled:enforced_pack",
                    Component.literal("StoneWalled Anti-Xray"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            Pack.ResourcesSupplier resourcesSupplier = new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo location) {
                    return new PathPackResources(location, assetsPath);
                }

                @Override
                public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                    return new PathPackResources(location, assetsPath);
                }
            };

            // required=true: rebuildSelected auto-inserts this pack via its required-pack loop
            // fixedPosition=true: no toggle in the UI (belt-and-suspenders)
            PackSelectionConfig selectionConfig = new PackSelectionConfig(
                    true,          // required — auto-selects via rebuildSelected required-pack loop
                    Pack.Position.TOP,
                    true           // fixedPosition
            );

            Pack pack = Pack.readMetaAndCreate(
                    locationInfo,
                    resourcesSupplier,
                    PackType.CLIENT_RESOURCES,
                    selectionConfig
            );

            if (pack == null) {
                LOGGER.error("[Stonewalled] Pack.readMetaAndCreate returned null — check pack.mcmeta format");
                return null;
            }

            LOGGER.info("[Stonewalled] Security pack created: id={}, required={}", pack.getId(), pack.isRequired());

            // hidden() makes the pack invisible in the Resource Pack screen UI.
            // expandAndRemoveRootChildren (called in rebuildSelected) skips hidden packs
            // from the UI stream, so it never appears in Available or Enabled tabs.
            // However, rebuildSelected's required-pack loop still iterates available.values()
            // directly and auto-inserts this pack because isRequired() returns true.
            return pack.hidden();

        } catch (Exception e) {
            LOGGER.error("[Stonewalled] Failed to build security pack", e);
            return null;
        }
    }
}
