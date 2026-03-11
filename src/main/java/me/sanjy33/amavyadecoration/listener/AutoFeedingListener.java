package me.sanjy33.amavyadecoration.listener;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import me.sanjy33.amavyadecoration.manager.AutoFeedingManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class AutoFeedingListener implements Listener {

    private final AutoFeedingManager manager;

    public AutoFeedingListener(AutoFeedingManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!manager.isEnabled()) return;
        Block block = event.getBlock();
        if (block.getType().equals(Material.HAY_BLOCK)) {
            if (event.isDropItems()) {
                ItemStack drop = manager.getHayBlockDrop(block.getLocation());
                if (drop != null) {
                    event.setDropItems(false);
                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!manager.isEnabled()) return;
        manager.onHayBlockPlace(event.getBlockPlaced());
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (event.isCancelled()) return;
        if (!manager.isEnabled()) return;
        Entity entity = event.getEntity();
        if (!entity.getType().equals(EntityType.COW)) return;
        manager.onCowPathfind((Cow) entity);

    }
}
