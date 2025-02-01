package net.unknownuser.beaconrange;

import com.google.gson.*;
import net.minecraft.util.*;

import java.lang.reflect.*;
import java.util.*;

// if this does not exist, Gson uses type defaults for each variable
// I hate this
// just use Jackson.

// This is technically not needed, but I hate whatever Gson is doing too much to just keep it
public class ConfigDeserializer implements JsonDeserializer<Config> {
	@Override
	public Config deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
		throws JsonParseException {
		JsonObject object  = jsonElement.getAsJsonObject();
		FieldGetter getter = new FieldGetter(object);
		
		// NOTE:
		// the names MUST equal Config.[attribute], otherwise there will be errors!
		int rangePerLevel = getter.getInt("rangePerLevel", Config.Defaults.RANGE_PER_LEVEL_MULTIPLIER);
		int baseOffset    = getter.getInt("baseRange", Config.Defaults.MINIMUM_RANGE);
		
		Map<String, JsonElement> rawMultipliers = getter.getMap("rangeMultipliers");
		Map<Identifier, Double>  multipliers    = new HashMap<>();
		
		for (Map.Entry<String, JsonElement> element : rawMultipliers.entrySet()) {
			try {
				Identifier identifier = Identifier.of(element.getKey());
				double     multiplier = element.getValue().getAsDouble();
				multipliers.put(identifier, multiplier);
			} catch (InvalidIdentifierException exc) {
				BeaconRange.LOGGER.error("Invalid block identifier in config: '{}'", element.getKey());
				throw exc;
			} catch (UnsupportedOperationException exc) {
				BeaconRange.LOGGER.error(
					"Identifier '{}' has invalid value '{}'",
					element.getKey(),
					element.getValue()
				);
				throw exc;
			}
		}
		
		/*
		This shouldn't be done -> player might have disabled it
		
		if (multipliers.isEmpty()) {
			multipliers = Config.Defaults.RANGE_EXTENDER_MULTIPLIER;
		}
		 */
		
		Config config = new Config(rangePerLevel, baseOffset, multipliers);
		
		if (getter.hasError()) {
			config.enableError();
		}
		
		return config;
	}
}
