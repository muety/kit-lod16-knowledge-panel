'use strict';

/* NOTE: Our ranking needs to be in file <entityName>_1.json */
const fs = require('fs'),
    path = require('path');

const IN_DIR = path.normalize('./rawdata'),
    OUT_DIR = path.normalize('./data'),
    FILTER_PATTERN = /^http\:\/\/dbpedia.org/,
    MAX = 25;

const files = fs.readdirSync(IN_DIR);
const maxMap = {};

files.forEach((f) => {
    let data = JSON.parse(fs.readFileSync(path.normalize(`${IN_DIR}/${f}`), 'utf-8'));
    let prefix = f.substring(0, f.indexOf('_'));
    let num = parseInt(f.substring(f.lastIndexOf('_') + 1, f.indexOf('.')));

    if (!Array.isArray(data)) data = Object.keys(data);
    data = data.filter(e => FILTER_PATTERN.test(e));

    if (num === 1) maxMap[prefix] = data.length;
    data = data.slice(0, Math.min(MAX, maxMap[prefix]));
    fs.writeFileSync(path.normalize(`${OUT_DIR}/${f}`), JSON.stringify(data, null, 4));
});