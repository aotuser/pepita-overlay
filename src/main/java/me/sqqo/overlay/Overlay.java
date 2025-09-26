package me.sqqo.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sqqo.Pepita;
import me.sqqo.events.PacketReceiveEvent;
import me.sqqo.events.PreUpdateEvent;
import me.sqqo.overlay.gui.GuiScreenOverlay;
import me.sqqo.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.sqqo.Pepita.mc;

public class Overlay {
    private boolean toggled = true;

    float posX = 3;
    float posY = 3;

    float namePos;
    float fkdrPos;
    float winsPos;
    float lossesPos;
    float winStreakPos;
    float levelPos;

    float tNamePos;
    float tFkdrPos;
    float tWinsPos;
    float tLossesPos;
    float tWinStreakPos;
    float tLevelPos;

    private final StringBuilder sb = new StringBuilder();

    private final DataProcessor dataProcessor = new DataProcessor(this::onPlayerData, 20);

    private final ConcurrentLinkedQueue<EntityPlayer> removeQueue = new ConcurrentLinkedQueue<>();
    private int removeTicks = 5;

    private final ConcurrentHashMap<EntityPlayer, UserData> playersCache = new ConcurrentHashMap<>();

    private static final String NICKED_TEXT = "§cNicked";

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);

        final float nameSize = mc.fontRendererObj.getStringWidth("12345678901234567890090912345678");
        final float fkdrSize = mc.fontRendererObj.getStringWidth("1000.0000");

        final float winsSize = mc.fontRendererObj.getStringWidth("10000000");
        final float lossesSize = mc.fontRendererObj.getStringWidth("10000000");

        final float winStreakSize = mc.fontRendererObj.getStringWidth("1000000");
        final float levelSize = mc.fontRendererObj.getStringWidth("[--10000]");

        float x = this.posX;

        this.namePos = x + (nameSize / 2);
        this.tNamePos = x;
        x += nameSize;

        this.fkdrPos = x + (fkdrSize / 2);
        this.tFkdrPos = x;
        x += fkdrSize;

        this.winsPos = x + (winsSize / 2);
        this.tWinsPos = x;
        x += winsSize;

        this.lossesPos = x + (lossesSize / 2);
        this.tLossesPos = x;
        x += lossesSize + 10;

        this.winStreakPos = x + (winStreakSize / 2);
        this.tWinStreakPos = x;
        x += winStreakSize + 25;

        this.levelPos = x + (levelSize / 2);
        this.tLevelPos = x;
    }

    public void onPlayerData(EntityPlayer en, String data) {
        final String name = en.getName();

        if (data == null) {
            return;
        }

        final JsonParser parser = new JsonParser();
        final JsonObject object = parser.parse(data).getAsJsonObject();

        final JsonObject response = object.get("response").getAsJsonObject();

        final String errorCode = object.get("error_code").getAsString();

        boolean nicked = false;

        if (errorCode.equals("404")) {
            mc.thePlayer.playSound("note.pling", 1, 1);
            Utils.print("Nicked player found: §l§b%s", name);

            nicked = true;
        }

        float fkdr = -1;

        int wins = -1;
        int losses = -1;
        int winStreak = -1;

        String level = "§7[0§l✫§7]";

        if (response.has("stats")) {
            JsonObject stats = response.get("stats").getAsJsonObject();

            if (stats.has("bedwars")) {
                JsonObject bedWars = stats.get("bedwars").getAsJsonObject();

                if (bedWars.has("wins")) {
                    wins = bedWars.get("wins").getAsInt();
                }

                if (bedWars.has("losses")) {
                    losses = bedWars.get("losses").getAsInt();
                }

                if (bedWars.has("winstreak")) {
                    winStreak = bedWars.get("winstreak").getAsInt();
                }

                if (bedWars.has("final_kills") && bedWars.has("final_deaths")) {
                    int finalKills = bedWars.get("final_kills").getAsInt();
                    int finalDeaths = bedWars.get("final_deaths").getAsInt();

                    if (finalDeaths == 0 || finalKills == 0) {
                        fkdr = finalKills;
                    } else {
                        fkdr = Utils.round((float) finalKills / finalDeaths, 1);
                    }
                }

                if (bedWars.has("level_badge")) {
                    JsonObject levelBadge = bedWars.get("level_badge").getAsJsonObject();

                    if (levelBadge.has("format")) {
                        level = levelBadge.get("format").getAsString().replace("&", "§");
                    }
                }
            }
        }

        UserData userData = new UserData(fkdr, wins, losses, winStreak, level, nicked);

        this.playersCache.put(en, userData);
        this.dataProcessor.getWaitingQueue().remove(en);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!toggled) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (dataProcessor.currDelay > 0) {
            dataProcessor.currDelay--;
        }

        for (EntityPlayer en : mc.theWorld.playerEntities) {
            if (Utils.isBot(en, true) || playersCache.containsKey(en) || dataProcessor.getDataQueue().contains(en) || dataProcessor.getWaitingQueue().contains(en) || en == mc.thePlayer) {
                continue;
            }

            this.dataProcessor.getDataQueue().add(en);
            this.dataProcessor.getWaitingQueue().add(en);
        }

        if (removeTicks >= 0) {
            removeTicks--;

            return;
        }

        for (EntityPlayer en : removeQueue) {
            playersCache.remove(en);

            dataProcessor.getDataQueue().remove(en);
            dataProcessor.getWaitingQueue().remove(en);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        dataProcessor.getDataQueue().clear();
        dataProcessor.getWaitingQueue().clear();
        dataProcessor.currDelay = 0;

        playersCache.clear();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof S13PacketDestroyEntities)) {
            return;
        }

        S13PacketDestroyEntities packet = (S13PacketDestroyEntities) event.getPacket();

        for (int i = 0; i < packet.getEntityIDs().length; i++) {
            Entity e =  mc.theWorld.getEntityByID(packet.getEntityIDs()[i]);

            if (!(e instanceof EntityPlayer)) {
                continue;
            }

            EntityPlayer en = (EntityPlayer) e;

            removeQueue.add(en);
            removeTicks = 5;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!toggled || playersCache.isEmpty()) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        float y = posY;

        mc.fontRendererObj.drawStringWithShadow("§7Name", this.namePos - s("§7Name"), y, -1);

        mc.fontRendererObj.drawStringWithShadow("§7FKDR", this.fkdrPos - s("§7FKDR"), y, -1);

        mc.fontRendererObj.drawStringWithShadow("§7Wins", this.winsPos - s("§7Wins"), y, -1);

        mc.fontRendererObj.drawStringWithShadow("§7Losses", this.lossesPos - s("§7Losses"), y, -1);

        mc.fontRendererObj.drawStringWithShadow("§7Winstreak", this.winStreakPos - s("§7Winstreak"), y, -1);

        mc.fontRendererObj.drawStringWithShadow("§7Level", this.levelPos - s("§7Level"), y, -1);

        y += 12;

        for (Map.Entry<EntityPlayer, UserData> en : playersCache.entrySet()) {
            final boolean nicked = en.getValue().nicked;
            final String name = en.getKey().getDisplayName().getUnformattedText();
            final float nameSize = mc.fontRendererObj.getStringWidth(name);

            if (nicked) {
                final float nickedSize = mc.fontRendererObj.getStringWidth(NICKED_TEXT);

                mc.fontRendererObj.drawStringWithShadow(name, namePos - (nameSize / 2), y, -1);

                mc.fontRendererObj.drawStringWithShadow(NICKED_TEXT, fkdrPos - (nickedSize / 2), y, -1);

                mc.fontRendererObj.drawStringWithShadow(NICKED_TEXT, winsPos - (nickedSize / 2), y, -1);
                mc.fontRendererObj.drawStringWithShadow(NICKED_TEXT, lossesPos - (nickedSize / 2), y, -1);

                mc.fontRendererObj.drawStringWithShadow(NICKED_TEXT, winStreakPos - (nickedSize / 2), y, -1);

                mc.fontRendererObj.drawStringWithShadow(NICKED_TEXT, levelPos - (nickedSize / 2), y, -1);
            } else {
                final UserData data = en.getValue();

                mc.fontRendererObj.drawStringWithShadow(name, namePos - (nameSize / 2), y, -1);

                final String fkdrText = "§7" + (data.fkdr == -1 ? "-" : data.fkdr);
                mc.fontRendererObj.drawStringWithShadow(fkdrText, fkdrPos - s(fkdrText), y, -1);

                sb.setLength(0);
                sb.append("§7").append(data.wins == -1 ? "-" : data.wins);
                mc.fontRendererObj.drawStringWithShadow(sb.toString(), winsPos - s(sb.toString()), y, -1);

                sb.setLength(0);
                sb.append("§7").append(data.losses == -1 ? "-" : data.losses);
                mc.fontRendererObj.drawStringWithShadow(sb.toString(), lossesPos - s(sb.toString()), y, -1);

                sb.setLength(0);
                sb.append("§7").append(data.winStreak == -1 ? "-" : data.winStreak);
                mc.fontRendererObj.drawStringWithShadow(sb.toString(), winStreakPos - s(sb.toString()), y, -1);

                mc.fontRendererObj.drawStringWithShadow(data.fLevel, levelPos - s(data.fLevel), y, -1);
            }

            y += 10;
        }
    }

    private float s(String text) {
        return mc.fontRendererObj.getStringWidth(text) / 2F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Pepita.toggleKey.isPressed()) {
            this.toggled = !this.toggled;
        }

        if (Pepita.guiKey.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(Pepita.getGuiOverlay());
        }
    }

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    static class UserData {
        final float fkdr;

        final int wins;
        final int losses;
        final int winStreak;

        final String fLevel;

        boolean nicked;

        public UserData(final float fkdr, final int wins, final int losses, final int winStreak, final String fLevel, boolean nicked) {
            this.fkdr = fkdr;
            this.wins = wins;
            this.losses = losses;
            this.winStreak = winStreak;
            this.fLevel = fLevel;
            this.nicked = nicked;
        }
    }
}
