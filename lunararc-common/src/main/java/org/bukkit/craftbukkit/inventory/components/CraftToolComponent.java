package org.bukkit.craftbukkit.inventory.components;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.*;


public final class CraftToolComponent implements ToolComponent {
    private Tool handle;
    public CraftToolComponent(Tool handle) { this.handle = Objects.requireNonNull(handle, "handle"); }
    public CraftToolComponent(ToolComponent source) {
        Objects.requireNonNull(source, "source");
        this.handle = new Tool(source.getRules().stream().map(CraftToolRule::new).map(CraftToolRule::getHandle).toList(), source.getDefaultMiningSpeed(), source.getDamagePerBlock());
    }
    public Tool getHandle() { return handle; }
    @Override public float getDefaultMiningSpeed() { return handle.defaultMiningSpeed(); }
    @Override public void setDefaultMiningSpeed(float speed) { handle = new Tool(handle.rules(), speed, handle.damagePerBlock()); }
    @Override public int getDamagePerBlock() { return handle.damagePerBlock(); }
    @Override public void setDamagePerBlock(int damage) { Preconditions.checkArgument(damage >= 0, "damage must be >= 0"); handle = new Tool(handle.rules(), handle.defaultMiningSpeed(), damage); }
    @Override public List<ToolRule> getRules() { return handle.rules().stream().map(CraftToolRule::new).map(r -> (ToolRule)r).toList(); }
    @Override public void setRules(List<ToolRule> rules) { Objects.requireNonNull(rules,"rules"); handle = new Tool(rules.stream().map(CraftToolRule::new).map(CraftToolRule::getHandle).toList(), handle.defaultMiningSpeed(), handle.damagePerBlock()); }
    @Override public ToolRule addRule(Material block, Float speed, Boolean correctForDrops) {
        Preconditions.checkArgument(block != null && block.isBlock(), "block must be a block type"); Preconditions.checkArgument(speed == null || speed > 0, "speed must be positive");
        return addNative(HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block.getKey().toString())))), speed, correctForDrops);
    }
    @Override public ToolRule addRule(Collection<Material> blocks, Float speed, Boolean correctForDrops) {
        Objects.requireNonNull(blocks,"blocks"); Preconditions.checkArgument(speed == null || speed > 0, "speed must be positive");
        List<Holder<Block>> holders = new ArrayList<>();
        for (Material material : blocks) { Preconditions.checkArgument(material != null && material.isBlock(), "blocks contains non-block type"); holders.add(BuiltInRegistries.BLOCK.wrapAsHolder(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(material.getKey().toString())))); }
        return addNative(HolderSet.direct(holders), speed, correctForDrops);
    }
    @Override public ToolRule addRule(Tag<Material> tag, Float speed, Boolean correctForDrops) {
        Objects.requireNonNull(tag,"tag"); Preconditions.checkArgument(speed == null || speed > 0, "speed must be positive");
        TagKey<Block> key = TagKey.create(Registries.BLOCK, ResourceLocation.parse(tag.getKey().toString()));
        HolderSet<Block> holders = BuiltInRegistries.BLOCK.getTag(key).<HolderSet<Block>>map(set -> set).orElseGet(HolderSet::empty); return addNative(holders, speed, correctForDrops);
    }
    private ToolRule addNative(HolderSet<Block> blocks, Float speed, Boolean correct) { Tool.Rule rule = new Tool.Rule(blocks, Optional.ofNullable(speed), Optional.ofNullable(correct)); List<Tool.Rule> list=new ArrayList<>(handle.rules()); list.add(rule); handle=new Tool(list,handle.defaultMiningSpeed(),handle.damagePerBlock()); return new CraftToolRule(rule); }
    @Override public boolean removeRule(ToolRule rule) { Objects.requireNonNull(rule,"rule"); Tool.Rule target = rule instanceof CraftToolRule c ? c.handle : new CraftToolRule(rule).handle; List<Tool.Rule> list=new ArrayList<>(handle.rules()); boolean changed=list.remove(target); if(changed)handle=new Tool(list,handle.defaultMiningSpeed(),handle.damagePerBlock()); return changed; }
    @Override public Map<String,Object> serialize(){ Map<String,Object> m=new LinkedHashMap<>(); m.put("default-mining-speed",getDefaultMiningSpeed());m.put("damage-per-block",getDamagePerBlock());m.put("rules",getRules());return m; }
    @Override public boolean equals(Object o){return o instanceof CraftToolComponent c&&Objects.equals(handle,c.handle);} @Override public int hashCode(){return Objects.hashCode(handle);}

    public static final class CraftToolRule implements ToolRule {
        private Tool.Rule handle;
        public CraftToolRule(Tool.Rule handle){this.handle=Objects.requireNonNull(handle,"handle");}
        public CraftToolRule(ToolRule source){ Objects.requireNonNull(source,"source"); Collection<Material> blocks=source.getBlocks(); this.handle=new Tool.Rule(toDirect(blocks),Optional.ofNullable(source.getSpeed()),Optional.ofNullable(source.isCorrectForDrops())); }
        Tool.Rule getHandle(){return handle;}
        @Override public Collection<Material> getBlocks(){ List<Material> out=new ArrayList<>(); for(Holder<Block> h:handle.blocks()){ ResourceLocation id=BuiltInRegistries.BLOCK.getKey(h.value()); Material m=Material.matchMaterial(id.toString()); if(m!=null)out.add(m);} return out; }
        @Override public void setBlocks(Material block){Preconditions.checkArgument(block!=null&&block.isBlock(),"block must be a block type");handle=new Tool.Rule(toDirect(List.of(block)),handle.speed(),handle.correctForDrops());}
        @Override public void setBlocks(Collection<Material> blocks){handle=new Tool.Rule(toDirect(blocks),handle.speed(),handle.correctForDrops());}
        @Override public void setBlocks(Tag<Material> tag){Objects.requireNonNull(tag,"tag"); TagKey<Block> key=TagKey.create(Registries.BLOCK,ResourceLocation.parse(tag.getKey().toString())); HolderSet<Block> holders=BuiltInRegistries.BLOCK.getTag(key).<HolderSet<Block>>map(set -> set).orElseGet(HolderSet::empty); handle=new Tool.Rule(holders,handle.speed(),handle.correctForDrops());}
        @Override public Float getSpeed(){return handle.speed().orElse(null);} @Override public void setSpeed(Float speed){Preconditions.checkArgument(speed==null||speed>0,"speed must be positive");handle=new Tool.Rule(handle.blocks(),Optional.ofNullable(speed),handle.correctForDrops());}
        @Override public Boolean isCorrectForDrops(){return handle.correctForDrops().orElse(null);} @Override public void setCorrectForDrops(Boolean correct){handle=new Tool.Rule(handle.blocks(),handle.speed(),Optional.ofNullable(correct));}
        @Override public Map<String,Object> serialize(){Map<String,Object>m=new LinkedHashMap<>();m.put("blocks",getBlocks().stream().map(x->x.getKey().toString()).toList());if(getSpeed()!=null)m.put("speed",getSpeed());if(isCorrectForDrops()!=null)m.put("correct-for-drops",isCorrectForDrops());return m;}
        private static HolderSet<Block> toDirect(Collection<Material> blocks){Objects.requireNonNull(blocks,"blocks");List<Holder<Block>>hs=new ArrayList<>();for(Material m:blocks){Preconditions.checkArgument(m!=null&&m.isBlock(),"blocks contains non-block type");hs.add(BuiltInRegistries.BLOCK.wrapAsHolder(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(m.getKey().toString()))));}return HolderSet.direct(hs);}
    }
}
