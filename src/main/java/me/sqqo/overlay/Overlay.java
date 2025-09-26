package me.sqqo.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sqqo.Pepita;
import me.sqqo.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.sqqo.Pepita.mc;

public class Overlay {
    private boolean toggled = true;

    float posX = 3;
    float posY = 3;

    private final DataProcessor dataProcessor = new DataProcessor(this::onPlayerData, 1500);

    private final ConcurrentHashMap<EntityPlayer, UserData> playersCache = new ConcurrentHashMap<>();

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
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
    public void onTick(TickEvent event) {
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
            if (Utils.isBot(en) || playersCache.containsKey(en) || dataProcessor.getDataQueue().contains(en) || dataProcessor.getWaitingQueue().contains(en) || en == mc.thePlayer) {
                continue;
            }

            this.dataProcessor.getDataQueue().add(en);
            this.dataProcessor.getWaitingQueue().add(en);
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
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!toggled || playersCache.isEmpty()) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        final float nameSize = mc.fontRendererObj.getStringWidth("123456789012345678900909");
        final float fkdrSize = mc.fontRendererObj.getStringWidth("1000.00");

        final float winsSize = mc.fontRendererObj.getStringWidth("100000");
        final float lossesSize = mc.fontRendererObj.getStringWidth("100000");

        final float winStreakSize = mc.fontRendererObj.getStringWidth("10000");

        float x = posX;
        float y = posY;

        mc.fontRendererObj.drawStringWithShadow("§7Name", x, y, -1);
        final float namePos = x;
        x += nameSize;

        mc.fontRendererObj.drawStringWithShadow("§7FKDR", x, y, -1);
        final float fkdrPos = x;
        x += fkdrSize;

        mc.fontRendererObj.drawStringWithShadow("§7Wins", x, y, -1);
        final float winsPos = x;
        x += winsSize;

        mc.fontRendererObj.drawStringWithShadow("§7Losses", x, y, -1);
        final float lossesPos = x;
        x += lossesSize + 10;

        mc.fontRendererObj.drawStringWithShadow("§7Winstreak", x, y, -1);
        final float winStreakPos = x;
        x += winStreakSize + 25;

        mc.fontRendererObj.drawStringWithShadow("§7Level", x, y, -1);
        final float levelPos = x;

        y += 11;

        for (Map.Entry<EntityPlayer, UserData> en : playersCache.entrySet()) {
            final boolean nicked = en.getValue().nicked;
            final String name = en.getKey().getDisplayName().getUnformattedText();

            if (nicked) {
                mc.fontRendererObj.drawStringWithShadow(name, namePos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§cNicked", fkdrPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§cNicked", winsPos, y, -1);
                mc.fontRendererObj.drawStringWithShadow("§cNicked", lossesPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§cNicked", winStreakPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§cNicked", levelPos, y, -1);
            } else {
                mc.fontRendererObj.drawStringWithShadow(name, namePos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§7" + en.getValue().fkdr, fkdrPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§7" + en.getValue().wins, winsPos, y, -1);
                mc.fontRendererObj.drawStringWithShadow("§7" + en.getValue().losses, lossesPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow("§7" + en.getValue().winStreak, winStreakPos, y, -1);

                mc.fontRendererObj.drawStringWithShadow(en.getValue().fLevel, levelPos, y, -1);
            }

            y += 10;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Pepita.toggleKey.isPressed()) {
            this.toggled = !this.toggled;
        }

        if (Pepita.clearKey.isPressed()) {
            if (!this.dataProcessor.getDataQueue().isEmpty()) {
                this.dataProcessor.getDataQueue().clear();
            }

            if (!this.dataProcessor.getWaitingQueue().isEmpty()) {
                this.dataProcessor.getWaitingQueue().clear();
            }

            dataProcessor.currDelay = 0;
            playersCache.clear();
        }
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
