/*
 * Preprocessing step 1:
 * Calculate normalized property frequency for every (property, class) tuple with K = 0.5.
 * Connects to the SPARQL endpoint containing the knowledge graph.
 */

'use strict';

const SparqlClient = require('sparql-client'),
    fs = require('fs');

// Config
// Classes with other prefixes then these will be ignored

/* <----- DBPEDIA ------> */
/* const CLASS_PREFIX_REPLACE = {
    'http://dbpedia.org/resource/': 'dbr:',
    'http://dbpedia.org/ontology/': 'dbo:',
    'http://dbpedia.org/property/': 'dbp:',
    'http://schema.org/': 'so:'
};
const ENDPOINT = 'http://aifb-ls3-vm8.aifb.kit.edu:8901/sparql';*/

/* <----- YAGO ------> */
const CLASS_PREFIX_REPLACE = {
    'http://yago-knowledge.org/resource/': 'yr:'
};
const ENDPOINT = 'http://localhost:8890/sparql';

const OUT_FILE = './out/yago_npf.json';
const client = new SparqlClient(ENDPOINT, {
    qs: { timeout: 600000 }
});
const classes = {};
const queryTemplate1 = `
        prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        prefix yr: <http://yago-knowledge.org/resource/>
        prefix dbr: <http://dbpedia.org/resource/>
        prefix dbo: <http://dbpedia.org/ontology/>
        prefix dbp: <http://dbpedia.org/property/>
        prefix so: <http://schema.org/>

        SELECT distinct ?class (count(distinct ?e) as ?c)
        WHERE {
                ?e rdf:type ?class .
                FILTER(REGEX (?class, "dbpedia.org/ontology", "i") || REGEX (?class, "schema.org", "i") || REGEX (?class, "yago-knowledge.org/resource", "i")) .
        }
        ORDER BY DESC(?c)
        LIMIT 2000
        OFFSET 0
    `;
const queryTemplate2 = `
        prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        prefix yr: <http://yago-knowledge.org/resource/>
        prefix dbr: <http://dbpedia.org/resource/>
        prefix dbo: <http://dbpedia.org/ontology/>
        prefix dbp: <http://dbpedia.org/property/>
        prefix so: <http://schema.org/>

        SELECT distinct ?prop (count(distinct ?e) as ?c)
        WHERE {
                ?e rdf:type ?class .
                ?e ?prop _:bn1
        }
        ORDER BY DESC(?c)
    `;

getClasses().then(() => {
    return new Promise((resolve, reject) => {
        getPropertiesRecursively(Object.keys(classes), resolve, reject);
    });
}).then(() => {
    fs.writeFileSync(OUT_FILE, JSON.stringify(classes, null, 4));
}).then(() => {
    process.exit(0);
});

function getClasses() {
    return new Promise((resolve, reject) => {
        client.query(queryTemplate1)
            .execute(function(error, results) {
                results.results.bindings.forEach(r => {
                    if (containsPrefix(r.class.value) && checkValidity(r.class.value)) {
                        let classKey = r.class.value;
                        for (let k in CLASS_PREFIX_REPLACE) {
                            classKey = classKey.replace(k, CLASS_PREFIX_REPLACE[k]);
                        }
                        classes[classKey] = {
                            count: parseInt(r.c.value),
                            properties: {}
                        };
                    }
                });
                resolve();
            });
    });
}

function getPropertiesRecursively(classIds, resolve, reject) {
    if (!classIds.length) return resolve();
    client.query(queryTemplate2)
        .bind('class', classIds[0])
        .execute(function(error, results) {
            results.results.bindings.forEach(r => {
                let propKey = r.prop.value;
                for (let k in CLASS_PREFIX_REPLACE) {
                    propKey = propKey.replace(k, CLASS_PREFIX_REPLACE[k]);
                }
                classes[classIds[0]].properties[propKey] = (parseInt(r.c.value) / classes[classIds[0]].count).toFixed(6);
            });
            getPropertiesRecursively(classIds.slice(1), resolve, reject);
        });
}

function containsPrefix(str) {
    let contains = false;
    for (let k in CLASS_PREFIX_REPLACE) {
        if (str.indexOf(k) !== -1) contains = true;
    }
    return contains;
}

function checkValidity(str) {
    return str.indexOf('(') === -1 &&
        str.indexOf(')') === -1 &&
        str.indexOf('\\') === -1 &&
        str.indexOf('!') === -1 &&
        str.indexOf('"') === -1 &&
        str.indexOf('\'') === -1 &&
        str.indexOf('§') === -1 &&
        str.indexOf('$') === -1 &&
        str.indexOf('%') === -1 &&
        str.indexOf('&') === -1 &&
        str.indexOf('[') === -1 &&
        str.indexOf(']') === -1 &&
        str.indexOf('{') === -1 &&
        str.indexOf('}') === -1 &&
        str.indexOf('?') === -1 &&
        str.indexOf('`') === -1 &&
        str.indexOf('´') === -1 &&
        str.indexOf('#') === -1 &&
        str.indexOf('*') === -1 &&
        str.indexOf('+') === -1 &&
        str.indexOf(',') === -1 &&
        (str.match(/:/g) || []).length <= 1;
}