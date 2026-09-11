package io.lunararcdevs.lunararc.common.mixin.core.world;

import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureTemplateManager.class)
public interface StructureTemplateManagerAccessor extends io.lunararcdevs.lunararc.common.bridge.access.StructureTemplateManagerAccessBridge {
    @Accessor("structureRepository")
    Map<ResourceLocation, Optional<StructureTemplate>> lunararc$getStructureRepository();
}
