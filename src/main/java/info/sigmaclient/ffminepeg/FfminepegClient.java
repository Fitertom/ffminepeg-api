package info.sigmaclient.ffminepeg;

import info.sigmaclient.ffminepeg.api.FfminepegApi;
import net.fabricmc.api.ClientModInitializer;

public final class FfminepegClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Trigger extraction early so dependent mods can use the binary immediately.
        FfminepegApi.ensureReady();
    }
}
