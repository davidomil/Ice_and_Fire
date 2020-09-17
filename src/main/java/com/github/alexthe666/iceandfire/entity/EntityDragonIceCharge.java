package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.IDragonProjectile;
import com.github.alexthe666.iceandfire.misc.IafDamageRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityDragonIceCharge extends AbstractFireballEntity implements IDragonProjectile {

    public int ticksInAir;

    public EntityDragonIceCharge(EntityType type, World worldIn) {
        super(type, worldIn);

    }

    public EntityDragonIceCharge(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(IafEntityRegistry.ICE_DRAGON_CHARGE, worldIn);
    }

    public EntityDragonIceCharge(EntityType type, World worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
        super(type, posX, posY, posZ, accelX, accelY, accelZ, worldIn);
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.07D;
        this.accelerationY = accelY / d0 * 0.07D;
        this.accelerationZ = accelZ / d0 * 0.07D;
    }

    public EntityDragonIceCharge(EntityType type, World worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
        super(type, shooter, accelX, accelY, accelZ, worldIn);
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.07D;
        this.accelerationY = accelY / d0 * 0.07D;
        this.accelerationZ = accelZ / d0 * 0.07D;
    }


    protected boolean isFireballFiery() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    public void tick() {
        Entity shootingEntity = this.func_234616_v_();
        if (this.world.isRemote) {
            for (int i = 0; i < 14; ++i) {
                IceAndFire.PROXY.spawnParticle("dragonice", this.getPosX() + this.rand.nextDouble() * 1 * (this.rand.nextBoolean() ? -1 : 1), this.getPosY() + this.rand.nextDouble() * 1 * (this.rand.nextBoolean() ? -1 : 1), this.getPosZ() + this.rand.nextDouble() * 1 * (this.rand.nextBoolean() ? -1 : 1), 0.0D, 0.0D, 0.0D);
            }
        }
        if (this.world.isRemote || (shootingEntity == null || shootingEntity.isAlive()) && this.world.isBlockLoaded(this.func_233580_cy_())) {
            super.tick();
            ++this.ticksInAir;
            Vector3d Vector3d = this.getMotion();
            RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::func_230298_a_, RayTraceContext.BlockMode.COLLIDER);

            if (!world.isRemote && raytraceresult != null) {
                this.onImpact(raytraceresult);
            }

            double d0 = this.getPosX() + Vector3d.x;
            double d1 = this.getPosY() + Vector3d.y;
            double d2 = this.getPosZ() + Vector3d.z;
            float f = MathHelper.sqrt(horizontalMag(Vector3d));
            this.rotationYaw = (float) (MathHelper.atan2(Vector3d.x, Vector3d.z) * (double) (180F / (float) Math.PI));
            for (this.rotationPitch = (float) (MathHelper.atan2(Vector3d.y, f) * (double) (180F / (float) Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            }
            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
            this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
            float f1 = 0.99F;
            float f2 = 0.06F;


            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    this.world.addParticle(ParticleTypes.BUBBLE, this.getPosX() - this.getMotion().x * 0.25D, this.getPosY() - this.getMotion().y * 0.25D, this.getPosZ() - this.getMotion().z * 0.25D, this.getMotion().x, this.getMotion().y, this.getMotion().z);
                }

                f = 0.8F;
            }
            this.setPosition(d0, d1, d2);
            this.world.addParticle(this.getParticle(), this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
            this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
        } else {
            this.remove();
        }
    }

    @Override
    protected void onImpact(RayTraceResult movingObject) {
        Entity shootingEntity = this.func_234616_v_();
        boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
        if (!this.world.isRemote) {
            if (movingObject.getType() == RayTraceResult.Type.ENTITY) {
                Entity entity = ((EntityRayTraceResult) movingObject).getEntity();

                if (entity != null && entity instanceof IDragonProjectile) {
                    return;
                }
                if (entity != null && shootingEntity != null && shootingEntity instanceof EntityDragonBase && entity != null) {
                    EntityDragonBase dragon = (EntityDragonBase) shootingEntity;
                    if (dragon.isOnSameTeam(entity) || dragon.isEntityEqual(entity) || dragon.isPart(entity)) {
                        return;
                    }
                }
                if (entity == null || !(entity instanceof IDragonProjectile) && entity != shootingEntity && shootingEntity instanceof EntityDragonBase) {
                    EntityDragonBase dragon = (EntityDragonBase) shootingEntity;
                    if (shootingEntity != null && (entity == shootingEntity || (entity instanceof TameableEntity && ((EntityDragonBase) shootingEntity).isOwner(((EntityDragonBase) shootingEntity).getOwner())))) {
                        return;
                    }
                    if (dragon != null) {
                        dragon.randomizeAttacks();
                    }
                    this.remove();
                }
                if (entity != null && !(entity instanceof IDragonProjectile) && !entity.isEntityEqual(shootingEntity)) {
                    if (shootingEntity != null && (entity.isEntityEqual(shootingEntity) || (shootingEntity instanceof EntityDragonBase & entity instanceof TameableEntity && ((EntityDragonBase) shootingEntity).getOwner() == ((TameableEntity) entity).getOwner()))) {
                        return;
                    }
                    if (shootingEntity != null && shootingEntity instanceof EntityDragonBase) {
                        entity.attackEntityFrom(IafDamageRegistry.DRAGON_ICE, 10.0F);
                        if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                            ((EntityDragonBase) shootingEntity).randomizeAttacks();
                        }
                    }
                    if(shootingEntity instanceof LivingEntity){
                        this.applyEnchantments((LivingEntity)shootingEntity, entity);
                    }
                    this.remove();
                }
            }
        }
        if(movingObject.getType() != RayTraceResult.Type.MISS) {
            if (shootingEntity instanceof EntityDragonBase && IafConfig.dragonGriefing != 2) {
                IafDragonDestructionManager.destroyAreaIceCharge(world, this.func_233580_cy_(), ((EntityDragonBase) shootingEntity));
            }
            this.remove();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    public float getCollisionBorderSize() {
        return 0F;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}