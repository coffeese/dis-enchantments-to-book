package dev.coffeese.disenchantmentstobook;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 * @author coffeese
 */
public class AnvilListener implements Listener {

    private final DisEnchantmentsToBookPlugin plugin;

    public AnvilListener(final DisEnchantmentsToBookPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) {
            return;
        }

        Player p = (Player) event.getView().getPlayer();
        AnvilInventory inventory = event.getInventory();
        ItemStack base = inventory.getItem(0);
        ItemStack addition = inventory.getItem(1);

        if (base == null || addition == null) {
            return;
        }

        if (base.getType() != Material.BOOK) {
            return;
        }

        if (addition.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        if (base.getAmount() > 1 || addition.getAmount() > 1) {
            return;
        }

        int cost = 0;
        ItemMeta additionMeta = addition.getItemMeta();
        if (!(additionMeta instanceof Repairable)) {
            return;
        }

        Repairable additionRepairable = (Repairable) additionMeta;
        if (additionRepairable.hasRepairCost()) {
            cost += additionRepairable.getRepairCost();
        }

        Map<Enchantment, Integer> additionEnchants = additionMeta.getEnchants();
        if (additionEnchants.size() == 0) {
            return;
        }

        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta resultMeta = result.getItemMeta();
        if (!(resultMeta instanceof Repairable)) {
            return;
        }

        EnchantmentStorageMeta enchantedMeta = (EnchantmentStorageMeta)resultMeta;
        for (Entry<Enchantment, Integer> entry : additionEnchants.entrySet()) {
            final Enchantment e = entry.getKey();

            int level = entry.getValue();
            if (level > e.getMaxLevel()) {
                level = e.getMaxLevel();
            }
            cost += this.getEnchantmentMultiplier(e) * level;

            if (isCopiableEnchant(e))
                enchantedMeta.addStoredEnchant(e, level, false);
        }

        if (!enchantedMeta.hasStoredEnchants()) {
            return;
        }

        Repairable resultRepairable = (Repairable)resultMeta;
        resultRepairable.setRepairCost(additionRepairable.hasRepairCost() ? additionRepairable.getRepairCost() * 2 + 1 : 1);
        result.setItemMeta(resultMeta);

        event.setResult(result);
        final int repairCost = cost;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            inventory.setRepairCost(repairCost);
            p.setWindowProperty(InventoryView.Property.REPAIR_COST, repairCost);
        });
    }

    private int getEnchantmentMultiplier(final Enchantment enchantment) {
        if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL)
                || enchantment.equals(Enchantment.DAMAGE_ALL)
                || enchantment.equals(Enchantment.DIG_SPEED)
                || enchantment.equals(Enchantment.ARROW_DAMAGE)
                || enchantment.equals(Enchantment.LOYALTY)) {
            return 1;
        } else if (enchantment.equals(Enchantment.PROTECTION_FIRE)
                || enchantment.equals(Enchantment.PROTECTION_FALL)
                || enchantment.equals(Enchantment.PROTECTION_PROJECTILE)
                || enchantment.equals(Enchantment.DAMAGE_UNDEAD)
                || enchantment.equals(Enchantment.DAMAGE_ARTHROPODS)
                || enchantment.equals(Enchantment.KNOCKBACK)
                || enchantment.equals(Enchantment.DURABILITY)
                || enchantment.equals(Enchantment.QUICK_CHARGE)) {
            return 2;
        } else if (enchantment.equals(Enchantment.THORNS)
                || enchantment.equals(Enchantment.SILK_TOUCH)
                || enchantment.equals(Enchantment.ARROW_INFINITE)
                || enchantment.equals(Enchantment.CHANNELING)
                || enchantment.equals(Enchantment.SOUL_SPEED)) {
            return 8;
        } else if (enchantment.equals(Enchantment.BINDING_CURSE)
                || enchantment.equals(Enchantment.VANISHING_CURSE)) {
            return 16;
        }

        return 4;
    }

    private boolean isCopiableEnchant(final Enchantment enchantment) {
        if (enchantment.equals(Enchantment.BINDING_CURSE)
                || enchantment.equals(Enchantment.VANISHING_CURSE)) {
            return false;
        }
        return true;
    }
}
