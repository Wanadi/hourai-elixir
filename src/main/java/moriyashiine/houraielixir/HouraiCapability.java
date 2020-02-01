package moriyashiine.houraielixir;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HouraiCapability implements ICapabilitySerializable<CompoundNBT> {
	@CapabilityInject(HouraiCapability.class)
	static Capability<HouraiCapability> CAP = null;
	
	boolean immortal = false;
	private byte timer = 0;
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return CAP.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) CAP.writeNBT(this, null);
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		CAP.readNBT(this, null, nbt);
	}
	
	static void register() {
		CapabilityManager.INSTANCE.register(HouraiCapability.class, new HouraiCapability.Storage(), HouraiCapability::new);
		MinecraftForge.EVENT_BUS.register(new Handler());
	}
	
	private static class Storage implements Capability.IStorage<HouraiCapability> {
		@Nullable
		@Override
		public INBT writeNBT(Capability<HouraiCapability> capability, HouraiCapability instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean("immortal", instance.immortal);
			tag.putByte("timer", instance.timer);
			return tag;
		}
		
		@Override
		public void readNBT(Capability<HouraiCapability> capability, HouraiCapability instance, Direction side, INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.immortal = tag.getBoolean("immortal");
			instance.timer = tag.getByte("timer");
		}
	}
	
	private static class Handler {
		private static final ResourceLocation KEY = new ResourceLocation(HouraiElixir.MODID, "hourai_data");
		
		@SubscribeEvent
		public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof PlayerEntity) event.addCapability(KEY, new HouraiCapability());
		}
		
		@SubscribeEvent
		public void clonePlayer(PlayerEvent.Clone event) {
			event.getPlayer().getCapability(CAP).ifPresent(c -> event.getOriginal().getCapability(CAP).ifPresent(e -> c.deserializeNBT(e.serializeNBT())));
		}
		
		@SubscribeEvent
		public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
			LivingEntity entity = event.getEntityLiving();
			if (!entity.world.isRemote && entity.ticksExisted % 20 == 0) {
				entity.getCapability(CAP).ifPresent(houraiCap -> {
					if (houraiCap.timer > 0) {
						houraiCap.timer = (byte) MathHelper.clamp(--houraiCap.timer, 0, 80);
						entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 80, 0, true, false));
						if (houraiCap.timer >= 20) {
							entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 80, 1, true, false));
							entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 80, 0, true, false));
							if (houraiCap.timer >= 40) {
								entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 80, 1, true, false));
								entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 80, 1, true, false));
								entity.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 80, 0, true, false));
								if (houraiCap.timer >= 60) {
									entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 80, 2, true, false));
									entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 80, 2, true, false));
									entity.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 80, 1, true, false));
								}
							}
						}
					}
				});
			}
		}
		
		@SubscribeEvent
		public void potionApplicable(PotionEvent.PotionApplicableEvent event) {
			LivingEntity entity = event.getEntityLiving();
			if (!entity.world.isRemote) {
				entity.getCapability(CAP).ifPresent(houraiCap -> {
					if (houraiCap.immortal && houraiCap.timer <= 0 && !event.getPotionEffect().getPotion().isBeneficial()) event.setResult(Event.Result.DENY);
				});
			}
		}
		
		@SuppressWarnings("ConstantConditions")
		@SubscribeEvent
		public void livingDamage(LivingDamageEvent event) {
			LivingEntity entity = event.getEntityLiving();
			World world = entity.world;
			if (!world.isRemote) {
				entity.getCapability(CAP).ifPresent(houraiCap -> {
					if (houraiCap.immortal && entity.getHealth() - event.getAmount() <= 0) {
						event.setCanceled(true);
						world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.PLAYERS, 1, 1);
						entity.heal(Float.MAX_VALUE);
						houraiCap.timer += 20;
						if (entity.getPosition().getY() <= -64 && event.getSource() == DamageSource.OUT_OF_WORLD && entity instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) entity;
							ServerWorld serverWorld = DimensionManager.getWorld(player.server, player.getSpawnDimension(), true, true);
							if (serverWorld != null) {
								BlockPos bed = player.getBedLocation(player.getSpawnDimension());
								BlockPos spawnPos = bed == null ? serverWorld.getSpawnPoint() : bed;
								player.func_200619_a(serverWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.rotationYaw, player.rotationPitch);
							}
						}
					}
				});
			}
		}
	}
}