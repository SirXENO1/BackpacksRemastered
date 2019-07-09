/*
 * BackpacksRemastered - remastered version of the popular Backpacks plugin
 * Copyright (C) 2019 Division Industries LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.divisionind.bprm.events;

import com.divisionind.bprm.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class BackpackLinkEvent implements Listener {
    @EventHandler
    public void onLinkBackpack(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack inhand = e.getPlayer().getInventory().getItemInMainHand();
            if (!inhand.getType().equals(Material.LEATHER_CHESTPLATE)) return;

            try {
                Object craftItemStack = NMSReflector.asNMSCopy(inhand);
                Object tagCompound = NMSReflector.getNBTTagCompound(craftItemStack);
                if (NMSReflector.hasNBTKey(tagCompound, "backpack_type")) {
                    int type = (int)NMSReflector.getNBT(tagCompound, NBTType.INT, "backpack_type");
                    if (type == BackpackObject.LINKED.getTypeId()) {
                        // the player is left clicking a block with a linked backpack in hand, this is lookin guuuuuddd

                        Block block = e.getClickedBlock();
                        Material blockMat = block.getType();
                        if (blockMat.equals(Material.CHEST) || blockMat.equals(Material.TRAPPED_CHEST) || blockMat.equals(Material.FURNACE)) {
                            NMSReflector.setNBT(tagCompound, NBTType.BYTE_ARRAY, "backpack_data", BackpackSerialization.toByteArrayLocation(block.getLocation()));
                            ItemStack newBackpack = NMSReflector.asBukkitCopy(craftItemStack);
                            ItemMeta meta = newBackpack.getItemMeta();
                            List<String> newLore = new ArrayList<>(BackpackObject.LINKED.getHandler().lore().build());
                            newLore.add("");
                            newLore.add(Backpacks.translate("&aLinked"));
                            meta.setLore(newLore);
                            newBackpack.setItemMeta(meta);
                            e.getPlayer().getInventory().setItemInMainHand(newBackpack);
                            ACommand.respond(e.getPlayer(), "&eYou feel a strong connection form between the backpack and the container.");
                        }
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
