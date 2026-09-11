package io.lunararcdevs.lunararc.common.bridge.access;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface StructureTemplateAccessBridge {
    List<StructureTemplate.Palette> lunararc$getPalettes();
    List<StructureTemplate.StructureEntityInfo> lunararc$getEntityInfoList();
}
