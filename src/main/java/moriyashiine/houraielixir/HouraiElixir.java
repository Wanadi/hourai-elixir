package moriyashiine.houraielixir;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HouraiElixir.MODID, name = HouraiElixir.NAME, version = HouraiElixir.VERSION)
public class HouraiElixir
{
	public static final String MODID = "houraielixir", NAME = "Hourai Elixir", VERSION = "1.0";
	
	@SidedProxy(serverSide = "moriyashiine.houraielixir.CommonProxy", clientSide = "moriyashiine.houraielixir.ClientProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}
}