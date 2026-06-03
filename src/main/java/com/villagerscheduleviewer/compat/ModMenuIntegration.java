package com.villagerscheduleviewer.compat;

import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import com.villagerscheduleviewer.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class ModMenuIntegration implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FabricLoader.getInstance().isModLoaded("cloth-config")
                ? ClothConfigIntegration.create(parent)
                : new ConfigScreen(parent, VillagerScheduleViewerClient.CONFIG);
    }
}
