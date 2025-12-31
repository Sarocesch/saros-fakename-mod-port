package tschipp.fakename;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-based config for Fabric.
 */
public class Config {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fakename.json");

	private static ConfigData data = new ConfigData();

	public static int getCommandPermissionLevelSelf() {
		return data.commandPermissionLevelSelf;
	}

	public static int getCommandPermissionLevelAll() {
		return data.commandPermissionLevelAll;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				data = GSON.fromJson(json, ConfigData.class);
				if (data == null) {
					data = new ConfigData();
				}
			} catch (IOException e) {
				FakeName.LOGGER.error("Failed to load config", e);
				data = new ConfigData();
			}
		} else {
			save();
		}
	}

	public static void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(data));
		} catch (IOException e) {
			FakeName.LOGGER.error("Failed to save config", e);
		}
	}

	private static class ConfigData {
		/** Permission level needed to change other people's fakename */
		int commandPermissionLevelAll = 2;

		/** Permission level needed to change your own fakename */
		int commandPermissionLevelSelf = 0;
	}
}
