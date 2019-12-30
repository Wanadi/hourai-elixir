package moriyashiine.houraielixir;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings({"NullableProblems", "ConstantConditions"})
class ItemHouraiElixir extends Item {
	ItemHouraiElixir() {
		super();
		String name = "hourai_elixir";
		setRegistryName(name);
		setTranslationKey(HouraiElixir.MODID + "." + name);
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		return Items.POTIONITEM.onItemRightClick(world, player, hand);
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return Items.POTIONITEM.getItemUseAction(stack);
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			HouraiCapability cap = player.getCapability(HouraiCapability.CAP, null);
			String message = cap.immortal ? "houraielixir.already_immortal" : "houraielixir.become_immortal";
			if (!world.isRemote) player.sendStatusMessage(new TextComponentTranslation(message), false);
			cap.immortal = true;
			if (!player.isCreative()) return new ItemStack(Items.GLASS_BOTTLE);
		}
		return super.onItemUseFinish(stack, world, entity);
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Items.POTIONITEM.getMaxItemUseDuration(stack);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("houraielixir.tooltip"));
	}
}