package fr.killcoinssmp;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class KillCoinsSMP extends JavaPlugin implements Listener {

    private NamespacedKey coinKey;
    private NamespacedKey balanceKey;

    @Override
    public void onEnable() {
        coinKey = new NamespacedKey(this, "killcoin");
        balanceKey = new NamespacedKey(this, "balance");

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("KillCoinsSMP est activé !");
    }

    private ItemStack createCoin() {
        ItemStack coin = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = coin.getItemMeta();

        meta.setDisplayName(
                ChatColor.LIGHT_PURPLE + "✦ " +
                ChatColor.AQUA + "K" +
                ChatColor.GREEN + "i" +
                ChatColor.YELLOW + "l" +
                ChatColor.GOLD + "l" +
                ChatColor.RED + "C" +
                ChatColor.LIGHT_PURPLE + "o" +
                ChatColor.AQUA + "i" +
                ChatColor.GREEN + "n"
        );

        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.getPersistentDataContainer().set(coinKey, PersistentDataType.BYTE, (byte) 1);
        coin.setItemMeta(meta);
        return coin;
    }

    private boolean isKillCoin(ItemStack item) {
        if (item == null || item.getType() != Material.GOLD_NUGGET || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(coinKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            ItemStack coin = createCoin();

            if (killer.getInventory().firstEmpty() != -1) {
                killer.getInventory().addItem(coin);
                killer.sendMessage(ChatColor.GREEN + "Tu as gagné un KillCoin !");
            } else {
                killer.sendMessage(ChatColor.RED + "Ton inventaire est plein : tu n'as pas reçu le KillCoin.");
            }
        }
    }

    @EventHandler
    public void onDropCoin(PlayerDropItemEvent event) {
        if (isKillCoin(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Tu ne peux pas jeter un KillCoin.");
        }
    }

    @EventHandler
    public void onUseCoin(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        ItemStack item = event.getItem();

        if (!isKillCoin(item)) {
            return;
        }

        event.setCancelled(true);

        int balance = event.getPlayer().getPersistentDataContainer()
                .getOrDefault(balanceKey, PersistentDataType.INTEGER, 0);

        event.getPlayer().getPersistentDataContainer()
                .set(balanceKey, PersistentDataType.INTEGER, balance + 1);

        item.setAmount(item.getAmount() - 1);
        event.getPlayer().sendMessage(ChatColor.GOLD + "+1 KillCoin ! Solde : " + (balance + 1));
    }

    @Override
    public boolean onCommand(
            org.bukkit.command.CommandSender sender,
            org.bukkit.command.Command command,
            String label,
            String[] args
    ) {
        if (command.getName().equalsIgnoreCase("killcoins")) {
            if (sender instanceof Player player) {
                int balance = player.getPersistentDataContainer()
                        .getOrDefault(balanceKey, PersistentDataType.INTEGER, 0);

                player.sendMessage(ChatColor.GOLD + "Ton solde : " + balance + " KillCoins.");
            } else {
                sender.sendMessage("Cette commande est réservée aux joueurs.");
            }
            return true;
        }

        return false;
    }
}
