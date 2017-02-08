package edu.kit.lod16.topic4.web;

import edu.kit.lod16.topic4.Main;
import edu.kit.lod16.topic4.services.PropertyRankingService;
import edu.kit.lod16.topic4.services.SparqlQueryService;
import edu.kit.lod16.topic4.util.LinkedDataUtils;
import org.apache.jena.riot.RDFFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.*;

@Path("/api")
public class ApiRootResource {
	private SparqlQueryService yagoSqs;
	private SparqlQueryService dbpSqs;
	private PropertyRankingService prs;

	private static Map<String, String> stringCache = new HashMap<>();
	private static Map<Integer, JSONArray> arrayCache = new HashMap<>();
	private static Map<Integer, JSONObject> objectCache = new HashMap<>();

	public ApiRootResource() {
		this.yagoSqs = SparqlQueryService.getInstance((String) Main.config.get("app.sparql.endpoints.yago"));
		this.dbpSqs = SparqlQueryService.getInstance((String) Main.config.get("app.sparql.endpoints.dbp"));
		this.prs = PropertyRankingService.getInstance();
	}

	@GET @Path("/ranking")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRanking(
			@DefaultValue("") @QueryParam("q") String q,
			@DefaultValue("10") @QueryParam("top") int top) {

		if (q.isEmpty()) return new JSONArray().toString();

    	/* We want the user to be able to type lower case search queries, but still find relevant object. Best way to do so would be
    	do do FILTER(lcase(str(?o)) = "karlsruhe") in the query, but that's extremely expensive. Since "most" of the labels in DBP and Yago
    	are capital case, capitalizing the string is a reasonable heuristic. */
		if (this.arrayCache.containsKey(q.hashCode() + top)) return this.arrayCache.get(q.hashCode() + top).toString();

		List<String> ranking = this.getCombinedPropertyRanking(q, top, 0);
		if (ranking == null) return new JSONArray().toString();

		JSONArray finalRanking = new JSONArray(ranking);
		if (finalRanking.length() > 0) arrayCache.put(q.hashCode() + top, finalRanking);
		return finalRanking.toString();
	}

	@GET @Path("/infobox")
	@Produces(MediaType.APPLICATION_JSON)
	public String getInfobox(
			@DefaultValue("") @QueryParam("q") String q,
			@DefaultValue("10") @QueryParam("top") int top) {

		if (q.isEmpty()) return new JSONObject().toString();
		if (this.objectCache.containsKey(q.hashCode() + top)) return this.objectCache.get(q.hashCode() + top).toString();

		List<String> ranking;
		if (this.arrayCache.containsKey(q.hashCode() + top)) ranking = LinkedDataUtils.jsonArrayToList(this.arrayCache.get(q.hashCode() + top));
		else ranking = this.getCombinedPropertyRanking(q, top, 0);
		if (ranking == null) return new JSONObject().toString();

		JSONObject finalGraph = this.getFinalGraph(ranking, q);
		if (finalGraph.length() > 0) this.objectCache.put(q.hashCode() + top, finalGraph);
		return finalGraph.toString();
	}

	private String resolveDbpEntityUri(String entityQuery) {
		if (this.stringCache.containsKey(entityQuery)) return this.stringCache.get(entityQuery);
		Collection<String> entityCandidates = dbpSqs.selectSubjectByPredicateAndObject("rdfs:label", entityQuery, "en");
		if (entityCandidates.isEmpty()) return null;
		String entityUri = LinkedDataUtils.getMostSimilar(entityCandidates, entityQuery);
		this.stringCache.put(entityQuery, entityUri);
		return entityUri;
	}

	private String resolveYagoEntityUri(String dbpEntityUri) {
		if (this.stringCache.containsKey(dbpEntityUri)) return this.stringCache.get(dbpEntityUri);
		String entityUri = dbpSqs.selectObjectBySubjectAndPredicate(dbpEntityUri, "owl:sameAs").stream()
				.filter(s -> s.contains((String) Main.config.get("app.sparql.resourceIdPatterns.yago")))
				.findFirst()
				.orElse("");
		this.stringCache.put(dbpEntityUri, entityUri);
		return entityUri;
	}

	private List<String> getCombinedPropertyRanking(String entityQuery, int top, int skip) {
		String dbpEntityUri = resolveDbpEntityUri(entityQuery);
		String yagoEntityUri = resolveYagoEntityUri(dbpEntityUri);

		List<String> entityDbpClasses = prs.intersectKnownClasses("dbp", new LinkedList<>(dbpSqs.selectObjectBySubjectAndPredicate(dbpEntityUri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
		List<String> entityYagoClasses = !yagoEntityUri.isEmpty()
				? prs.intersectKnownClasses("yago", new LinkedList<>(yagoSqs.selectObjectBySubjectAndPredicate(yagoEntityUri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))
				: new LinkedList<>();

		List<String> entityProperties = new LinkedList<>();
		if (!dbpEntityUri.isEmpty()) entityProperties.addAll(dbpSqs.selectPredicates(dbpEntityUri));
		if (!yagoEntityUri.isEmpty()) entityProperties.addAll(yagoSqs.selectPredicates(yagoEntityUri));

		Map<String, List<String>> datasetClasses = new HashMap<>();
		datasetClasses.put("dbp", entityDbpClasses);
		datasetClasses.put("yago", entityYagoClasses);

		return prs.getCombinedTopProperties(datasetClasses, top, skip, entityProperties);
	}

	private JSONObject getFinalGraph(List<String> finalPropertyRanking, String originalSearchQuery) {
		StringWriter dbpFinalGraphWriter = new StringWriter();
		StringWriter yagoFinalGraphWriter = new StringWriter();
		dbpSqs.entityConstructSome(this.resolveDbpEntityUri(originalSearchQuery), prs.intersectKnownProperties("dbp", finalPropertyRanking).toArray(new String[finalPropertyRanking.size()]), dbpFinalGraphWriter, RDFFormat.JSONLD);
		yagoSqs.entityConstructSome(this.resolveYagoEntityUri(this.resolveDbpEntityUri(originalSearchQuery)), prs.intersectKnownProperties("yago", finalPropertyRanking).toArray(new String[finalPropertyRanking.size()]), yagoFinalGraphWriter, RDFFormat.JSONLD);

		List<JSONObject> finalGraphs = new LinkedList<>();
		finalGraphs.add(new JSONObject(dbpFinalGraphWriter.toString()));
		finalGraphs.add(new JSONObject(yagoFinalGraphWriter.toString()));

		Map<String, String> customPrefixes = (Map<String, String>) Main.config.get("app.sparql.customPrefixes");
		return LinkedDataUtils.mergeJsonLd(
				finalGraphs,
				new String[] {"dbp", "yago"},
				new LinkedList<>(customPrefixes.keySet()).get(0) + ":" + originalSearchQuery,
				customPrefixes);
	}
}
