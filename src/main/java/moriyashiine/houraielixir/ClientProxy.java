package moriyashiine.houraielixir;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor()
		{
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex)
			{
				EntityPlayer player = Minecraft.getMinecraft().player;
				return tintIndex == 0 && player != null ? EnumDyeColor.values()[player.ticksExisted % EnumDyeColor.values().length].getColorValue() : EnumDyeColor.CYAN.getColorValue();
			}
		}, elixir);
	}
	
	@Override
	public void registerTexture(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "normal"));
	}
}