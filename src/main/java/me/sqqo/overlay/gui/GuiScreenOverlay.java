package me.sqqo.overlay.gui;

import me.sqqo.Pepita;
import me.sqqo.overlay.config.Config;
import me.sqqo.overlay.gui.bind.GuiButtonBind;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

public class GuiScreenOverlay extends GuiScreen {
    GuiPageButtonList.GuiResponder responder = new GuiPageButtonList.GuiResponder() {
        @Override
        public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {}

        @Override
        public void onTick(int id, float value) {
            int rounded = Math.round(value);

            if (id == 0) {
                Pepita.config.updateConfig(rounded, Pepita.toggleKey.getKeyCode());
            }
        }

        @Override
        public void func_175319_a(int p_175319_1_, String p_175319_2_) {}
    };

    GuiSlider requestsDelay = new GuiSlider(responder, 0, 0, 120, "Requests Delay", 1, 40, 20, new GuiSlider.FormatHelper() {
        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + Math.round(value) + " ticks";
        }
    });

    GuiButton saveConfig = new GuiButton(1, 0, 0, "Save Config");

    GuiButtonBind toggleBind = new GuiButtonBind(2, 0, 0, Keyboard.KEY_B);

    public GuiScreenOverlay() {
        toggleBind.width = 150;
    }

    @Override
    public void initGui() {
        ScaledResolution res = new ScaledResolution(mc);

        requestsDelay.xPosition = (res.getScaledWidth() / 2) - 75;
        requestsDelay.yPosition = 140;

        toggleBind.xPosition = (res.getScaledWidth() / 2) - (toggleBind.width / 2);
        toggleBind.yPosition = 200;

        saveConfig.xPosition = (res.getScaledWidth() / 2) - (saveConfig.width / 2);
        saveConfig.yPosition = this.height - 35;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution res = new ScaledResolution(mc);

        drawDefaultBackground();

        GlStateManager.pushMatrix();

        final String title = "§e§lP§7§lepita Overlay";

        GlStateManager.translate((res.getScaledWidth() / 2F) - fontRendererObj.getStringWidth(title), 40, 1);

        GlStateManager.scale(2F, 2F, 1);

        fontRendererObj.drawStringWithShadow(title, 0, 0, -1);

        GlStateManager.popMatrix();

        fontRendererObj.drawStringWithShadow("§7Made by §b§lsqqo", (res.getScaledWidth() / 2F) - (fontRendererObj.getStringWidth("§7Made by §b§lsqqo") / 2F), 40 + 19, -1);

        drawCenteredString(fontRendererObj, "Requests", this.width / 2, 140 - 22, -1);

        drawCenteredString(fontRendererObj, "Binds", this.width / 2, 200 - 18, -1);

        requestsDelay.drawButton(mc, mouseX, mouseY);
        saveConfig.drawButton(mc, mouseX, mouseY);
        toggleBind.drawButton(mc, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        requestsDelay.mousePressed(mc, mouseX, mouseY);

        if (saveConfig.mousePressed(mc, mouseX, mouseY)) {
            Pepita.config.saveConfig();
        }

        toggleBind.onMouse(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        requestsDelay.mouseReleased(mouseX, mouseY);
        saveConfig.mouseReleased(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        int k = toggleBind.onKeyPress(keyCode);

        if (k != -999) {
            Pepita.config.updateConfig(Pepita.config.getRequestsDelay(), k);
        }

        if (keyCode != Keyboard.KEY_ESCAPE && keyCode != Pepita.guiKey.getKeyCode()) {
            return;
        }

        mc.displayGuiScreen(null);
        requestsDelay.mouseReleased(0, 0);
        saveConfig.mouseReleased(0, 0);
    }

    public void loadConfig(Config config) {
        this.requestsDelay.func_175218_a((float) config.getRequestsDelay(), false);
        this.toggleBind.setKey(config.getToggleKey());

        Pepita.toggleKey.setKeyCode(config.getToggleKey());
    }
}
