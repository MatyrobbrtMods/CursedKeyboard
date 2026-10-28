package com.matyrobbrt.cursedkeyboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursedKeybinds {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STORAGE_PATH = Path.of("cursed_keybinds.json");
    private static List<KeybindSwap> changes = List.of();

    public static boolean isAffected(KeyMapping mapping) {
        for (KeybindSwap change : changes) {
            for (ChangedKeybind changedKeybind : change.changed) {
                if (changedKeybind.name.equals(mapping.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void load() {
        try (var reader = Files.newBufferedReader(STORAGE_PATH)) {
            var result = GSON.fromJson(reader, Data.class);
            changes = List.copyOf(result.changes);
        } catch (Exception e) {
            if (!(e instanceof NoSuchFileException)) {
                CursedKeyboard.LOGGER.error("Failed to read cursed keybinds list: ", e);
            }
        }
    }

    public static void save() {
        try (var writer = Files.newBufferedWriter(STORAGE_PATH)) {
            GSON.toJson(new Data(changes), writer);
        } catch (Exception e) {
            CursedKeyboard.LOGGER.error("Failed to write cursed keybinds list: ", e);
        }
    }

    public static int getChangesCount() {
        return changes.size();
    }

    public static KeybindSwap removeFirst() {
        var nc = new ArrayList<>(changes);
        var value = nc.removeFirst();
        changes = List.copyOf(nc);
        save();
        return value;
    }

    public static void add(KeybindSwap change) {
        var nc = new ArrayList<>(changes);
        nc.add(change);
        changes = List.copyOf(nc);
        save();
    }

    public record Data(List<KeybindSwap> changes) {

    }

    public record KeybindSwap(List<ChangedKeybind> changed) {
        public void apply() {
            for (ChangedKeybind changed : this.changed) {
                var mapping = changed.resolveMapping();
                mapping.setKey(InputConstants.getKey(changed.to));
            }
            KeyMapping.resetMapping();
        }

        public void reverse() {
            for (ChangedKeybind changed : this.changed) {
                var mapping = changed.resolveMapping();
                mapping.setKey(InputConstants.getKey(changed.from));
            }
            KeyMapping.resetMapping();
        }
    }

    public record ChangedKeybind(String name, String from, String to) {
        public KeyMapping resolveMapping() {
            return Arrays.stream(Minecraft.getInstance().options.keyMappings)
                    .filter(m -> m.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        public Component asText(boolean toNew) {
            return Component.translatable("message.cursedkeyboard.is_now", resolveMapping().getDisplayName().plainCopy().withStyle(ChatFormatting.GOLD), InputConstants.getKey(toNew ? to : from).getDisplayName().plainCopy().withStyle(ChatFormatting.AQUA));
        }
    }
}
