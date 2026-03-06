package me.sanjy33.amavyadecoration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class Shelf {

    public static final int MAX_SLOTS = 4;
    private final Location location;
    private final ShelfSlot[] shelfSlots = new ShelfSlot[MAX_SLOTS];
    private final ItemDisplay[] itemDisplays = new ItemDisplay[MAX_SLOTS];

    public Shelf(Location location) {
        this.location = location.clone().toCenterLocation();
        Arrays.fill(this.shelfSlots, null);
        Arrays.fill(this.itemDisplays, null);
    }

    public boolean isSlotEmpty(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return true;
        return (shelfSlots[slotIndex] == null);
    }

    public ShelfSlot setSlot(int slotIndex, ShelfSlot slot) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return null;
        ShelfSlot currentSlot = shelfSlots[slotIndex];
        shelfSlots[slotIndex] = slot;
        return currentSlot;
    }

    public boolean isUpperHalf() {
        Block block = this.location.getWorld().getBlockAt(location);
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Slab slab)) return false;
        return (slab.getType().equals(Slab.Type.TOP));
    }

    public ShelfSlot setSlotMaterial(int slotIndex, ItemStack itemStack) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return null;
        ShelfSlot currentSlot = shelfSlots[slotIndex];
//        if (currentSlot == null)
//            System.out.println("Current Slot is null");
//        else {
//            System.out.println("Current Slot is " + currentSlot.getItemStack().getType());
//        }
        shelfSlots[slotIndex] = new ShelfSlot(itemStack, 0);
        return currentSlot;
    }

    public ShelfSlot getSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return null;
        return shelfSlots[slotIndex];
    }

    public void rotateSlot(int slotIndex, double angle) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return;
        if (shelfSlots[slotIndex] == null) return;
        shelfSlots[slotIndex] = shelfSlots[slotIndex].addOrientation(angle);
    }

    public Location getLocation() {
        return location;
    }

    private boolean isChunkLoaded() {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public void refreshSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return;
        if (!isChunkLoaded()) return;
        final boolean isUpperHalf = isUpperHalf();

        ItemDisplay itemDisplay = itemDisplays[slotIndex];
        if ((itemDisplay != null) && (itemDisplay.isValid())) {
            itemDisplay.remove();
        }

        ShelfSlot slot = shelfSlots[slotIndex];
        if (slot == null) return;
        itemDisplay = location.getWorld().spawn(location, ItemDisplay.class, entity -> {
            float x = (float) (slotIndex % 2) - 0.5f;
            float y = 0.25f;
            float z = (float) (slotIndex / 2) - 0.5f;
            x *= 0.5f;
            z *= 0.5f;
            if (isUpperHalf) {
                y += 0.5f;
            }
            entity.setTransformation(new Transformation(
                    new Vector3f(x,y,z),
                    new AxisAngle4f(),
                    new Vector3f(0.5f,0.5f,0.5f),
                    new AxisAngle4f((float) Math.toRadians(slot.orientation), 0, 1, 0)
            ));
            entity.setItemStack(slot.itemStack);
            entity.setPersistent(false);
        });
        itemDisplays[slotIndex] = itemDisplay;

    }

    public void refreshDisplay() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            refreshSlot(i);
        }
    }

    public void cleanupDisplays() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemDisplay itemDisplay = itemDisplays[i];
            if ((itemDisplay != null) && (itemDisplay.isValid())) {
                itemDisplay.remove();
            }
        }
    }

    public void save(ConfigurationSection section) {
        section.set("location",location);
        ConfigurationSection slotSection = section.createSection("slots");
        for (int i = 0; i < MAX_SLOTS; i++) {
            ShelfSlot slot = shelfSlots[i];
            if (slot == null) continue;;
            ConfigurationSection subSection = slotSection.createSection(Integer.toString(i));
            shelfSlots[i].save(subSection);
        }
    }

    public static Shelf load(ConfigurationSection section) {
        Location location = section.getLocation("location");
        Shelf shelf = new Shelf(location);
        ConfigurationSection slotSection = section.getConfigurationSection("slots");
        if (slotSection != null) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                ConfigurationSection subSection = slotSection.getConfigurationSection(Integer.toString(i));
                if (subSection == null) continue;
                ShelfSlot slot = ShelfSlot.load(subSection);
                shelf.setSlot(i, slot);
            }
        }
        return shelf;
    }

    public static class ShelfSlot {
        private final ItemStack itemStack;
        private final double orientation;

        public ShelfSlot(ItemStack itemStack, double orientation) {
            this.itemStack = itemStack.clone();
            this.itemStack.setAmount(1);
            this.orientation = orientation;

        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public double getOrientation() {
            return orientation;
        }

        public ShelfSlot setOrientation(double orientation) {
            return new ShelfSlot(itemStack, orientation);
        }

        public ShelfSlot addOrientation(double angle) {
            double newOrientation = (this.orientation + angle) % 360;
//            System.out.println("New angle: " + newOrientation);
            return new ShelfSlot(itemStack, newOrientation);
        }

        public void save(ConfigurationSection section) {
            section.set("itemstack",itemStack);
            section.set("orientation",orientation);
        }

        public static ShelfSlot load(ConfigurationSection section) {
            Object o_itemStack = section.get("itemstack");
            if (o_itemStack == null) return null;
            double orient = section.getDouble("orientation");
            return new ShelfSlot((ItemStack) o_itemStack, orient);
        }
    }

}
