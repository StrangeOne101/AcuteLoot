package acute.loot.enchanting;

import acute.loot.AcuteLoot;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EnchantUtils {

    public static final Map<Enchantment, Integer> MAX_LEVELS = new HashMap<Enchantment, Integer>();

    private static final Map<Enchantment, Object> NMS_OBJECTS = new HashMap<Enchantment, Object>();
    private static final Map<Material, Integer> ENCHANTABILITY = new HashMap<Material, Integer>(); //Too annoying to do via reflection
    private static final Map<Enchantment, Integer> WEIGHTS = new HashMap<Enchantment, Integer>(); //Also done manually because its easier

    private static Method getMinLevelForLevelStrength;
    private static Method getMaxLevelForLevelStrength;
    private static Method getHandle;
    private static AcuteLoot plugin;

    private static boolean overclockHack = true;

    static {
        /**
         * Reflection time!
         */

        String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
        try {
            Class enchantClass = Class.forName("net.minecraft.server." + NMS_VERSION + ".Enchantment");
            getMinLevelForLevelStrength = enchantClass.getDeclaredMethod("a", Integer.TYPE);
            getMaxLevelForLevelStrength = enchantClass.getDeclaredMethod("b", Integer.TYPE);

            Class craftEnchantClass = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".enchantments.CraftEnchantment");
            getHandle = craftEnchantClass.getDeclaredMethod("getHandle");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        /**
         * Map enchants with their max levels as well as the NMS objects. We store each NMS enchantment
         * in a map for easy access, so we don't have to use reflection as much at runtime to get them
         */

        for (Enchantment enchant : Enchantment.values()) {
            try {
                NMS_OBJECTS.put(enchant, getHandle.invoke(enchant));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        /**
         * Set the enchantability of each material. Done manually instead of reflection because it's nested within multiple classes and methods
         * and it's annoying to access
         */

        for (Material m : new Material[] {Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, Material.WOODEN_SWORD,
                Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET, Material.LEATHER_LEGGINGS,
                Material.NETHERITE_AXE, Material.NETHERITE_HOE, Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_SWORD,
                Material.NETHERITE_BOOTS, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_HELMET, Material.NETHERITE_LEGGINGS}) {
            ENCHANTABILITY.put(m, 15);
        }

        for (Material m : new Material[] {Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, Material.STONE_SWORD}) {
            ENCHANTABILITY.put(m, 5);
        }

        for (Material m : new Material[] {Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_LEGGINGS, Material.STONE_SWORD}) {
            ENCHANTABILITY.put(m, 12);
        }

        for (Material m : new Material[] {Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD}) {
            ENCHANTABILITY.put(m, 14);
        }

        for (Material m : new Material[] {Material.IRON_BOOTS, Material.IRON_CHESTPLATE, Material.IRON_HELMET, Material.IRON_LEGGINGS, Material.IRON_SWORD}) {
            ENCHANTABILITY.put(m, 9);
        }

        for (Material m : new Material[] {Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD}) {
            ENCHANTABILITY.put(m, 22);
        }

        for (Material m : new Material[] {Material.GOLDEN_BOOTS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET, Material.GOLDEN_LEGGINGS, Material.GOLDEN_SWORD}) {
            ENCHANTABILITY.put(m, 25);
        }

        for (Material m : new Material[] {Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_SWORD,
                Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET, Material.DIAMOND_LEGGINGS, Material.DIAMOND_SWORD}) {
            ENCHANTABILITY.put(m, 10);
        }

        ENCHANTABILITY.put(Material.TURTLE_HELMET, 9);
        ENCHANTABILITY.put(Material.BOOK, 1);

        /**
         * Set the weights for each enchantment. We could also do this via reflection but it's also a pain in the ass and not worth it
         */

        for (Enchantment e : new Enchantment[] {Enchantment.DAMAGE_ALL, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DIG_SPEED, Enchantment.ARROW_DAMAGE, Enchantment.PIERCING}) {
            WEIGHTS.put(e, 10);
        }

        for (Enchantment e : new Enchantment[] {Enchantment.THORNS, Enchantment.BINDING_CURSE, Enchantment.SILK_TOUCH, Enchantment.ARROW_INFINITE, Enchantment.CHANNELING, Enchantment.VANISHING_CURSE}) {
            WEIGHTS.put(e, 1);
        }
        //1.15 and lower patch
        if (Enchantment.getByKey(NamespacedKey.minecraft("soul_speed")) != null) {
            WEIGHTS.put(Enchantment.getByKey(NamespacedKey.minecraft("soul_speed")), 1);
        }

        for (Enchantment e : new Enchantment[] {Enchantment.OXYGEN, Enchantment.DEPTH_STRIDER, Enchantment.WATER_WORKER, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.FROST_WALKER,
                Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.ARROW_FIRE, Enchantment.ARROW_KNOCKBACK, Enchantment.LURE,
                Enchantment.MULTISHOT, Enchantment.IMPALING, Enchantment.RIPTIDE, Enchantment.LUCK, Enchantment.MENDING}) {
            WEIGHTS.put(e, 2);
        }

        //For all enchantments we haven't listed, they are weight 5. This may not be correct for future enchants added, but they can be manually patched
        for (Enchantment e : Enchantment.values()) {
            if (!WEIGHTS.containsKey(e)) WEIGHTS.put(e, 5);
        }

    }

    public static void setup(AcuteLoot plugin) {
        EnchantUtils.plugin = plugin;
        FileConfiguration fc = plugin.getConfig();

        MAX_LEVELS.put(Enchantment.ARROW_KNOCKBACK, fc.getInt("loot-enchants.enchantments.max-level.punch", Enchantment.ARROW_KNOCKBACK.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DAMAGE_ALL, fc.getInt("loot-enchants.enchantments.max-level.sharpness", Enchantment.DAMAGE_ALL.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DAMAGE_ARTHROPODS, fc.getInt("loot-enchants.enchantments.max-level.bane-of-athropods", Enchantment.DAMAGE_ARTHROPODS.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DAMAGE_UNDEAD, fc.getInt("loot-enchants.enchantments.max-level.smite", Enchantment.DAMAGE_UNDEAD.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DEPTH_STRIDER, fc.getInt("loot-enchants.enchantments.max-level.depth-strider", Enchantment.DEPTH_STRIDER.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DIG_SPEED, fc.getInt("loot-enchants.enchantments.max-level.efficiency", Enchantment.DIG_SPEED.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.DURABILITY, fc.getInt("loot-enchants.enchantments.max-level.unbreaking", Enchantment.DURABILITY.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.FIRE_ASPECT, fc.getInt("loot-enchants.enchantments.max-level.fire-aspect", Enchantment.FIRE_ASPECT.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.FROST_WALKER, fc.getInt("loot-enchants.enchantments.max-level.frost-walker", Enchantment.FROST_WALKER.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.IMPALING, fc.getInt("loot-enchants.enchantments.max-level.impaling", Enchantment.IMPALING.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.KNOCKBACK, fc.getInt("loot-enchants.enchantments.max-level.knockback", Enchantment.KNOCKBACK.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.LOOT_BONUS_BLOCKS, fc.getInt("loot-enchants.enchantments.max-level.fortune", Enchantment.LOOT_BONUS_BLOCKS.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.LOOT_BONUS_MOBS, fc.getInt("loot-enchants.enchantments.max-level.looting", Enchantment.LOOT_BONUS_MOBS.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.LOYALTY, fc.getInt("loot-enchants.enchantments.max-level.loyalty", Enchantment.LOYALTY.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.LUCK, fc.getInt("loot-enchants.enchantments.max-level.luck-of-the-sea", Enchantment.LUCK.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.LURE, fc.getInt("loot-enchants.enchantments.max-level.lure", Enchantment.LURE.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.OXYGEN, fc.getInt("loot-enchants.enchantments.max-level.respiration", Enchantment.OXYGEN.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PIERCING, fc.getInt("loot-enchants.enchantments.max-level.piercing", Enchantment.PIERCING.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PROTECTION_ENVIRONMENTAL, fc.getInt("loot-enchants.enchantments.protection", Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PROTECTION_EXPLOSIONS, fc.getInt("loot-enchants.enchantments.max-level.blast-protection", Enchantment.PROTECTION_EXPLOSIONS.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PROTECTION_FALL, fc.getInt("loot-enchants.enchantments.max-level.feather-falling", Enchantment.PROTECTION_FALL.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PROTECTION_FIRE, fc.getInt("loot-enchants.enchantments.max-level.fire-protection", Enchantment.PROTECTION_FIRE.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.PROTECTION_PROJECTILE, fc.getInt("loot-enchants.enchantments.max-level.projectile-protection", Enchantment.PROTECTION_PROJECTILE.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.QUICK_CHARGE, fc.getInt("loot-enchants.enchantments.max-level.quick-charge", Enchantment.LUCK.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.RIPTIDE, fc.getInt("loot-enchants.enchantments.max-level.riptide", Enchantment.RIPTIDE.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.SWEEPING_EDGE, fc.getInt("loot-enchants.enchantments.max-level.sweeping-edge", Enchantment.SWEEPING_EDGE.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.SILK_TOUCH, fc.getInt("loot-enchants.enchantments.max-level.silk-touch", Enchantment.SILK_TOUCH.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.THORNS, fc.getInt("loot-enchants.enchantments.max-level.thorns", Enchantment.THORNS.getMaxLevel()));
        MAX_LEVELS.put(Enchantment.WATER_WORKER, fc.getInt("loot-enchants.enchantments.max-level.aqua-affinity", Enchantment.WATER_WORKER.getMaxLevel()));

        if (Enchantment.getByKey(NamespacedKey.minecraft("soul_speed")) != null) {
            MAX_LEVELS.put(Enchantment.getByKey(NamespacedKey.minecraft("soul_speed")), fc.getInt("loot-enchants.enchantments.max-level.soul-speed", Enchantment.getByKey(NamespacedKey.minecraft("soul_speed")).getMaxLevel()));
        }

    }

    /**
     * Enchants an item using the same mechanics as vanilla
     * @param stack The itemstack
     * @param levels The amount of levels in this enchant. Levels higher than 30 can be used
     * @param bonusEnchantability The amount of bonus enchantability
     * @param treasure If treasure enchants are allowed
     * @param curses If curse enchants are allowed
     * @param ignoreItemType If the enchantments should be allowed on the item regardless of its type
     * @return The enchanted ItemStack
     */
    public static ItemStack enchant(ItemStack stack, int levels, int bonusEnchantability, boolean treasure, boolean curses, boolean ignoreItemType) {
        if (!stack.getEnchantments().isEmpty()) return stack;

        Random random = ThreadLocalRandom.current();

        if (stack.getType() == Material.BOOK) {
            stack.setType(Material.ENCHANTED_BOOK);
        }

        //The logic bellow is taken straight from vanilla

        int enchantability = ENCHANTABILITY.containsKey(stack.getType()) ? ENCHANTABILITY.get(stack.getType()) : 1 + bonusEnchantability;
        int offset = 1 + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1);
        levels += offset;
        float randomness = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        levels = Math.max(Math.min(Math.round(levels + levels * randomness), 2147483647), 1);

        int maxedLevels = Math.max(levels, 100); //Make level unable to go past 100. Things past here are too chaotic.
        Map<Enchantment, Integer> allEnchs = getAllEnchants(stack, maxedLevels, treasure, curses, ignoreItemType);

        //The stuff bellow uses weighted random to pick enchants from the applicable enchants
        if (!allEnchs.isEmpty()) {

            int totalWeight = 0;
            for (Enchantment ench : allEnchs.keySet()) {
                totalWeight += WEIGHTS.get(ench);
            }
            List<Enchantment> enchs = new ArrayList<>(allEnchs.keySet());

            Enchantment ench = pickWeighted(enchs, allEnchs, random.nextInt(totalWeight));
            addEnchant(stack, ench, allEnchs.get(ench));
            enchs.remove(ench);
            totalWeight -= WEIGHTS.get(ench);

            while (random.nextInt(50 - offset) <= levels && !enchs.isEmpty()) {
                ench = pickWeighted(enchs, allEnchs, random.nextInt(totalWeight));

                if ((ench == Enchantment.LOOT_BONUS_BLOCKS && stack.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) ||
                        (ench == Enchantment.SILK_TOUCH && stack.getEnchantments().containsKey(Enchantment.LOOT_BONUS_BLOCKS))) {
                    totalWeight -= WEIGHTS.get(ench);
                    enchs.remove(ench);
                    continue; //Little hotfix to prevent items getting fortune AND silk touch
                }

                totalWeight -= WEIGHTS.get(ench);
                addEnchant(stack, ench, allEnchs.get(ench));
                enchs.remove(ench);

                levels /= 2;
            }
        }

        return stack;
    }

    public static ItemStack enchant(ItemStack stack, int levels) {
        return enchant(stack, levels, 0);
    }

    public static ItemStack enchant(ItemStack stack, int levels, int bonusEnchantability) {
        return enchant(stack, levels, bonusEnchantability, false, false, false);
    }

    public static ItemStack enchant(ItemStack stack, int levels, int bonusEnchantability, boolean treasure) {
        return enchant(stack, levels, bonusEnchantability, treasure, false, false);
    }

    public static ItemStack enchant(ItemStack stack, int levels, int bonusEnchantability, boolean treasure, boolean curses) {
        return enchant(stack, levels, bonusEnchantability, treasure, curses, false);
    }

    /**
     * Gets random enchants. Contains no randomness
     * @param stack
     * @param levels
     * @param treasure
     * @param curses
     * @param ignoreItemType
     * @return
     */
    public static Map<Enchantment, Integer> getAllEnchants(ItemStack stack, int levels, boolean treasure, boolean curses, boolean ignoreItemType) {
        Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();

        for (Enchantment enchant : Enchantment.values()) {
            if (!enchant.canEnchantItem(stack) && !ignoreItemType) continue; //Skip if the enchant isn't for this item and its not meant to override
            if (enchant.isTreasure() && !treasure) continue;
            if (enchant.isCursed() && !curses) continue;    //Seems that even through it is deprecated, there is no alternative. Vanilla enchantments use
            // Enchantment#c() to check if it's a curse but Bukkit doesn't use that

            //A lot of vanilla enchantment methods are not wrapped in the bukkit enchant class, so we have to use reflection to use them.
            //We could manually make a list of what they should be, but it'd be too hard and wouldn't support 3rd part enchants. This method
            //isn't meant to be called lots, so a tiny performance hit reflection from using reflection should be fine

            Object nmsEnchant = NMS_OBJECTS.get(enchant);
            boolean overclockFlag = MAX_LEVELS.containsKey(enchant) && MAX_LEVELS.get(enchant) > enchant.getMaxLevel();
            int maxLevel = overclockFlag ? MAX_LEVELS.get(enchant) : enchant.getMaxLevel();

            try {
                for (int lvl = maxLevel; lvl > enchant.getStartLevel() - 1; lvl--) {
                    int minLvlForStrength = (int) getMinLevelForLevelStrength.invoke(nmsEnchant, lvl);
                    int maxLvlForStrength = (int) getMaxLevelForLevelStrength.invoke(nmsEnchant, lvl);

                    if (levels >= minLvlForStrength && levels <= maxLvlForStrength) {
                        enchs.put(enchant, lvl);
                        break;
                    } else if (lvl == maxLevel && overclockHack) { //If the max level isnt custom set but we should still try the hack
                        int minLvlForStrength2 = (int) getMinLevelForLevelStrength.invoke(nmsEnchant, lvl + 1);
                        int maxLvlForStrength2 = (int) getMaxLevelForLevelStrength.invoke(nmsEnchant, lvl + 1);
                        if (overclockFlag) { //Handle enchantments that have been overclocked
                            if (levels >= minLvlForStrength2 && levels >= maxLvlForStrength) { //If the level is above the max range for the highest level, go with highest lvl
                                enchs.put(enchant, lvl);
                                break;
                            }
                        } else { //Vanilla enchants that aren't overclocked at all
                            if (maxLvlForStrength2 >= 45 && levels >= minLvlForStrength) { //If the enchantment would be out of range for high level enchantments, only check min level
                                enchs.put(enchant, lvl);
                                break;
                            } else if (maxLevel == 1 && (minLvlForStrength >= 20 || maxLvlForStrength >= 40) && levels >= minLvlForStrength) { //Fix for enchantments like AquaAffinity.
                                enchs.put(enchant, lvl);
                                break;
                            }
                        }

                    }
                }

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return enchs;
    }

    private static Enchantment pickWeighted(List<Enchantment> enchants, Map<Enchantment, Integer> levels, int weight) {
        for (int var2 = 0, var3 = enchants.size(); var2 < var3; var2++) {
            Enchantment ench = enchants.get(var2);
            weight -= WEIGHTS.get(ench);
            if (weight < 0)
                return ench;
        }
        return null;
    }

    private static void addEnchant(ItemStack stack, Enchantment enchantment, int level) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
            meta.addStoredEnchant(enchantment, level, true);
            stack.setItemMeta(meta);
        } else {
            stack.addUnsafeEnchantment(enchantment, level);
        }
    }
}
