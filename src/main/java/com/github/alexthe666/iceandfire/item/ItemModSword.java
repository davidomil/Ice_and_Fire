package com.github.alexthe666.iceandfire.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.alexthe666.citadel.server.entity.datatracker.EntityPropertiesHandler;
import com.github.alexthe666.citadel.server.item.CustomToolMaterial;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;

import com.github.alexthe666.iceandfire.entity.props.FrozenProperties;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemModSword extends SwordItem {

    private final CustomToolMaterial toolMaterial;

    public ItemModSword(CustomToolMaterial toolmaterial, String gameName) {
        super(toolmaterial, 3, -2.4F, new Item.Properties().group(IceAndFire.TAB_ITEMS));
        this.toolMaterial = toolmaterial;
        this.setRegistryName(IceAndFire.MODID, gameName);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        return equipmentSlot == EquipmentSlotType.MAINHAND && this.toolMaterial instanceof DragonsteelToolMaterial ? this.bakeDragonsteel() : super.getAttributeModifiers(equipmentSlot);
    }

    private Multimap<Attribute, AttributeModifier> dragonsteelModifiers;
    private Multimap<Attribute, AttributeModifier> bakeDragonsteel() {
        if(toolMaterial.getAttackDamage() != IafConfig.dragonsteelBaseDamage || dragonsteelModifiers == null){
            ImmutableMultimap.Builder<Attribute, AttributeModifier> lvt_5_1_ = ImmutableMultimap.builder();
            lvt_5_1_.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)IafConfig.dragonsteelBaseDamage - 1F, AttributeModifier.Operation.ADDITION));
            lvt_5_1_.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)-2.4, AttributeModifier.Operation.ADDITION));
            this.dragonsteelModifiers = lvt_5_1_.build();
            return this.dragonsteelModifiers;
        }else{
            return dragonsteelModifiers;
        }
    }

    public float getAttackDamage() {
        return this.toolMaterial instanceof DragonsteelToolMaterial ? (float) IafConfig.dragonsteelBaseDamage : super.getAttackDamage();
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (toolMaterial == IafItemRegistry.SILVER_TOOL_MATERIAL) {
            if (target.getCreatureAttribute() == CreatureAttribute.UNDEAD) {
                target.attackEntityFrom(DamageSource.MAGIC, this.getAttackDamage() + 3);
            }
        }
        if (this.toolMaterial == IafItemRegistry.MYRMEX_CHITIN_TOOL_MATERIAL) {
            if (target.getCreatureAttribute() != CreatureAttribute.ARTHROPOD) {
                target.attackEntityFrom(DamageSource.GENERIC, this.getAttackDamage() + 5F);
            }
            if (target instanceof EntityDeathWorm) {
                target.attackEntityFrom(DamageSource.GENERIC, this.getAttackDamage() + 5F);
            }
            if(this == IafItemRegistry.MYRMEX_DESERT_SWORD_VENOM || this == IafItemRegistry.MYRMEX_JUNGLE_SWORD_VENOM){
                target.addPotionEffect(new EffectInstance(Effects.POISON, 200, 1));
            }
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_FIRE_TOOL_MATERIAL && IafConfig.dragonWeaponFireAbility) {
            target.setFire(15);
            target.applyKnockback( 1F, attacker.getPosX() - target.getPosX(), attacker.getPosZ() - target.getPosZ());
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_ICE_TOOL_MATERIAL && IafConfig.dragonWeaponIceAbility) {
            FrozenProperties.setFrozenFor(target, 300);
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 300, 2));
            target.applyKnockback( 1F, attacker.getPosX() - target.getPosX(), attacker.getPosZ() - target.getPosZ());
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_LIGHTNING_TOOL_MATERIAL && IafConfig.dragonWeaponLightningAbility) {
            boolean flag = true;
            if(attacker instanceof PlayerEntity){
                if(((PlayerEntity)attacker).swingProgress > 0.2){
                    flag = false;
                }
            }
            if(!attacker.world.isRemote && flag){
                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.world);
                lightningboltentity.moveForced(target.getPositionVec());
                if(!target.world.isRemote){
                    target.world.addEntity(lightningboltentity);
                }
            }
            target.applyKnockback( 1F, attacker.getPosX() - target.getPosX(), attacker.getPosZ() - target.getPosZ());
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (toolMaterial == IafItemRegistry.SILVER_TOOL_MATERIAL) {
            tooltip.add(new TranslationTextComponent("silvertools.hurt").mergeStyle(TextFormatting.GREEN));
        }
        if (toolMaterial == IafItemRegistry.MYRMEX_CHITIN_TOOL_MATERIAL) {
            tooltip.add(new TranslationTextComponent("myrmextools.hurt").mergeStyle(TextFormatting.GREEN));
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_FIRE_TOOL_MATERIAL) {
            tooltip.add(new TranslationTextComponent("dragon_sword_fire.hurt2").mergeStyle(TextFormatting.DARK_RED));
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_ICE_TOOL_MATERIAL) {
            tooltip.add(new TranslationTextComponent("dragon_sword_ice.hurt2").mergeStyle(TextFormatting.AQUA));
        }
        if (toolMaterial == IafItemRegistry.DRAGONSTEEL_LIGHTNING_TOOL_MATERIAL) {
            tooltip.add(new TranslationTextComponent("dragon_sword_lightning.hurt2").mergeStyle(TextFormatting.DARK_PURPLE));
        }
    }
}
