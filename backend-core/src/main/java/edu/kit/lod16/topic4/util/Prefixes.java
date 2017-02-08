package edu.kit.lod16.topic4.util;

import java.util.Map;
import java.util.stream.Collectors;

public class Prefixes {
	private static Map<String, String> replaceMap;
	private static Map<String, String> inverseReplaceMap;

	public static void init(Map<String, String> replaceMapData) {
		setReplaceMap(replaceMapData);
	}

	public static void setReplaceMap(Map<String, String> replaceMapData) {
		replaceMap = replaceMapData;
		inverseReplaceMap = replaceMap.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	public static Map<String, String> getReplaceMap() {
		return replaceMap;
	}

	public static Map<String, String> getInverseReplaceMap() {
		return inverseReplaceMap;
	}

	/* Shortens a long URI like http://dbpedia.org/Karlsruhe/ to dbr:Karlsruhe. */
	public static String replace(String uri) {
		return getReplaceMap().keySet().stream().filter(k -> uri.contains(k)).findFirst().map(k -> uri.replace(k, replaceMap.get(k))).orElse(uri);
	}

	/* Expands a shorthand URI like dbr:Karlsruhe to http://dbpedia.org/Karlsruhe/ */
	public static String replaceInverse(String shortName) {
		String f = getInverseReplaceMap().keySet().stream().filter(k -> shortName.contains(k)).findFirst().map(k -> shortName.replace(k, inverseReplaceMap.get(k))).orElse(shortName);
		return f;
	}
}
