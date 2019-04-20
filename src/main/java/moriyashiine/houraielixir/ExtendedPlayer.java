package moriyashiine.houraielixir;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class ExtendedPlayer implements ICapabilitySerializable<NBTTagCompound>, IStorage<ExtendedPlayer>
{
	@CapabilityInject(ExtendedPlayer.class)
	public static final Capability<ExtendedPlayer> CAPABILITY = null;
	
	public int level;
	
	@Override
	public NBTBase writeNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, EnumFacing side)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("level", instance.level);
		return tag;
	}
	
	@Override
	public void readNBT(Capability<ExtendedPlayer> capability, ExtendedPlayer instance, EnumFacing side, NBTBase nbt)
	{
		NBTTagCompound tag = (NBTTagCompound) nbt;
		instance.level = tag.getInteger("level");
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == CAPABILITY;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		return (NBTTagCompound) CAPABILITY.getStorage().writeNBT(CAPABILITY, this, null);
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		CAPABILITY.getStorage().readNBT(CAPABILITY, this, null, nbt);
	}
	
	public static class Handler
	{
		private static final ResourceLocation CAP = new ResourceLocation(HouraiElixir.MODID, "extendedPlayer");
		
		@SubscribeEvent
		public void attachCapabilityE(AttachCapabilitiesEvent<Entity> event)
		{
			if (event.getObject() instanceof EntityPlayer) event.addCapability(CAP, new ExtendedPlayer());
		}
		
		@SubscribeEvent
		public void clonePlayer(PlayerEvent.Clone event)
		{
			event.getEntityPlayer().getCapability(ExtendedPlayer.CAPABILITY, null).deserializeNBT(event.getOriginal().getCapability(ExtendedPlayer.CAPABILITY, null).serializeNBT());
		}
		
		@SubscribeEvent
		public void playerTick(PlayerTickEvent event)
		{
			EntityPlayer player = event.player;
			World world = player.world;
			if (!world.isRemote && player.ticksExisted % 20 == 0)
			{
				ExtendedPlayer cap = player.getCapability(CAPABILITY, null);
				if (cap.level > 0)
				{
					if (player.ticksExisted % 200 == 0) player.getFoodStats().setFoodLevel(player.getFoodStats().getFoodLevel() + 1);
					if (cap.level > 1)
					{
						for (PotionEffect effect : player.getActivePotionEffects())
						{
							if (effect.getPotion().isBadEffect()) player.removePotionEffect(effect.getPotion());
						}
					}
				}
			}
		}
		
		@SubscribeEvent
		public void onLivingDeath(LivingDeathEvent event)
		{
			EntityLivingBase living = event.getEntityLiving();
			if (living instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) living;
				World world = player.world;
				if (!world.isRemote && player.posY > -64)
				{
					ExtendedPlayer cap = player.getCapability(CAPABILITY, null);
					if (cap.level > 2)
					{
						event.setCanceled(true);
						player.setHealth(1);
						world.playSound(null, player.getPosition(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.PLAYERS, 1, 1);
					}
				}
			}
		}
	}
}