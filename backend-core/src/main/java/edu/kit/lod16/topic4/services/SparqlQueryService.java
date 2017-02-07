package edu.kit.lod16.topic4.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.lod16.topic4.util.InsecureSSLSocketFactory;
import edu.kit.lod16.topic4.util.LinkedDataUtils;
import edu.kit.lod16.topic4.util.Prefixes;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.security.KeyStore;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Setter
public class SparqlQueryService {
    private static Map<String, SparqlQueryService> instances = new HashMap();
    private String endpointUrl;
    private HttpClient httpClient;

    private SparqlQueryService(String endpointUrl) {
        this.endpointUrl = endpointUrl;
	    this.httpClient = this.getInsecureHttpClient();
    }

	private static HttpClient getInsecureHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			InsecureSSLSocketFactory sf = new InsecureSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

    /*
    Returns the result of a SPARQL SELECT query as a map, where key is a predicate and value a list of objects to that predicate.
     */
    public Map<String, Collection<String>> selectPredicateAndObjectBySubject(String entityUrl) {
        String queryString = generatePrefixQueryString(Prefixes.getReplaceMap())
                + "SELECT ?p ?o \n"
                + "WHERE { " + Prefixes.replace(entityUrl) + " ?p ?o . } \n ";

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(getEndpointUrl(), QueryFactory.create(queryString), this.httpClient)) {
            ResultSet results = qexec.execSelect() ;
            return twoVariableSolutionToMap(results, null, null);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
        return null;
    }

    public Collection<String> selectObjectBySubjectAndPredicate(String entityUrl, String predicate) {
        if (!predicate.contains(":")) predicate = "\"" + predicate + "\"";

        String queryString = generatePrefixQueryString(Prefixes.getReplaceMap())
                + "SELECT ?o \n"
                + "WHERE { " + Prefixes.replace(entityUrl) + " " + Prefixes.replace(predicate) + " ?o } \n ";

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(getEndpointUrl(), QueryFactory.create(queryString), this.httpClient)) {
            ResultSet results = qexec.execSelect() ;
            return oneVariableSolutionToList(results, null);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
        return null;
    }

