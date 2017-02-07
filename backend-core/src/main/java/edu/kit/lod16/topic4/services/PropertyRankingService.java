package edu.kit.lod16.topic4.services;

import edu.kit.lod16.topic4.util.Prefixes;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class PropertyRankingService {
    private static PropertyRankingService instance;

    Map<String, Map<String, Map<String, Double>>> propertyFrequencies;
    Map<String, Map<String, Double>> classFrequencies;
    Map<Integer, Double> classFrequencyCache;
    List<String> blacklist;

    private PropertyRankingService() {
        this.classFrequencyCache = new HashMap<>();
        this.blacklist = new LinkedList<>();
    }

    public void setBlacklist(List blacklist) {
        this.blacklist = blacklist;
    }

    public void loadPropertyFrequencies(String datasetKey, String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        JSONObject jsonData = new JSONObject(IOUtils.toString(in, Charset.forName("utf-8")));

        if (this.propertyFrequencies == null) this.propertyFrequencies = new HashMap();
        this.propertyFrequencies.put(datasetKey, jsonData.keySet().parallelStream().collect(Collectors.toMap(
                ck -> Prefixes.replaceInverse(ck),
                ck -> {
                    JSONObject properties = jsonData.getJSONObject(ck).getJSONObject("properties");
                    return properties.keySet().parallelStream()
                            .filter(pk -> !this.blacklist.contains(Prefixes.replaceInverse(pk)))
                            .collect(Collectors.toMap(
                                    pk -> Prefixes.replaceInverse(pk),
                                    pk -> properties.getDouble(pk)
                            ));
                }
        )));
    }

    public void loadClassFrequencies(String datasetKey, String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        JSONObject jsonData = new JSONObject(IOUtils.toString(in, Charset.forName("utf-8")));

        if (this.classFrequencies == null) this.classFrequencies = new HashMap();
        this.classFrequencies.put(datasetKey, jsonData.keySet().parallelStream()
                .filter(k -> !this.blacklist.contains(Prefixes.replaceInverse(k)))
                .collect(Collectors.toMap(
                        k -> Prefixes.replaceInverse(k),
                        k -> jsonData.getDouble(k)
                )));
    }

    /**
     * Calculates the inverse class frequency cf for a given property @property within a set of classes with names @classSet
     * @param datasetKey Key of a loaded dataset to use.
     * @param property Name of the property to calc cf for.
     * @param classSet Set of class names present in the chosen dataset to be used for cf computation. Or null to use the pre-calculated numbers of the entire knowledge graph loaded using loadClassFrequencies().
     * @return Class frequency cf(p, C).
     */
    public Double calcClassFrequency(String datasetKey, String property, List<String> classSet) {
        if (classSet == null) return this.classFrequencies.get(datasetKey).get(property);

        int hash = property.hashCode() + classSet.hashCode();
        if (classFrequencyCache.containsKey(hash)) return classFrequencyCache.get(hash);
        long totalClasses = classSet.size();
        long count = classSet.parallelStream().filter(c -> propertyFrequencies.get(datasetKey).get(c).containsKey(property)).count();
        double cf = 1.0 / (Math.log((double) (totalClasses + 1) / (double) count));
        classFrequencyCache.put(hash, cf);
        return cf;
    }

    /**
     * Merges multiple "property to normalized property frequency" maps to a single one, replacing duplicated entries by the one with a maximum value.
     * @param propertyMaps Maps to be merged.
     * @return Merged map with unique keys.
     */
    public Map<String, Double> mergePropertiesByMax(List<Map<String, Double>> propertyMaps) {
        Map<String, Double> uniqueMap = new HashMap();

        propertyMaps.parallelStream().forEach(propertyMap -> {
            uniqueMap.putAll(propertyMap.keySet().parallelStream().collect(Collectors.toMap(
                    k -> k,
                    k -> (uniqueMap.containsKey(k) && uniqueMap.get(k) > propertyMap.get(k)) ? uniqueMap.get(k) : propertyMap.get(k)
            )));
        });

        return uniqueMap;
    }

    /**
     * Calculates weights for all properties in @forClass, taking into account the normalized property frequency of the respective property within that class
     * as well as the properties class frequency within a set of all classes.
     * @param datasetKey Key of a loaded dataset to use.
     * @param forClass The class to calculate property's weights for.
     * @param top The number of top properties to return.
     * @param skip The number of properties to skip at the top of the ordered list.
     * @return Map of properties and their weights.
     */
    public Map<String, Double> getClassTopProperties(String datasetKey, String forClass, int top, int skip, List<String> intersectWith) {
        Map<String, Double> frequencies = this.propertyFrequencies.get(datasetKey).get(forClass);

        return frequencies.keySet().parallelStream()
                .filter(p -> Prefixes.replace(p).split(Pattern.quote(":"))[1].matches("^[a-zA-Z0-9]*$"))
                .filter(p -> intersectWith.contains(p))
                .sorted((k1, k2) -> Double.compare(frequencies.get(k2), frequencies.get(k1)))
                .skip(skip)
                .limit(top)
                .collect(Collectors.toMap(
                        e -> e,
                        e -> frequencies.get(e) * calcClassFrequency(datasetKey, e, null)
                ));
    }

    /**
     * Calculates weights for all properties in all @forClasses, taking into account the normalized property frequency of the respective property within a class
     * as well as the properties class frequency within a set of all classes.
     * @param datasetKey Key of a loaded dataset to use.
     * @param forClasses The classes to calculate property's weights for.
     * @param top The number of top properties to return.
     * @param skip The number of properties to skip at the top of the ordered list.
     * @return Map of properties and their weights.
     */
    public Map<String, Double> getMultiClassTopProperties(String datasetKey, List<String> forClasses, int top, int skip, List<String> intersectWith) {
        return mergePropertiesByMax(forClasses.stream().map(c -> this.getClassTopProperties(datasetKey, c, top, skip, intersectWith)).collect(Collectors.toList()));
    }

    /**
     * Returns a combined, ordered list of the @top most relevant property names, given a list of multiple classes for multiple datasets.
     * @param datasetsClasses Mapping from a dataset key (as loaded into this class) to a list of classes.
     * @param top The number of top properties to return.
     * @param skip The number of properties to skip at the top of the ordered list.
     * @return List of property names of length <= @top.
     */
    public List<String> getCombinedTopProperties(Map<String, List<String>> datasetsClasses, int top, int skip, List<String> intersectWith) {
        Map<String, Double> mergedMap = mergePropertiesByMax(
                datasetsClasses.keySet().parallelStream().map(d -> this.getMultiClassTopProperties(d, datasetsClasses.get(d), top, skip, intersectWith)).collect(Collectors.toList())
        );

        return mergedMap.keySet().parallelStream()
                .sorted((k1, k2) -> Double.compare(mergedMap.get(k2), mergedMap.get(k1)))
                .skip(skip)
                .limit(top)
                .collect(Collectors.toList());
    }

    public List<String> intersectKnownClasses(String datasetKey, List<String> classes) {
        return classes.parallelStream().filter(c -> this.propertyFrequencies.get(datasetKey).containsKey(c)).collect(Collectors.toList());
    }

    public List<String> intersectKnownProperties(String datasetKey, List<String> properties) {
        List<String> p = properties.parallelStream()
                .filter(c -> this.propertyFrequencies.get(datasetKey).keySet().stream().anyMatch(ck -> this.propertyFrequencies.get(datasetKey).get(ck).containsKey(c)))
                .collect(Collectors.toList());
        return p;
    }

    public static PropertyRankingService getInstance() {
        if (instance == null) {
            instance = new PropertyRankingService();
        }
        return instance;
    }
}