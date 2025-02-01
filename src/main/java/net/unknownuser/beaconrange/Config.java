package net.unknownuser.beaconrange;

import com.google.gson.*;
import com.google.gson.annotations.*;
import net.fabricmc.loader.api.*;
import net.minecraft.util.*;

import java.io.*;
import java.util.*;

public class Config implements Serializable {
	public static class Defaults {
		private Defaults() {}
		
		static final int                     RANGE_PER_LEVEL_MULTIPLIER = 10;
		static final int                     MINIMUM_RANGE              = 10;
		static final Map<Identifier, Double> RANGE_EXTENDER_MULTIPLIER  = new HashMap<>();
		
		static {
			initializeMultiplierTable();
		}
		
		private static void initializeMultiplierTable() {
			RANGE_EXTENDER_MULTIPLIER.put(Identifier.ofVanilla("diamond_block"), 2.0);
			RANGE_EXTENDER_MULTIPLIER.put(Identifier.ofVanilla("netherite_block"), 4.0);
		}
		
		private static Config defaultConfig() {
			return new Config(RANGE_PER_LEVEL_MULTIPLIER, MINIMUM_RANGE, RANGE_EXTENDER_MULTIPLIER);
		}
	}
	
	public static final File   CONFIG_FILE = FabricLoader.getInstance()
		.getConfigDir()
		.resolve("beacon-range-extender.json")
		.toFile();
	private static      Config instance    = null;
	
	public Config(int rangePerLevel, int baseRange, Map<Identifier, Double> rangeMultipliers) {
		this.rangePerLevel    = rangePerLevel;
		this.baseRange        = baseRange;
		this.rangeMultipliers = rangeMultipliers;
	}
	
	@Expose
	protected final   int                     rangePerLevel;
	@Expose
	protected final   int                     baseRange;
	@Expose
	protected final   Map<Identifier, Double> rangeMultipliers; // NOSONAR
	// Gson *will* write all fields, even when enabling ignoreNonExposed
	// use transient to even skip the inclusion
	private transient boolean                 hasError = false;
	
	public void enableError() {
		hasError = true;
	}
	
	public static int rangePerLevel() {
		return instance.rangePerLevel;
	}
	
	public static int baseOffset() {
		return instance.baseRange;
	}
	
	public static double blockMultiplier(Identifier identifier) {
		return instance.rangeMultipliers.getOrDefault(identifier, 1.0);
	}
	
	private static Config getConfigFromFile() {
		Config cfg;
		
		if (CONFIG_FILE.exists()) {
			BeaconRange.LOGGER.info("using existing config file");
			cfg = loadConfig();
		} else {
			BeaconRange.LOGGER.info("config file is missing, creating using default settings!");
			cfg = Defaults.defaultConfig();
			cfg.write();
		}
		
		return cfg;
	}
	
	private static Config loadConfig() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Config.class, new ConfigDeserializer())
			.excludeFieldsWithoutExposeAnnotation()
			.setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
			.create();
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			Config config = gson.fromJson(reader, Config.class);
			
			if (config.hasError) {
				BeaconRange.LOGGER.warn("config has errors, rewriting config file");
				config.write();
			}
			
			return config;
		} catch (IOException e) {
			BeaconRange.LOGGER.error("Could not read config file: {}", e.getMessage());
			BeaconRange.LOGGER.warn("Using default values for settings!");
			
			Config config = Defaults.defaultConfig();
			config.write();
			return config;
		} catch (JsonSyntaxException exc) {
			BeaconRange.LOGGER.error("The config file '{}' has errors!", CONFIG_FILE);
			BeaconRange.LOGGER.error("Aborting start to prevent errors or data loss!");
			throw exc;
		}
	}
	
	// Fabric need to be fully initialized
	// loading during class-initialization will break FabricLoader and therefore the config dir
	public static void init() {
		instance = getConfigFromFile();
	}
	
	public void write() {
		Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			gson.toJson(this, writer);
		} catch (IOException e) {
			BeaconRange.LOGGER.error("Could not write config file! Reason: %s", e);
		}
	}
}
