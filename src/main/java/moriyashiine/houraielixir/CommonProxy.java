package moriyashiine.houraielixir;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = HouraiElixir.MODID)
public class CommonProxy
{
	protected static final Item elixir = new ItemHouraiElixir();
	
	public void preInit(FMLPreInitializationEvent event)
	{
		CapabilityManager.INSTANCE.register(ExtendedPlayer.class, new ExtendedPlayer(), ExtendedPlayer::new);
	}
	
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new ExtendedPlayer.Handler());
	}
	
	public void registerTexture(Item item)
	{
	}
	
	@SubscribeEvent
	public static void registerItems(Register<Item> event)
	{
		event.getRegistry().register(elixir);
		HouraiElixir.proxy.registerTexture(elixir);
	}
}