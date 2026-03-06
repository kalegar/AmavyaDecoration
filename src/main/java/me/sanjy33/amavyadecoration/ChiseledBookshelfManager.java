package me.sanjy33.amavyadecoration;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ChiseledBookshelfInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class ChiseledBookshelfManager {

    private final AmavyaDecoration plugin;
    private final Map<String, List<TextDisplay>> textDisplays = new HashMap<>();
    private BukkitTask bookshelfTask = null;
    private int bookshelfSearchRadius = 3;

    public ChiseledBookshelfManager(AmavyaDecoration plugin) {
        this.plugin = plugin;
    }

    public void stopBookshelfTask() {
        if (bookshelfTask != null) {
            bookshelfTask.cancel();
            bookshelfTask = null;
            plugin.getLogger().info("Stopped bookshelf TextDisplay task.");
        }
    }

    public void startBookshelfTask() {
        stopBookshelfTask();
        bookshelfTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<ChiseledBookshelf> bookshelves = new ArrayList<>();
            Set<String> keys = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                getBookshelvesNearLocation(player.getLocation(), bookshelfSearchRadius, bookshelves);
            }
            for (ChiseledBookshelf bookshelf : bookshelves) {
                String key = locationToStringKey(bookshelf.getLocation());
                keys.add(key);
                List<TextDisplay> displays;
                if (textDisplays.containsKey(key)) {
                    displays = textDisplays.get(key);
                }else{
                    displays = new ArrayList<>(6);
                }
                createTextDisplaysForBookshelf(bookshelf, displays);
                textDisplays.put(key, displays);

            }
            // Remove displays that aren't in range of players anymore
            for (Iterator<String> it = textDisplays.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (!keys.contains(key)) {
                    List<TextDisplay> list = textDisplays.get(key);
                    if (list != null) {
                        for (TextDisplay display : list) {
                            if (display != null && display.isValid()) {
                                display.remove();
                            }
                        }
                    }
                    it.remove();
                }

            }
        }, 20, 20);
        plugin.getLogger().info("Started bookshelf TextDisplay task.");
    }

    public void getBookshelvesNearLocation(Location location, int radius, List<ChiseledBookshelf> out) {
        for (int z = location.getBlockZ() - radius; z < location.getBlockZ()+radius+1; z++) {
            for (int y = location.getBlockY() - radius; y < location.getBlockY()+radius+1; y++) {
                for (int x = location.getBlockX() - radius; x < location.getBlockX()+radius+1; x++) {
                    Block block = location.getWorld().getBlockAt(x,y,z);
                    if (block.getState() instanceof ChiseledBookshelf) {
                        out.add((ChiseledBookshelf) block.getState());
                    }
                }
            }
        }
    }

    public void createTextDisplaysForBookshelf(ChiseledBookshelf bookshelf, List<TextDisplay> out) {


        org.bukkit.block.data.type.ChiseledBookshelf data = (org.bukkit.block.data.type.ChiseledBookshelf) bookshelf.getBlockData();
        BlockFace facing = data.getFacing();

        ChiseledBookshelfInventory inventory = bookshelf.getInventory();

        for (int i = 0; i < data.getMaximumOccupiedSlots(); i++) {

            TextDisplay textDisplay;
            if (i < out.size()) {
                textDisplay = out.get(i);
            }else{
                out.add(null); // Increase list size w/ empty slot for now.
                textDisplay = null;
            }

            if (data.isSlotOccupied(i)) {

                ItemStack item = inventory.getContents()[i];
                if (item == null) continue;
                Component displayComponent = item.effectiveName();

                if (item.getItemMeta() instanceof EnchantmentStorageMeta enchMeta) {

                    Map<Enchantment, Integer> enchantments = enchMeta.getStoredEnchants();


                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        displayComponent = displayComponent.appendNewline();
                        displayComponent = displayComponent.append(entry.getKey().displayName(entry.getValue()));
                    }
                }

                final Component finalComponent = displayComponent;

                Location spawnLocation = bookshelf.getLocation().toCenterLocation().add(facing.getDirection().multiply(0.6f));
                Vector offset = new Vector();

                int x = (i % 3) - 1;
                int y = i / 3;

                offset.setY((float) (1 - y) - 0.5);
                if (facing.equals(BlockFace.NORTH)) { // -z
                    offset.setX(-x);
                } else if (facing.equals(BlockFace.EAST)) { // +x
                    offset.setZ(-x);
                } else if (facing.equals(BlockFace.SOUTH)) {
                    offset.setX(x);
                } else if (facing.equals(BlockFace.WEST)) {
                    offset.setZ(x);
                }
                offset.multiply(0.33f);

                spawnLocation.add(offset);
                if (textDisplay != null && textDisplay.isValid()) {
                    // Update existing display
                    textDisplay.text(finalComponent);
                } else {
                    // Create new display
                    textDisplay = bookshelf.getWorld().spawn(spawnLocation, TextDisplay.class, entity -> {
                        entity.text(finalComponent);
                        entity.setBillboard(Display.Billboard.VERTICAL);
                        entity.setPersistent(false);
                        entity.setTransformation(new Transformation(
                                new Vector3f(),
                                new AxisAngle4f(),
                                new Vector3f(0.1f, 0.1f, 0.1f),
                                new AxisAngle4f()
                        ));
                    });
                }
                out.set(i, textDisplay);
            } else {
                textDisplay = out.get(i);
                if (textDisplay != null && textDisplay.isValid()) {
                    textDisplay.remove();
                }
                out.set(i, null);
            }
        }

    }

    public String locationToStringKey(Location location) {
        return location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }

    public Location stringKeyToLocation(String key) {
        String[] parts = key.split("\\|");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }

    public void cleanup() {
        stopBookshelfTask();
        for (List<TextDisplay> list : textDisplays.values()) {
            if (list == null) continue;
            for (TextDisplay display : list) {
                if (display != null && display.isValid()) {
                    display.remove();
                }
            }
        }
        textDisplays.clear();
    }

}
