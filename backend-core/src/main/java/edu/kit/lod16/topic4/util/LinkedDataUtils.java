package edu.kit.lod16.topic4.util;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LinkedDataUtils {

	/**
	 * Returns one string out of @strings which is most similar to @reference, according to normalized Levenshtein similarity.
	 * @param strings Set of string to be compared to @reference.
	 * @param reference Reference string to compare others to.
	 * @return Most similar string.
	 */
	public static String getMostSimilar(Collection<String> strings, String reference) {
		NormalizedLevenshtein l = new NormalizedLevenshtein();

		return strings.stream()
				.map(s -> new AbstractMap.SimpleEntry<String, Double>(s, l.similarity(s, reference)))
				.sorted((k1, k2) -> Double.compare(k2.getValue(), k1.getValue()))
				.findFirst()
				.get()
				.getKey();
	}

	/**
	 * Generates a new JSON-LD object from multiple origin objects, whereas the predicates get suffixed with _ plus the respective entry of @orderedSuffixes.
	 * The order of @orderedSuffixes must match the order @graphs.
	 * E.g. shortened property "label" from the first graph is copied as "label_yago", given @orderedSuffixes[0] is "yago".
	 * @param graphs Graphs to be merged.
	 * @param orderedSuffixes Suffixes for the respective graphs.
	 * @param newId Id of the new object.
	 * @return Merged JSON-LD object containing all properties of all origin graphs, but suffixed.
	 */
	public static JSONObject mergeJsonLd(List<JSONObject> graphs, String[] orderedSuffixes, String newId, Map<String, String> customPrefixes) {
		JSONObject newGraph = new JSONObject();
		int graphCounter = 0;
		newGraph.put("@id", newId);

		for (JSONObject g : graphs) {
			if (!g.has("@context")) continue;
			g.remove("@id");
			newGraph.put("@context", new JSONObject());
			JSONObject currentContext = g.getJSONObject("@context");
			for (String ck : currentContext.keySet()) {
				newGraph.getJSONObject("@context").put(ck + "_" + orderedSuffixes[graphCounter], currentContext.get(ck));
			}

			g.remove("@context");

			for (String pk : g.keySet()) {
				newGraph.put(pk + "_" + orderedSuffixes[graphCounter], g.get(pk));
			}
			graphCounter++;
		}

		for (String p : customPrefixes.keySet()) {
			if (!newGraph.has("@context")) continue;
			newGraph.getJSONObject("@context").put(p, customPrefixes.get(p));
		}

		return newGraph;
	}

	public static Collection<String> uniqueCollection(Collection<String> c) {
		Map<String, Boolean> seen = new HashMap();
		Collection<String> uniqueCollection = new LinkedList<>();
		for (String o : c) {
			if (!seen.containsKey(o)) {
				seen.put(o, true);
				uniqueCollection.add(o);
			}
		}
		return uniqueCollection;
	}

	public static List jsonArrayToList(JSONArray jsonArray) {
		Iterable<Object> iterable = () -> jsonArray.iterator();
		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
	}
}
