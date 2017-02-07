# Linked Open Data Seminar 2016 - Knowledge Panel
## Evaluation
### Prerequsites
- NodeJS (recommendable is version > 6.x)

### Usage
1. Get ordered JSON array of property URIs or JSON object with property URIs as keys for both ranking tio be compared against each other
2. Save JSON data for the ranking to be evaluated to `rawdata/SomeEntityName_1.json`
3. Save JSON data for the ranking to be used for comparison to `rawdata/SomeEntityName_2.json`
4. Run `node preprocess.js` (two new files are written to `data` directory)
5. Edit `evaluate_rbo.js` to adapt `p` parameter as well as the respective JSON filenames from the `data` directory
6. Run `node evaluate_rbo.js`

### License
MIT