package me.jakedadream.snwplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;


public class entityedits implements Listener {



        public void run() {
            Bukkit.getWorlds();
            for (World w : Bukkit.getWorlds()) {

                for (Entity entity : w.getEntities()) {

//                =======================
//                Coin Spin
//                =======================
                    ArmorStand as = (ArmorStand) entity;
                    if (entity == as) {
                        if (as.getName().equalsIgnoreCase("coin")) {
                            Location loc = as.getLocation();
                            loc.setYaw(as.getLocation().getYaw() + 6.0F);
                            as.teleport(loc);
                        }
                    }
                }
            }
        }
}

