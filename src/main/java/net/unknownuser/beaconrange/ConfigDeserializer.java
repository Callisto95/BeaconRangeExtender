package net.unknownuser.beaconrange;

import com.google.gson.*;
import com.google.gson.stream.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.*;

import java.io.*;
import java.util.*;

public class ConfigDeserializer extends TypeAdapter<Config> {
	@Override
	public void write(JsonWriter writer, Config value) throws IOException {
		if (value == null) {
			writer.nullValue();
			return;
		}
		
		writer.setFormattingStyle(FormattingStyle.PRETTY);
		writer.beginObject()
			.name("rangePerLevel")
			.value(value.rangePerLevel)
			.name("baseRange")
			.value(value.baseRange)
			.name("rangeMultipliers")
			.beginObject();
		
		for (Map.Entry<Block, Double> entry : value.rangeMultipliers.entrySet()) {
			Block  block      = entry.getKey();
			Double multiplier = entry.getValue();
			writer.name(BuiltInRegistries.BLOCK.getKey(block).toString()).value(multiplier);
		}
		
		writer.endObject().endObject();
	}
	
	@Override
	public Config read(JsonReader reader) throws IOException {
		int                rangePerLevel    = Config.Defaults.RANGE_PER_LEVEL_MULTIPLIER;
		int                baseRange        = Config.Defaults.MINIMUM_RANGE;
		Map<Block, Double> rangeMultipliers = new HashMap<>();
		
		reader.beginObject();
		
		while (reader.hasNext()) {
			String fieldName = reader.nextName();
			
			switch (fieldName) {
				case "rangePerLevel" -> rangePerLevel = reader.nextInt();
				case "baseRange" -> baseRange = reader.nextInt();
				case "rangeMultipliers" -> {
					reader.beginObject();
					
					while (!reader.peek().equals(JsonToken.END_OBJECT)) {
						String blockID    = reader.nextName();
						double multiplier = reader.nextDouble();
						
						Block block = BuiltInRegistries.BLOCK.get(Identifier.parse(blockID)).orElseThrow().value();
						
						rangeMultipliers.put(block, multiplier);
					}
					
					reader.endObject();
				}
				default -> BeaconRange.LOGGER.warn("Unknown config key: {}", fieldName);
			}
		}
		
		reader.endObject();
		
		return new Config(rangePerLevel, baseRange, rangeMultipliers);
	}
}
