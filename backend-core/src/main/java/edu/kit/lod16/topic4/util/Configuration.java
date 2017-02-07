package edu.kit.lod16.topic4.util;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@AllArgsConstructor
public class Configuration {
	private Map<String, Object> configMap;

	public Object get(String key) {
		List<String> parts = Arrays.asList(key.split(Pattern.quote(".")));
		if (parts.size() == 1) return configMap.get(parts.get(0));
		else return ((Map<String, Object>) get(String.join(".", parts.subList(0, parts.size() - 1)))).get(parts.get(parts.size() - 1));
	}
}