	public Collection<String> selectPredicates(String entityUrl) {
		String queryString = generatePrefixQueryString(Prefixes.getReplaceMap())
				+ "SELECT ?p \n"
				+ "WHERE { " + Prefixes.replace(entityUrl) + " ?p ?o . }";

		try (QueryExecution qexec = QueryExecutionFactory.sparqlService(getEndpointUrl(), QueryFactory.create(queryString), this.httpClient)) {
			ResultSet results = qexec.execSelect() ;
			return LinkedDataUtils.uniqueCollection(oneVariableSolutionToList(results, "p")); // query executor has a problem with DISTICNT :/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    public Collection<String> selectSubjectByPredicateAndObject(String predicate, String object, String literalLanguageKey) {
        if (!predicate.contains(":")) predicate = "\"" + predicate + "\"";
        if (!object.contains(":")) object = "\"" + object + "\"";
        if (literalLanguageKey != null) object = object + "@" + literalLanguageKey;

        String queryString = generatePrefixQueryString(Prefixes.getReplaceMap())
                + "SELECT ?s \n"
                + "WHERE { ?s " + Prefixes.replace(predicate) + " " + Prefixes.replace(object) + "  } \n ";

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(getEndpointUrl(), QueryFactory.create(queryString), this.httpClient)) {
            ResultSet results = qexec.execSelect() ;
            return oneVariableSolutionToList(results, "s");
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
        return null;
    }

    /*
    Constructs a graph with one or more triples for each @predicates, where subject is always @entityUrl and object is queried
    */
    public void entityConstructSome(String entityUrl, String[] predicates, Writer outStream, RDFFormat format) {
        String queryString = generatePrefixQueryString(Prefixes.getReplaceMap())
                + "CONSTRUCT { $CONSTRUCT_TPL$ } \n"
                + "WHERE { $WHERE_TPL$ }";

/*        String whereTpl = new ArrayList<>(Arrays.asList(predicates)).stream()
		        .filter(s -> s != null)
                .map(s -> "OPTIONAL { " + Prefixes.replace(s) + " rdfs:label ?l" + Math.abs(s.hashCode()) + " } . " + Prefixes.replace(entityUrl) + " " + Prefixes.replace(s) + " ?o" + Math.abs(s.hashCode()) + " . ")
                .collect(Collectors.joining());

	    String constructTpl = new ArrayList<>(Arrays.asList(predicates)).stream()
			    .filter(s -> s != null)
			    .map(s -> Prefixes.replace(entityUrl) + " " + Prefixes.replace(s) + " ?o" + Math.abs(s.hashCode()) + " . " + Prefixes.replace(s) + " rdfs:label ?l" + Math.abs(s.hashCode()) + " . ")
			    .collect(Collectors.joining());*/

		String tpl = new ArrayList<>(Arrays.asList(predicates)).stream()
			    .filter(s -> s != null)
			    .map(s -> Prefixes.replace(entityUrl) + " " + Prefixes.replace(s) + " ?o" + Math.abs(s.hashCode()) + " . ")
			    .collect(Collectors.joining());

        queryString = queryString.replace("$CONSTRUCT_TPL$", tpl).replace("$WHERE_TPL$", tpl);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(getEndpointUrl(), QueryFactory.create(queryString), this.httpClient)) {
            Model result = qexec.execConstruct();
            RDFDataMgr.write(outStream, result, format);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }

    /* Converts results of a SPARQL SELECT query into a map with object bindings for each predicate */
    public static Map<String, Collection<String>> twoVariableSolutionToMap(final ResultSet results, String predicateKey, String objectKey) {
        Map<String, Collection<String>> predicateObjectMap = new HashMap();
        Map<String, String> uri2Prefix = Prefixes.getReplaceMap();

        Iterable<QuerySolution> iterable = () -> results;
        StreamSupport.stream(iterable.spliterator(), false).forEach((QuerySolution qs) -> {
            String p = qs.get(predicateKey != null ? predicateKey : "p").toString();
            String o = qs.get(objectKey != null ? objectKey : "o").toString();

            if (!predicateObjectMap.containsKey(p)) {
                predicateObjectMap.put(p, new ArrayList<>());
            }

            predicateObjectMap.get(p).add(o);
        });

        return predicateObjectMap;
    }

    /* Converts results of a SPARQL SELECT query into a list of object bindings */
    public static Collection<String> oneVariableSolutionToList(final ResultSet results, String objectKey) {
        Collection<String> objectList = new ArrayList<>();
        Map<String, String> uri2Prefix = Prefixes.getReplaceMap();

        Iterable<QuerySolution> iterable = () -> results;
        StreamSupport.stream(iterable.spliterator(), false).forEach((QuerySolution qs) -> {
            String o = qs.get(objectKey != null ? objectKey : "o").toString();
            objectList.add(o);
        });

        return objectList;
    }

    public static String generatePrefixQueryString(Map<String, String> prefixes) {
        return prefixes.entrySet().stream()
                .map(e -> "PREFIX " + e.getValue() + " <" + e.getKey() + "> \n")
                .collect(Collectors.joining());
    }

    /* Requires full URIs */
    @Deprecated
    public String entityToJsonLdString(Map<String, Collection<String>> entity, Map<String, String> prefixes) {
        String jsonStr = "";

        try {
            Object entityJson = JsonUtils.fromString(new JSONObject(entity).toString());
            Map context = prefixes == null ? Prefixes.getInverseReplaceMap() : prefixes;
            Object compact = JsonLdProcessor.compact(entityJson, context, new JsonLdOptions());
            jsonStr = JsonUtils.toPrettyString(compact);
        } catch (JsonLdError jsonLdError) {
            jsonLdError.printStackTrace();
            return "";
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonStr;
    }

    public static SparqlQueryService getInstance(String endpointUrl) {
        if (!instances.containsKey(endpointUrl)) {
            instances.put(endpointUrl, new SparqlQueryService(endpointUrl));
        }
        return instances.get(endpointUrl);
    }
}
