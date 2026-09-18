package io.github.flemmli97.mobbattle.neoforge.data;

import io.github.flemmli97.mobbattle.MobBattle;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;

import java.util.concurrent.CompletableFuture;

public class EntityTagGen extends TagsProvider<EntityType<?>> {

    public EntityTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, MobBattle.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(MobBattle.HURT_IGNORED)
                .add(EntityTypeIds.VEX);
    }

    @Override
    public String getName() {
        return "Entity Tags";
    }
}
