package net.unknownuser.beaconrange;

import com.google.gson.*;

import java.util.*;

// utility class
class FieldGetter {
	private final JsonObject object;
	private       boolean    hasError;
	
	public FieldGetter(JsonObject object) {
		this.object   = object;
		this.hasError = false;
	}
	
	private JsonElement get(String name) {
		// wrapper to identify errors
		JsonElement value = object.get(name);
		
		if (value == null) {
			this.hasError = true;
			return null;
		}
		
		return value;
	}
	
	public int getInt(String name, int fallback) {
		JsonElement element = get(name);
		
		if (element == null) {
			BeaconRange.LOGGER.warn("config entry '{}' is missing, using '{}' as value", name, fallback);
			return fallback;
		}
		
		return element.getAsInt();
	}
	
	public Map<String, JsonElement> getMap(String name) {
		JsonElement element = get(name);
		
		if (element == null) {
			return new HashMap<>();
		} else {
			return element.getAsJsonObject().asMap();
		}
	}
	
	public boolean hasError() {
		return hasError;
	}
}
