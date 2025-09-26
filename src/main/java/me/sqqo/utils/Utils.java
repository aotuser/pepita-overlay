package me.sqqo.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static me.sqqo.Pepita.mc;

public class Utils {
    public static boolean isBot(EntityPlayer en, boolean strict) {
        return Minecraft.getMinecraft().getNetHandler().getPlayerInfo(en.getUniqueID()) == null || (!en.getDisplayName().getUnformattedText().startsWith("§") && strict);
    }

    public static void print(String str, Object... args) {
        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§7[§e§lPepita§r§7] " + str, args)));
    }

    public static void print(String str) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§e§lPepita§r§7] " + str));
    }

    public static float round(float number, int places) {
        BigDecimal bd = new BigDecimal(number).setScale(places, RoundingMode.HALF_UP);

        return bd.floatValue();
    }
}
