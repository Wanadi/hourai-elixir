package moriyashiine.houraielixir;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HouraiElixir.MODID)
public class HouraiElixir {
    static final String MODID = "houraielixir";
    
    public HouraiElixir() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        HouraiCapability.register();
    }
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new HouraiElixirItem().setRegistryName(MODID, "hourai_elixir"));
        }
    }
}
