package org.mcr;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

import java.util.Random;

public class MrcCache extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("MrcCache has been enabled");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("MrcCache has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("createcache")) {
                Block targetBlock = getTargetBlock(player, 5);  // Получаем блок, на который смотрит игрок, в пределах 5 блоков
                if (targetBlock != null && targetBlock.getType() == Material.CHEST) {
                    createCache((Chest) targetBlock.getState());
                    player.sendMessage("Тайник создан!");
                    return true;
                } else {
                    player.sendMessage("Пожалуйста, смотрите на сундук в пределах 5 блоков.");
                    return true;
                }
            }
        }
        return false;
    }

    private Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) continue;
            break;
        }
        return lastBlock.getType() != Material.AIR ? lastBlock : null;
    }

    private void createCache(Chest chest) {
        Inventory cache = chest.getInventory();
        cache.clear();

        // Генерация случайных предметов
        Random random = new Random();
        for (int i = 0; i < cache.getSize(); i++) {
            if (random.nextBoolean()) {
                Material material = getRandomMaterial(random);
                ItemStack item = new ItemStack(material, random.nextInt(5) + 1);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("Random Item " + (i + 1));
                    item.setItemMeta(meta);
                }
                cache.setItem(i, item);
            }
        }
    }

    private Material getRandomMaterial(Random random) {
        Material[] materials = Material.values();
        Material material = materials[random.nextInt(materials.length)];
        while (!material.isItem() || material == Material.AIR) {
            material = materials[random.nextInt(materials.length)];
        }
        return material;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            Player player = (Player) event.getPlayer();
            Inventory cache = chest.getInventory();

            // Проверка, не пуст ли сундук
            boolean isEmpty = true;
            for (ItemStack item : cache.getContents()) {
                if (item != null) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                return;
            }

            // Выдача случайного предмета игроку
            Random random = new Random();
            ItemStack randomItem = null;
            while (randomItem == null) {
                int slot = random.nextInt(cache.getSize());
                randomItem = cache.getItem(slot);
            }

            if (randomItem != null) {
                player.getInventory().addItem(randomItem);
                player.sendMessage("Вы получили " + randomItem.getAmount() + " " + randomItem.getType().toString());

                // Очистка сундука
                cache.clear();

                // Создание частиц лавы
                Location chestLocation = chest.getLocation();
                chest.getWorld().spawnParticle(Particle.LAVA, chestLocation.add(0.5, 0.5, 0.5), 20);
            }
        }
    }
}
