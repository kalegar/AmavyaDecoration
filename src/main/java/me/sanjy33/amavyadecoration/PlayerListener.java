package me.sanjy33.amavyadecoration;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class PlayerListener implements Listener {

    private final AmavyaDecoration plugin;
//    private final Set<Material> shelfMaterials = new HashSet<>(Set.of(
//            Material.ACACIA_SLAB,
//            Material.ANDESITE_SLAB,
//            Material.BAMBOO_MOSAIC_SLAB,
//            Material.BAMBOO_SLAB,
//            Material.BIRCH_SLAB,
//            Material.BLACKSTONE_SLAB,
//            Material.BRICK_SLAB,
//            Material.CHERRY_SLAB,
//            Material.COBBLED_DEEPSLATE_SLAB,
//            Material.COBBLESTONE_SLAB,
//            Material.CRIMSON_SLAB,
//            Material.CUT_COPPER_SLAB,
//            Material.CUT_RED_SANDSTONE_SLAB,
//            Material.CUT_SANDSTONE_SLAB,
//            Material.DARK_OAK_SLAB,
//            Material.DARK_PRISMARINE_SLAB,
//            Material.DEEPSLATE_TILE_SLAB,
//            Material.DIORITE_SLAB,
//            Material.END_STONE_BRICK_SLAB,
//            Material.EXPOSED_CUT_COPPER_SLAB,
//            Material.GRANITE_SLAB,
//            Material.JUNGLE_SLAB,
//            Material.MANGROVE_SLAB,
//            Material.MOSSY_COBBLESTONE_SLAB,
//            Material.MOSSY_STONE_BRICK_SLAB,
//            Material.MUD_BRICK_SLAB,
//            Material.NETHER_BRICK_SLAB,
//            Material.OAK_SLAB,
//            Material.OXIDIZED_CUT_COPPER_SLAB,
//            Material.PALE_OAK_SLAB,
//            Material.PETRIFIED_OAK_SLAB,
//            Material.POLISHED_ANDESITE_SLAB,
//            Material.POLISHED_BLACKSTONE_SLAB,
//            Material.POLISHED_GRANITE_SLAB,
//            Material.POLISHED_DIORITE_SLAB,
//            Material.POLISHED_TUFF_SLAB,
//            Material.POLISHED_DEEPSLATE_SLAB,
//            Material.POLISHED_BLACKSTONE_BRICK_SLAB,
//            Material.SPRUCE_SLAB,
//            Material.WARPED_SLAB,
//            Material.PURPUR_SLAB,
//            Material.QUARTZ_SLAB
//    ));

    public PlayerListener(AmavyaDecoration plugin) {
        this.plugin = plugin;
    }

    public boolean isSlab(Block block) {
        BlockData blockData = block.getBlockData();
        return (blockData instanceof Slab);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.shelfManager.refreshNearbyDisplays(event.getPlayer().getLocation()), 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        Block block = event.getBlock();
        if (!isSlab(block)) return;

        Location blockLocation = block.getLocation().toCenterLocation();
        plugin.shelfManager.deleteShelfAtLocation(blockLocation, true);
    }

    @EventHandler
    public void onBLockDestroy(BlockDestroyEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        Block block = event.getBlock();
        if (!isSlab(block)) return;

        Location blockLocation = block.getLocation().toCenterLocation();
        plugin.shelfManager.deleteShelfAtLocation(blockLocation, true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        if (!isSlab(event.getExplodedBlockState().getBlock())) return;

        Location blockLocation = event.getBlock().getLocation().toCenterLocation();
        plugin.shelfManager.deleteShelfAtLocation(blockLocation, true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        for (Block block : event.blockList()) {
            if (isSlab(block)) {
                plugin.shelfManager.deleteShelfAtLocation(block.getLocation(), true);
            }
        }
    }

    @EventHandler
    public void onChunkLoaded(ChunkLoadEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        if (event.isNewChunk()) return; // Shelves are never in new chunks
        plugin.shelfManager.refreshDisplaysInChunk(event.getChunk());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.shelfManager.isEnabled()) return;
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;
        if (!hand.equals(EquipmentSlot.HAND)) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!isSlab(block)) return;

        Slab slab = (Slab) block.getBlockData();
        if (slab.getType().equals(Slab.Type.DOUBLE)) return;

        Location blockLocation = block.getLocation().toCenterLocation();
        Shelf shelf = plugin.shelfManager.getShelfAtLocation(blockLocation);

        if (shelf == null) {
            if (itemStack.getType().equals(Material.STICK)) {
                if (plugin.shelfManager.isCreatingShelf(player.getUniqueId(), blockLocation)) {
                    plugin.shelfManager.createShelfAtLocation(blockLocation);
                    player.sendActionBar(Component.text("Shelf created!",NamedTextColor.LIGHT_PURPLE));
                } else {
                    plugin.shelfManager.startCreatingShelf(player.getUniqueId(), blockLocation);
                    player.sendActionBar(Component.text("Right click the slab again to make a shelf!", NamedTextColor.DARK_PURPLE));
                }
            }
            return;
        }

        Location interactionPoint = event.getInteractionPoint();
        if (interactionPoint == null) return;

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        Location subLoc = blockLocation.clone().subtract(interactionPoint);
        int slotIndex;
        if (subLoc.getX() > 0 && subLoc.getZ() > 0) {
            slotIndex = 0;
        } else if (subLoc.getX() < 0 && subLoc.getZ() > 0) {
            slotIndex = 1;
        } else if (subLoc.getX() < 0 && subLoc.getZ() < 0) {
            slotIndex = 3;
        } else {
            slotIndex = 2;
        }

        if (!player.isSneaking()) {
            if (shelf.isSlotEmpty(slotIndex) && (!playerInventory.getItemInMainHand().isEmpty())) {
                shelf.setSlotMaterial(slotIndex, itemStack);
                if (itemStack.getAmount() == 1) {
                    playerInventory.setItemInMainHand(null);
                }else{
                    itemStack.setAmount(itemStack.getAmount()-1);
                }
            }else{
                shelf.rotateSlot(slotIndex, 45);
            }
        }else{
            Shelf.ShelfSlot originalSlot = shelf.setSlot(slotIndex, null);
            if (originalSlot != null)
                player.getWorld().dropItem(blockLocation, new ItemStack(originalSlot.itemStack()));
        }

        shelf.refreshSlot(slotIndex);
        plugin.saveAsync();
    }
}
