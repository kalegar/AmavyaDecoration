package me.sanjy33.amavyadecoration.flags;

import me.sanjy33.amavyadecoration.flags.windfunction.SinSinWindFunction;
import me.sanjy33.amavyadecoration.flags.windfunction.WindFunction;
import me.sanjy33.amavyadecoration.manager.FlagManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class AnimatedFlag {

    private final FlagManager manager;
    private final UUID id;
    private final String worldName;
    private final Vector positionStart;
    private final Vector positionEnd;
    private final Vector flagpoleOffset = new Vector();
    private Material[][] materials;
    private BlockDisplay[][] entities;
    private float frameIndex = 0;
    private WindFunction windFunction = SinSinWindFunction.getInstance();
    private boolean valid = true;

    public AnimatedFlag(FlagManager manager, UUID id, Location start, Location end) {
        if (!start.getWorld().getName().equals(end.getWorld().getName())) {
            throw new IllegalArgumentException("Start and end must be in the same world.");
        }
        this.manager = manager;
        this.id = id;
        worldName = start.getWorld().getName();
        positionStart = start.toVector();
        positionEnd = end.toVector();

        validatePositions();

        constructMaterialArray();
    }

    public boolean isValid() {
        return valid;
    }

    public void breakNaturally() {
        forEachCell(null, (block, x, y, w, h, inXDirection) -> {
            Material material = materials[x][y];
            if (block.getType().isAir()) {
                block.setType(material);
                block.breakNaturally();
            }
        });
        remove();
    }

    public void remove() {
        cleanup();
        valid = false;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public boolean isChunkLoaded() {
        World world = getWorld();
        if (world == null) return false;
        return world.isChunkLoaded(positionStart.getBlockX() >> 4, positionStart.getBlockZ() >> 4) && world.isChunkLoaded(positionEnd.getBlockX() >> 4, positionEnd.getBlockZ() >> 4);
    }

    public void cleanup() {
        if (this.entities == null) return;
        for (int x = 0; x < entities.length; x++) {
            for (int y = 0; y < entities[x].length; y++) {
                if (entities[x][y] == null || !entities[x][y].isValid()) {
                    continue;
                }
                entities[x][y].remove();
                entities[x][y] = null;
            }
        }
    }

    public void animate(float delta) {
        frameIndex += delta;
        while (frameIndex > 3.14f*8) {
            frameIndex -= 3.14f*8;
        }

        forEachCell((w, h, inXDirection) -> {
            if (this.entities == null) {
                this.entities = new BlockDisplay[w][h];
            }
        }, (block, x, w, h, inXDirection, lastOffset) -> {
            float xOffset = windFunction.getValue(frameIndex + x);
//            xOffset = Math.clamp(xOffset, lastOffset-0.5f, lastOffset+0.8f);
            for (int y = 0; y < h; y++) {
                Block colBlock = block.getRelative(BlockFace.UP, y);
                Material material = materials[x][y];
                if (!material.isSolid()) continue;
                BlockDisplay entity = this.entities[x][y];
                if (entity == null || !entity.isValid()) {
                    // Create new entity
                    entity = colBlock.getWorld().spawn(colBlock.getLocation(), BlockDisplay.class, e -> {
                        e.setBlock(material.createBlockData());
                        e.setPersistent(false);
                        e.getPersistentDataContainer().set(manager.getEntityKey(), PersistentDataType.STRING, id.toString());
                    });
                }
                entity.setTransformation(
                        new Transformation(
                                new Vector3f(!inXDirection ? xOffset : (float) flagpoleOffset.getX(), 0, inXDirection ? xOffset : (float) flagpoleOffset.getZ()), // Translation
                                new AxisAngle4f(), // left rotation
                                new Vector3f(1,1,1), // scale
                                new AxisAngle4f() // right rotation
                        )
                );
                this.entities[x][y] = entity;
            }
            return xOffset;
        }, null);

//        int xDif = Math.abs(positionStart.getBlockX() - positionEnd.getBlockX());
//        int zDif = Math.abs(positionStart.getBlockZ() - positionEnd.getBlockZ());
//        int height = positionEnd.getBlockY() - positionStart.getBlockY()+1;
//
//        int xMin, yMin, xMax, yMax;
//        yMin = positionStart.getBlockY();
//        yMax = positionEnd.getBlockY()+1;
//        boolean inXDirection;
//        if (xDif > 1 && zDif > 1) {
//            throw new IllegalArgumentException("Flag cannot extend in both x and z directions.");
//        } else if (xDif > 1) {
//            xMin = Math.min(positionStart.getBlockX(), positionEnd.getBlockX());
//            xMax = Math.max(positionStart.getBlockX(), positionEnd.getBlockX())+1;
//            inXDirection = true;
//        } else {
//            xMin = Math.min(positionStart.getBlockZ(), positionEnd.getBlockZ());
//            xMax = Math.max(positionStart.getBlockZ(), positionEnd.getBlockZ())+1;
//            inXDirection = false;
//        }
//
//        World world = Bukkit.getWorld(this.worldName);
//        if (world == null) {
//            throw new IllegalArgumentException("World is null");
//        }
//        if (this.entities == null) {
//            this.entities = new BlockDisplay[xMax - xMin][height];
//        }
//        for (int x = xMin; x < xMax; x++)  {
//            for (int y = yMin; y < yMax; y++) {
//                int _x = x - xMin;
//                int _y = y - yMin;
//                Location location;
//                if (inXDirection) {
//                    location = new Location(world, x, y, positionStart.getBlockZ());
//                } else {
//                    location = new Location(world, positionStart.getBlockX(), y, x);
//                }
//                Material material = materials[_x][_y];
//                if (!material.isSolid()) continue;
//                BlockDisplay entity = this.entities[_x][_y];
//                if (entity == null || !entity.isValid()) {
//                    // Create new entity
//                    entity = world.spawn(location, BlockDisplay.class, e -> {
//                        e.setBlock(material.createBlockData());
//                        e.setPersistent(false);
//                        e.getPersistentDataContainer().set(manager.getEntityKey(), PersistentDataType.STRING, id.toString());
//                    });
//                }
//                float xOffset = windFunction.getValue(frameIndex + x);
//                entity.setTransformation(
//                        new Transformation(
//                                new Vector3f(!inXDirection ? xOffset : (float) flagpoleOffset.getX(), 0, inXDirection ? xOffset : (float) flagpoleOffset.getZ()), // Translation
//                                new AxisAngle4f(), // left rotation
//                                new Vector3f(1,1,1), // scale
//                                new AxisAngle4f() // right rotation
//                        )
//                );
//                this.entities[_x][_y] = entity;
//
//            }
//        }
    }

    private void validatePositions() {
        // Ensure start position is minimum, end position is maximum
        Vector tempStart = positionStart.clone();
        Vector tempEnd = positionEnd.clone();

        positionStart.setX(Math.min(tempStart.getBlockX(), tempEnd.getBlockX()));
        positionStart.setY(Math.min(tempStart.getBlockY(), tempEnd.getBlockY()));
        positionStart.setZ(Math.min(tempStart.getBlockZ(), tempEnd.getBlockZ()));
        positionEnd.setX(Math.max(tempStart.getBlockX(), tempEnd.getBlockX()));
        positionEnd.setY(Math.max(tempStart.getBlockY(), tempEnd.getBlockY()));
        positionEnd.setZ(Math.max(tempStart.getBlockZ(), tempEnd.getBlockZ()));
    }

    public boolean detectFlagPole() {
        flagpoleOffset.zero();
        int xDif = Math.abs(positionStart.getBlockX() - positionEnd.getBlockX());
        int zDif = Math.abs(positionStart.getBlockZ() - positionEnd.getBlockZ());
        boolean inXDirection;
        if (xDif > 1 && zDif > 1) {
            throw new IllegalArgumentException("Flag cannot extend in both x and z directions.");
        } else inXDirection = xDif > 1;

        Block blockStart, blockEnd;
        if (inXDirection) {
            blockStart = getWorld().getBlockAt(positionStart.getBlockX()-1, positionStart.getBlockY(), positionStart.getBlockZ());
            blockEnd = getWorld().getBlockAt(positionEnd.getBlockX()+1, positionEnd.getBlockY(), positionEnd.getBlockZ());
        } else {
            blockStart = getWorld().getBlockAt(positionStart.getBlockX(), positionStart.getBlockY(), positionStart.getBlockZ()-1);
            blockEnd = getWorld().getBlockAt(positionEnd.getBlockX(), positionEnd.getBlockY(), positionEnd.getBlockZ()+1);
        }

        if (manager.isPoleMaterialAllowed(blockStart.getType())) {
            if (inXDirection) {
                flagpoleOffset.setX(-0.5f);
            }else{
                flagpoleOffset.setZ(-0.5f);
            }
            return true;
        } else if (manager.isPoleMaterialAllowed(blockEnd.getType())) {
            if (inXDirection) {
                flagpoleOffset.setX(0.5f);
            }else{
                flagpoleOffset.setZ(0.5f);
            }
            return true;
        } else {
            return false;
        }
    }

    private interface CellStartCallback {
        void start(int width, int height, boolean inXDirection);
    }

    private interface CellCallback {
        void callback(Block block, int x, int y, int width, int height, boolean inXDirection);
    }

    private interface CellColumnCallback {
        float column(Block block, int x, int width, int height, boolean inXDirection, float lastValue);
    }

    private void forEachCell(CellStartCallback start, CellCallback callback) {
        forEachCell(start, null, callback);
    }

    private void forEachCell(CellStartCallback start, CellColumnCallback column, CellCallback callback) {
        int xDif = Math.abs(positionStart.getBlockX() - positionEnd.getBlockX());
        int zDif = Math.abs(positionStart.getBlockZ() - positionEnd.getBlockZ());
        int height = positionEnd.getBlockY() - positionStart.getBlockY()+1;

        int xMin, yMin, xMax, yMax;
        yMin = positionStart.getBlockY();
        yMax = positionEnd.getBlockY()+1;
        boolean inXDirection;
        if (xDif > 1 && zDif > 1) {
            throw new IllegalArgumentException("Flag cannot extend in both x and z directions.");
        } else if (xDif > 1) {
            xMin = Math.min(positionStart.getBlockX(), positionEnd.getBlockX());
            xMax = Math.max(positionStart.getBlockX(), positionEnd.getBlockX()) + 1;
            inXDirection = true;
        } else {
            xMin = Math.min(positionStart.getBlockZ(), positionEnd.getBlockZ());
            xMax = Math.max(positionStart.getBlockZ(), positionEnd.getBlockZ()) + 1;
            inXDirection = false;
        }
        int width = xMax-xMin;
        if (start != null) {
            start.start(width, height, inXDirection);
        }

        if (callback == null && column == null) {
            return;
        }

        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            throw new IllegalArgumentException("World is null");
        }
        float lastValue = 0;
        for (int x = xMin; x < xMax; x++)  {
            for (int y = yMin; y < yMax; y++) {
                int _x = x - xMin;
                int _y = y - yMin;
                Block block;
                if (inXDirection) {
                    block = world.getBlockAt(x, y, positionStart.getBlockZ());
                } else {
                    block = world.getBlockAt(positionStart.getBlockX(), y, x);
                }
                if (y == yMin && column != null) {
                    lastValue = column.column(block, _x, width, height, inXDirection, lastValue);
                }
                if (callback != null) {
                    callback.callback(block, _x, _y, width, height, inXDirection);
                }
            }
        }
    }

    private void constructMaterialArray() {
        forEachCell((w, h, inXDirection) -> {
            this.materials = new Material[w][h];
        }, (block, x, y, w, h, inXDirection) -> {
            this.materials[x][y] = block.getType();
            block.setType(Material.AIR);
        });
    }

    public UUID getId() {
        return id;
    }

    public AnimatedFlag(FlagManager manager, UUID id, ConfigurationSection section) {
        this.manager = manager;
        this.id = id;
        this.worldName = section.getString("worldName");
        this.positionStart = section.getVector("positionStart");
        this.positionEnd = section.getVector("positionEnd");
        validatePositions();

        forEachCell((w, h, inXDirection) -> {
            this.materials = new Material[w][h];
        }, (block, x, y, width, height, inXDirection) -> {
            this.materials[x][y] = Material.valueOf(section.getString("materials."+x+"."+y, Material.WHITE_WOOL.name()));
        });

//        int width = Math.max(Math.abs(positionStart.getBlockX() - positionEnd.getBlockX()), Math.abs(positionStart.getBlockZ() - positionEnd.getBlockZ()))+1;
//        int height = positionEnd.getBlockY() - positionStart.getBlockY()+1;
//        this.materials = new Material[width][height];
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                this.materials[x][y] = Material.valueOf(section.getString("materials."+x+"."+y, Material.WHITE_WOOL.name()));
//            }
//        }
    }

    public void toConfig(ConfigurationSection section) {
        section.set("worldName", worldName);
        section.set("positionStart", positionStart);
        section.set("positionEnd", positionEnd);
        for (int x = 0; x < materials.length; x++) {
            for (int y = 0; y < materials[x].length; y++) {
                section.set("materials."+x+"."+y, materials[x][y].name());
            }
        }
    }

}
