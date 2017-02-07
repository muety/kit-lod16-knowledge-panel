'use strict';

/* NOTE: Our ranking needs to be in file <entityName>_1.json */
const fs = require('fs'),
    path = require('path');

const IN_DIR = path.normalize('./rawdata'),
    DBP_PATTERN = /^http\:\/\/dbpedia.org/,
    YAGO_PATTERN = /^http\:\/\/yago-knowledge.org/;

const files = fs.readdirSync(IN_DIR);
const maxMap = {};

let totalCountDbp = 0;
let totalCountYago = 0;
let totalCountOther = 0;

files.forEach((f) => {
    let num = parseInt(f.substring(f.lastIndexOf('_') + 1, f.indexOf('.')));
    if (num == 1) {
        let data = JSON.parse(fs.readFileSync(path.normalize(`${IN_DIR}/${f}`), 'utf-8'));

        totalCountDbp += data.filter(e => DBP_PATTERN.test(e)).length;
        totalCountYago += data.filter(e => YAGO_PATTERN.test(e)).length;
        totalCountOther += data.filter(e => !YAGO_PATTERN.test(e) && !DBP_PATTERN.test(e)).length;
    }
});

console.log("DBP ratio: " + (totalCountDbp / (totalCountDbp + totalCountYago + totalCountOther)).toFixed(2));
console.log("Yago ratio: " + (totalCountYago / (totalCountDbp + totalCountYago + totalCountOther)).toFixed(2));
console.log("Other ratio: " + (totalCountOther / (totalCountDbp + totalCountYago + totalCountOther)).toFixed(2));