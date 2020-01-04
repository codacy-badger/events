package com.rabbit13.events.managers;

import com.rabbit13.events.main.Main;
import com.rabbit13.events.objects.eData;
import com.rabbit13.events.objects.eEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.rabbit13.events.main.Misc.debugMessage;

public class PlayerManager {
    private static final Map<Player, eData> joinedEvent = new HashMap<>();
    private static final Map<Player, Location> checkpointed = new HashMap<>();
    private static final Map<Player, eEvent> modifyingEvent = new HashMap<>();
    private static final Map<String, Integer> winCounter = new HashMap<>();

    /**
     * Called when player is about to enter event
     *
     * @param player player entering event
     * @return data of player (items, potions, and location)
     */
    public static eData playerEnteringEvent(Player player) {
        debugMessage("all items in inventory: " + player.getInventory().getContents().length);
        eData data = new eData(player.getInventory().getContents()
                , player.getActivePotionEffects()
                , player.getLocation());
        // TODO: 04.01.2020 test: only clear()
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        //health set to maximum
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        debugMessage("Max Health for player " + player.getName() + ": " + maxHealth);
        player.setHealth(maxHealth);
        BackupItemsManager.createBackup(player, data);
        return data;
    }

    /**
     * Retrieve items to player that had before joining and teleport him to his location before joining
     *
     * @param player player leaving event
     */
    public static void playerLeavingEvent(Player player) {
        debugMessage("Leaving player: " + player);
        eData data = joinedEvent.get(player);
        PlayerInventory inventory = player.getInventory();
        player.teleport(data.getLocation());
        inventory.setContents(data.getItems());
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            player.addPotionEffects(data.getEffects());
            player.setFireTicks(0);
        });

        joinedEvent.remove(player);
    }

    /**
     * @return players that joined active event
     */
    public static Map<Player, eData> getJoinedEvent() {
        return joinedEvent;
    }

    /**
     * @return player that have saved checkpojnt
     */
    public static Map<Player, Location> getCheckpointed() {
        return checkpointed;
    }

    /**
     * @return admins that editing event rn
     */
    public static Map<Player, eEvent> getModifyingEvent() {
        return modifyingEvent;
    }

    public static Map<String, Integer> getWinCounter() {
        return winCounter;
    }
}