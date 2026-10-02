package dev.smartgolems.neoforge;

import dev.smartgolems.SmartGolemsMod;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge entrypoint (client and server). Registration is in {@link SmartGolemsNeoForgeEvents}. */
@Mod(SmartGolemsMod.MOD_ID)
public class SmartGolemsNeoForge {
	public SmartGolemsNeoForge() {
		SmartGolemsMod.init(FMLPaths.CONFIGDIR.get());
	}
}
