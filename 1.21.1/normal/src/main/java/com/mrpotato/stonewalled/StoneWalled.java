package com.mrpotato.stonewalled;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

@Mod(StoneWalled.MODID)
public class StoneWalled {
    public static final String MODID = "stonewalled";
    private static final Logger LOGGER = LogUtils.getLogger();

    public StoneWalled(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[Stonewalled] Loading StoneWalled mod...");
        
        modEventBus.addListener(this::onAddPackFinders);
    }

    private void onAddPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            LOGGER.info("[Stonewalled] Registering security pack source");
            event.addRepositorySource(new SecurityPackSource());
        }
    }

    public static class SecurityPackSource implements RepositorySource {
        @Override
        public void loadPacks(java.util.function.Consumer<Pack> consumer) {
            Pack pack = PackHelper.createSecurityPack();
            if (pack != null) {
                consumer.accept(pack);
            }
        }
    }
}
