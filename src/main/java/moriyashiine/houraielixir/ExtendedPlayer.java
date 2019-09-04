package moriyashiine.houraielixir;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("ConstantConditions")
public class ExtendedPlayer implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<ExtendedPlayer>
{
	@CapabilityInject(ExtendedPlayer.class)
	static final Capability<ExtendedPlayer> CAPABILITY = null;
	
	boolean immortal = false;
	private int timer = 0;
	
	@Nullable
	@Override
	public NBTBase writeNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, EnumFacing side) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("immortal", instance.immortal);
		tag.setInteger("timer", instance.timer);
		return tag;
	}
	
	@Override
	public void readNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound tag = (NBTTagCompound) nbt;
		if (tag.hasKey("immortal")) instance.immortal = tag.getBoolean("immortal");
		if (tag.hasKey("timer")) instance.timer = tag.getInteger("timer");
	}
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return getCapability(capability, facing) != null;
	}
	
	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		return (NBTTagCompound) CAPABILITY.getStorage().writeNBT(CAPABILITY, this, null);
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, this, null, nbt);
	}
	
	public static class Handler
	{
		private static final ResourceLocation LOC = new ResourceLocation(HouraiElixir.MODID, "extended_player");
		
		@SubscribeEvent
		public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof EntityPlayer) event.addCapability(LOC, new ExtendedPlayer());
		}
		
		@SubscribeEvent
		public void clonePlayer(PlayerEvent.Clone event) {
			event.getEntityPlayer().getCapability(CAPABILITY, null).deserializeNBT(event.getOriginal().getCapability(CAPABILITY, null).serializeNBT());
		}
		
		@SubscribeEvent
		public void livingUpdate(LivingEvent.LivingUpdateEvent event)
		{
			EntityLivingBase living = event.getEntityLiving();
			World world = living.world;
			if (!world.isRemote && living.ticksExisted % 20 == 0 && living instanceof EntityPlayer) {
				ExtendedPlayer cap = living.getCapability(CAPABILITY, null);
				if (cap.immortal) {
					if (cap.timer == 0) {
						Collection<PotionEffect> effects = living.getActivePotionEffects();
						living.clearActivePotions();
						for (PotionEffect effect : effects) if (!effect.getPotion().isBadEffect()) living.addPotionEffect(effect);
					}
					else {
						if (cap.timer > 0) {
							cap.timer = MathHelper.clamp(cap.timer, 0, 80);
							cap.timer--;
							living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 0, false, false));
							if (cap.timer >= 20) {
								living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, false, false));
								living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 0, false, false));
								if (cap.timer >= 40) {
									living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, false, false));
									living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1));
									living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 0, false, false));
									if (cap.timer >= 60) {
										living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 2, false, false));
										living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 2, false, false));
										living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 1, false, false));
									}
								}
							}
						}
					}
				}
			}
		}
		
		@SubscribeEvent
		public void livingDamage(LivingDamageEvent event) {
			EntityLivingBase entity = event.getEntityLiving();
			if (entity instanceof EntityPlayer) {
				World world = entity.world;
				if (!world.isRemote) {
					ExtendedPlayer cap = entity.getCapability(CAPABILITY, null);
					if (cap.immortal && entity.getHealth() - event.getAmount() <= 0) {
						if (entity.posY > -64) {
							event.setCanceled(true);
							entity.heal(entity.getMaxHealth() / 2);
							world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.PLAYERS, 1, 1);
							cap.timer += 20;
						}
						else cap.timer = 0;
					}
				}
			}
		}
	}
}