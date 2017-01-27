package org.openmrs.addonindex.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson converter for Version.
 */
public class VersionTypeConverter implements JsonSerializer<Version>, JsonDeserializer<Version> {
	
	@Override
	public JsonElement serialize(Version version, Type type, JsonSerializationContext jsonSerializationContext) {
		return new JsonPrimitive(version.toString());
	}
	
	@Override
	public Version deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
			throws JsonParseException {
		return new Version(jsonElement.getAsString());
	}
}
