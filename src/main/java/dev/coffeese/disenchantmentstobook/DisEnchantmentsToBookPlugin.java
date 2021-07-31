package dev.coffeese.disenchantmentstobook;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author coffeese
 */
public class DisEnchantmentsToBookPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Enabled");

        this.getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }
}
