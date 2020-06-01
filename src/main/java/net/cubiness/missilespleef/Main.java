package net.cubiness.missilespleef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

  private HashMap<Material, Missile> missiles = new HashMap<>();
  private final String WORLD_NAME = "world";

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    try {
      BufferedReader configReader = new BufferedReader(
          new FileReader(new File(getDataFolder() + "/missiles.csv")));
      String line;
      while ((line = configReader.readLine()) != null) {
        List<String> sections = Arrays.asList(line.split(","));
        Material mat = Material.getMaterial(sections.get(0));
        Location min = new Location(Bukkit.getWorld(WORLD_NAME),
            Integer.parseInt(sections.get(1)),
            Integer.parseInt(sections.get(2)),
            Integer.parseInt(sections.get(3)));
        Location max = new Location(Bukkit.getWorld(WORLD_NAME),
            Integer.parseInt(sections.get(4)),
            Integer.parseInt(sections.get(5)),
            Integer.parseInt(sections.get(6)));
        missiles.put(mat, new Missile(min, max));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    getLogger().info("Missiles: " + missiles);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (e.getItem() != null && missiles.containsKey(e.getItem().getType())) {
        missiles.get(e.getItem().getType()).spawn(e.getPlayer().getLocation());
        e.setCancelled(true);
      }
    }
  }

  @Override
  public void onDisable() {
    Bukkit.broadcastMessage("MissileSpleef disabled");
  }

  private class Missile {
    private HashMap<Location, Material> blocks = new HashMap<>();

    Missile(Location min, Location max) {
      int width  = max.getBlockX() - min.getBlockX();
      int height = max.getBlockY() - min.getBlockY();
      int depth  = max.getBlockZ() - min.getBlockZ();
      for (int z = 0; z <= depth; z++) {
        for (int y = 0; y <= height; y++) {
          for (int x = 0; x <= width; x++) {
            Location loc = new Location(Bukkit.getWorld("world"),
                x - width / 2,
                y - height / 2,
                z - depth / 2);
            Location worldLoc = new Location(Bukkit.getWorld(WORLD_NAME),
                x + min.getBlockX(),
                y + min.getBlockY(),
                z + min.getBlockZ());
            blocks.put(loc, worldLoc.getBlock().getType());
          }
        }
      }
    }

    void spawn(Location center) {
      for (Location l : blocks.keySet()) {
        Location tmp = center.clone().add(l);
        tmp.getBlock().setType(blocks.get(l));
      }
    }

    @Override
    public String toString() {
      return "Missile{" +
          "blocks=" + blocks +
          '}';
    }
  }
}
