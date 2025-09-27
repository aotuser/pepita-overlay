package me.sqqo;

import me.sqqo.overlay.Overlay;
import me.sqqo.overlay.config.Config;
import me.sqqo.overlay.gui.GuiScreenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

/**
 * Credits to viniciusroger (rogeiro) and sqqo (me) for creating the Pepita Overlay.
 */
@Mod(modid = Pepita.MOD_ID, version = Pepita.VERSION)
public class Pepita {
    public static final String MOD_ID = "pepita_overlay";
    public static final String VERSION = "2.0.1";

    public static Minecraft mc = null;

    public static final Overlay OVERLAY = new Overlay();
    private static final GuiScreenOverlay guiOverlay = new GuiScreenOverlay();

    public static final KeyBinding toggleKey = new KeyBinding("Toggle Bind", Keyboard.KEY_B, "Pepita Overlay");
    public static final KeyBinding guiKey = new KeyBinding("Gui Bind", Keyboard.KEY_INSERT, "Pepita Overlay");

    public static final Config config = new Config();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(Pepita.guiKey);

        Pepita.mc = Minecraft.getMinecraft();
        Pepita.OVERLAY.init();

        config.setupConfig();
        config.loadConfig();
    }

    public static GuiScreenOverlay getGuiOverlay() {
        return guiOverlay;
    }
}
