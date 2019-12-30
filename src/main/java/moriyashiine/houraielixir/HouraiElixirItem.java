package moriyashiine.houraielixir;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HouraiElixirItem extends Item {
	HouraiElixirItem() {
		super(new Properties().group(ItemGroup.MISC).rarity(Rarity.EPIC).maxStackSize(1));
	}
	
	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
		return Items.POTION.onItemRightClick(world, player, hand);
	}
	
	@Override
	@Nonnull
	public UseAction getUseAction(ItemStack stack) {
		return Items.POTION.getUseAction(stack);
	}
	
	@Override
	@Nonnull
	public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull LivingEntity entity) {
		AtomicReference<ItemStack> fin = new AtomicReference<>(super.onItemUseFinish(stack, world, entity));
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			player.getCapability(HouraiCapability.CAP).ifPresent(houraiCap -> {
				String message = HouraiElixir.MODID + (houraiCap.immortal ? ".already_immortal" : ".become_immortal");
				if (!world.isRemote) player.sendStatusMessage(new TranslationTextComponent(message), false);
				houraiCap.immortal = true;
				if (!player.isCreative()) fin.set(new ItemStack(Items.GLASS_BOTTLE));
			});
		}
		return fin.get();
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return Items.POTION.getUseDuration(stack);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltips, ITooltipFlag flags) {
		tooltips.add(new TranslationTextComponent(HouraiElixir.MODID + ".tooltip").applyTextStyle(TextFormatting.GRAY));
	}
}