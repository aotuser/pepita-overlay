package me.sqqo;

/**
 * Creditos ao viniciusroger (rogeiro) e sqqo (eu) por terem criado a Pepita Overlay.
 */

import me.sqqo.overlay.Overlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = Pepita.MOD_ID, version = Pepita.VERSION)
public class Pepita {
    public static final String MOD_ID = "pepita_overlay";
    public static final String VERSION = "2.0.1";

    public static Minecraft mc = null;

    public static final Overlay OVERLAY = new Overlay();

    public static final KeyBinding toggleKey = new KeyBinding("Toggle Bind", Keyboard.KEY_B, "Pepita Overlay");
    public static final KeyBinding clearKey = new KeyBinding("Clear Bind", Keyboard.KEY_INSERT, "Pepita Overlay");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(Pepita.toggleKey);
        ClientRegistry.registerKeyBinding(Pepita.clearKey);

        Pepita.mc = Minecraft.getMinecraft();
        Pepita.OVERLAY.init();
    }
}
