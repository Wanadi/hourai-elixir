package moriyashiine.houraielixir;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemHouraiElixir extends Item
{
	public ItemHouraiElixir()
	{
		setRegistryName(HouraiElixir.MODID, "hourai_elixir");
		setTranslationKey(getRegistryName().toString().replace(":", "."));
		setCreativeTab(CreativeTabs.BREWING);
		setMaxStackSize(1);
		setMaxDamage(3);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if (!world.isRemote)
		{
			ExtendedPlayer cap = player.getCapability(ExtendedPlayer.CAPABILITY, null);
			if (cap.level < 3)
			{
				player.setActiveHand(hand);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.DRINK;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase living)
	{
		if (living instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) living;
			if (!player.isCreative()) stack.damageItem(1, player);
			if (!player.world.isRemote)
			{
				ExtendedPlayer cap = player.getCapability(ExtendedPlayer.CAPABILITY, null);
				if (cap.level < 3)
				{
					int level = cap.level++;
					player.sendMessage(new TextComponentTranslation("elixir.message" + level, level));
				}
			}
		}
		return super.onItemUseFinish(stack, world, living);
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 32;
	}
}