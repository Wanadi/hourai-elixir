package moriyashiine.houraielixir;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = HouraiElixir.MODID, name = HouraiElixir.NAME, version = HouraiElixir.VERSION)
public class HouraiElixir {
	static final String MODID = "houraielixir", NAME = "Hourai Elixir", VERSION = "1.0.2.2";
	
	@SidedProxy(serverSide = "moriyashiine." + MODID + ".ServerProxy", clientSide = "moriyashiine." + MODID + ".ClientProxy")
	static ServerProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(HouraiCapability.class, new HouraiCapability(), HouraiCapability::new);
		MinecraftForge.EVENT_BUS.register(new HouraiCapability.Handler());
	}
	
	@Mod.EventBusSubscriber
	static class Registry {
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			Item item = new ItemHouraiElixir();
			event.getRegistry().register(item);
			proxy.registerTexture(item);
		}
	}
}