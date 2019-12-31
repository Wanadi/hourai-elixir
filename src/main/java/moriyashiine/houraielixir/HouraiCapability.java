package moriyashiine.houraielixir;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class HouraiCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<HouraiCapability> {
	@CapabilityInject(HouraiCapability.class)
	static final Capability<HouraiCapability> CAP = null;
	
	boolean immortal = false;
	private int timer = 0;
	
	@Nullable
	@Override
	public NBTBase writeNBT(Capability<HouraiCapability> capability, HouraiCapability instance, EnumFacing side) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("immortal", instance.immortal);
		tag.setInteger("timer", instance.timer);
		return tag;
	}
	
	@Override
	public void readNBT(Capability<HouraiCapability> capability, HouraiCapability instance, EnumFacing side, NBTBase nbt) {
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
		return capability == CAP ? CAP.cast(this) : null;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		return (NBTTagCompound) CAP.getStorage().writeNBT(CAP, this, null);
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		CAP.getStorage().readNBT(CAP, this, null, nbt);
	}
	
	static class Handler {
		private static final ResourceLocation KEY = new ResourceLocation(HouraiElixir.MODID, "hourai_data");
		
		@SubscribeEvent
		public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof EntityPlayer) event.addCapability(KEY, new HouraiCapability());
		}
		
		@SubscribeEvent
		public void clonePlayer(PlayerEvent.Clone event) {
			event.getEntityPlayer().getCapability(CAP, null).deserializeNBT(event.getOriginal().getCapability(CAP, null).serializeNBT());
		}
		
		@SubscribeEvent
		public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
			EntityLivingBase entity = event.getEntityLiving();
			if (!entity.world.isRemote && entity.ticksExisted % 20 == 0) {
				HouraiCapability houraiCap = entity.getCapability(CAP, null);
				if (houraiCap != null && houraiCap.timer > 0) {
					houraiCap.timer = (byte) MathHelper.clamp(--houraiCap.timer, 0, 80);
					entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 0, true, false));
					if (houraiCap.timer >= 20) {
						entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, true, false));
						entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 0, true, false));
						if (houraiCap.timer >= 40) {
							entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, true, false));
							entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1, true, false));
							entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 0, true, false));
							if (houraiCap.timer >= 60) {
								entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 2, true, false));
								entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 2, true, false));
								entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 1, true, false));
							}
						}
					}
				}
			}
		}
		
		@SubscribeEvent
		public void potionApplicable(PotionEvent.PotionApplicableEvent event) {
			EntityLivingBase entity = event.getEntityLiving();
			if (!entity.world.isRemote) {
				HouraiCapability houraiCap = entity.getCapability(CAP, null);
				if (houraiCap != null && houraiCap.immortal && houraiCap.timer <= 0 && event.getPotionEffect().getPotion().isBadEffect()) event.setResult(Event.Result.DENY);
			}
		}
		
		@SuppressWarnings("ConstantConditions")
		@SubscribeEvent
		public void livingDamage(LivingDamageEvent event) {
			EntityLivingBase entity = event.getEntityLiving();
			World world = entity.world;
			if (!world.isRemote) {
				HouraiCapability houraiCap = entity.getCapability(CAP, null);
				if (houraiCap != null) {
					if (houraiCap.immortal && entity.getHealth() - event.getAmount() <= 0) {
						event.setCanceled(true);
						world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.PLAYERS, 1, 1);
						entity.heal(Float.MAX_VALUE);
						houraiCap.timer += 20;
						if (entity.getPosition().getY() <= -64 && event.getSource() == DamageSource.OUT_OF_WORLD && entity instanceof EntityPlayerMP) {
							EntityPlayerMP player = (EntityPlayerMP) entity;
							int spawnDimension = player.getSpawnDimension();
							WorldServer serverWorld = DimensionManager.getWorld(spawnDimension);
							if (serverWorld != null) {
								BlockPos bed = player.getBedLocation(spawnDimension);
								BlockPos spawnPos = bed == null ? serverWorld.getSpawnPoint() : bed;
								player.changeDimension(spawnDimension, (world1, entity1, yaw) -> {});
								player.setPositionAndUpdate(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
								while (player.isEntityInsideOpaqueBlock()) player.setPositionAndUpdate(player.posX, player.posY + 1, player.posZ);
								player.setPositionAndUpdate(player.posX, player.posY + 1, player.posZ);
								player.motionX = player.motionY = player.motionZ = 0;
								player.fallDistance = 0;
								player.connection.sendPacket(new SPacketEntityVelocity(player));
							}
						}
					}
				}
			}
		}
	}
}