/*
 * Preprocessing step 2:
 * Calculate inverse class frequency for every property.
 * Takes output of step 1 as input.
 */

'use strict';

const fs = require('fs');
const INFILE = './out/dbp_npf.json';
const OUTFILE = './out/dbp_cf.json';

let inverseClassFrequencies = {};
let data = JSON.parse(fs.readFileSync(INFILE, 'utf-8'));
let totalClasses = Object.keys(data).length;

for (let c in data) {
    for (let p in data[c].properties) {
        if (inverseClassFrequencies.hasOwnProperty(p)) continue;
        inverseClassFrequencies[p] = (1 / Math.log((totalClasses + 1) / countClasses(p))).toFixed(6);
    }
}

function countClasses(property) {
    let counter = 0;
    for (let c in data) {
        if (data[c].properties.hasOwnProperty(property)) counter++;
    }
    return counter;
}

fs.writeFileSync(OUTFILE, JSON.stringify(inverseClassFrequencies, null, 4));