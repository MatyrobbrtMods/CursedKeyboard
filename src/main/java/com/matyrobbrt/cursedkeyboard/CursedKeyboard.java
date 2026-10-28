package com.matyrobbrt.cursedkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mod(value = CursedKeyboard.MODID, dist = Dist.CLIENT)
public class CursedKeyboard {
    public static final String MODID = "cursedkeyboard";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue MINIMUM_CURSE_INTERVAL, MAX_CURSES;

    static {
        var builder = new ModConfigSpec.Builder();
        MINIMUM_CURSE_INTERVAL = builder.comment("The minimum amount of seconds that must pass before keymappings can be cursed again.")
                .defineInRange("minimum_curse_interval", 60 * 20, 1, Integer.MAX_VALUE / 2); // 20 minutes
        MAX_CURSES = builder.comment("The maximum amount of active keybind curses")
                .defineInRange("max_curses", 1, 1, 50);
        SPEC = builder.build();
    }

    private static final float INCREASING_CHANCE = 0.0001f; // 0.2% per second

    public static final Tooltip CANNOT_CHANGE = Tooltip.create(Component.translatable("message.cursedkeyboard.cannot_change_keybind"));

    public CursedKeyboard(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener((final FMLClientSetupEvent _) -> CursedKeybinds.load());

        NeoForge.EVENT_BUS.addListener(this::onTick);

        modContainer.registerConfig(ModConfig.Type.CLIENT, SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private int counter;
    private void onTick(final ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        counter++;

        var tickRate = minecraft.level.tickRateManager().tickrate();

        if (counter > tickRate * MINIMUM_CURSE_INTERVAL.getAsInt() && (counter - tickRate * MINIMUM_CURSE_INTERVAL.getAsInt()) * INCREASING_CHANCE + minecraft.level.getRandom().nextFloat() > 0.95f) {
            counter = 0;
            var keybinds = minecraft.options.keyMappings;

            KeyMapping candidate;
            do {
                candidate = keybinds[minecraft.level.getRandom().nextIntBetweenInclusive(0, keybinds.length - 1)];
            } while (candidate.isUnbound() || candidate.getKey().getType() != InputConstants.Type.KEYSYM);

            final var selected = candidate;

            int candidateSelectedKey;
            do {
                candidateSelectedKey = minecraft.level.getRandom().nextIntBetweenInclusive(InputConstants.KEY_A, InputConstants.KEY_Z);
            } while (candidateSelectedKey == selected.getKey().getValue());

            final var newSelectedKey = InputConstants.Type.KEYSYM.getOrCreate(candidateSelectedKey);

            var otherBound = Arrays.stream(minecraft.options.keyMappings)
                    .filter(mp -> Objects.equals(mp.getKey(), newSelectedKey))
                    .toList();

            List<CursedKeybinds.ChangedKeybind> reversed = new ArrayList<>();
            while (CursedKeybinds.getChangesCount() >= MAX_CURSES.getAsInt()) {
                var first = CursedKeybinds.removeFirst();
                reversed.addAll(first.changed());
                first.reverse();
            }
            if (!reversed.isEmpty()) {
                minecraft.getChatListener().handleSystemMessage(Component.translatable("message.cursedkeyboard.curse_reverse",
                        join(Component.literal(", "), reversed.stream()
                                .map(k -> k.asText(false))
                                .toList())), false);
            }

            var swap = new CursedKeybinds.KeybindSwap(
                    Stream.concat(
                                    otherBound.stream().map(m -> new CursedKeybinds.ChangedKeybind(m.getName(), m.getKey().getName(), selected.getKey().getName())),
                                    Stream.of(new CursedKeybinds.ChangedKeybind(selected.getName(), selected.getKey().getName(), newSelectedKey.getName()))
                            )
                            .toList()
            );
            CursedKeybinds.add(swap);

            swap.apply();
            minecraft.getChatListener().handleOverlay(Component.translatable("message.cursedkeyboard.your_keyboards_cursed",
                    join(Component.literal(", "), swap.changed().stream()
                            .map(k -> k.asText(true))
                            .toList())));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static Component join(Component separator, Iterable<Component> toJoin) {
        var iter = toJoin.iterator();
        MutableComponent result = Component.empty().append(iter.next());
        while (iter.hasNext()) {
            result = result.append(separator).append(iter.next());
        }
        return result;
    }
}
