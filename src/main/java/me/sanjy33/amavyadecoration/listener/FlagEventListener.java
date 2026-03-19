package me.sanjy33.amavyadecoration.listener;

import me.sanjy33.amavyadecoration.flags.AnimatedFlag;
import me.sanjy33.amavyadecoration.flags.FlagSetup;
import me.sanjy33.amavyadecoration.manager.FlagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FlagEventListener implements Listener {

    private final FlagManager manager;

    public FlagEventListener(FlagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerEntityInteract(EntityDamageByEntityEvent event) {
        if (!manager.isEnabled()) return;
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;
        if (!(entity instanceof BlockDisplay)) return;

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(manager.getEntityKey())) return;

        String idString = pdc.get(manager.getEntityKey(), PersistentDataType.STRING);
        if (idString == null) return;
        UUID id = UUID.fromString(idString);

        AnimatedFlag flag = manager.getFlagByID(id);
        if (flag != null) {
            flag.remove();
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!manager.isEnabled()) return;
        if (event.useItemInHand().equals(Event.Result.DENY)) {
            return;
        }
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;
        if (!hand.equals(EquipmentSlot.HAND)) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();
        if (!itemStack.getType().equals(Material.STICK)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!manager.isFlagMaterialAllowed(block.getType())) return;

        FlagSetup flagSetup = manager.getFlagSetup(player);
        if (flagSetup == null) {
            flagSetup = manager.startFlagSetup(player);
        }
        if (flagSetup.start == null) {
            flagSetup.start = block.getLocation();
            player.sendActionBar(Component.text("First corner of flag selected!", NamedTextColor.GREEN));
        } else if (flagSetup.end == null) {
            flagSetup.end = block.getLocation();
            player.sendActionBar(Component.text("Second corner of flag selected!", NamedTextColor.GREEN));
        }

        if (flagSetup.isValid()) {
            try {
                manager.addFlag(new AnimatedFlag(manager, UUID.randomUUID(), flagSetup.start, flagSetup.end));
            } catch (IllegalArgumentException e) {
                player.sendActionBar(Component.text(e.getMessage(), NamedTextColor.RED));
            }
            player.sendActionBar(Component.text("Flag created!", NamedTextColor.GREEN));
            manager.removeFlagSetup(player);
        }

    }

}
