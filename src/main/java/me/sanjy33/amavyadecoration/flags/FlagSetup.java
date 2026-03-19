package me.sanjy33.amavyadecoration.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlagSetup {

    public Player player;
    public Location start;
    public Location end;

    public FlagSetup(Player player) {
        this.player = player;
    }

    public boolean isValid() {
        return (player != null) && (start != null) && (end != null);
    }
}
