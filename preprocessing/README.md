# Linked Open Data Seminar 2016 - Knowledge Panel
## Preprocessing
Computes [term frequency](https://en.wikipedia.org/wiki/Tf%E2%80%93idf) as well as [inverse document frequency](https://en.wikipedia.org/wiki/Tf%E2%80%93idf) for a linked open data graph like [DBPedia](http://dbpedia.org) or [Yago](http://yago-knowledge.org), whiles _terms_ are RDF properties within specific RDF classes and _documents_ are RDF graphs.
### Prerequisites
- NodeJS (recommendable is version > 6.x)

### Usage
1. `mkdir ./out`
2. Run `npm install`
3. To compute statistics for DBPedia, comment Yago section in `1_property_frequency.js`. To compute statistics for Yago, comment DBPedia section in that same file.
4. Set the `ENDPOINT` constant to the SPARQL endpoint to run the script against
5. Run `node 1_property_frequency.js` (new files are written to `./out`)
6. Adapt `INFILE` and `OUTFILE` constants in `2_class_frequency.js`
7. Run `node 2_class_frequency.js`

### License
MIT