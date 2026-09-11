package io.lunararcdevs.lunararc.common.mixin.core.world;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureTemplate.class)
public interface StructureTemplateAccessor extends io.lunararcdevs.lunararc.common.bridge.access.StructureTemplateAccessBridge {
    @Accessor("palettes")
    List<StructureTemplate.Palette> lunararc$getPalettes();

    @Accessor("entityInfoList")
    List<StructureTemplate.StructureEntityInfo> lunararc$getEntityInfoList();
}
