'use strict';

const RBO = require('./rbo'),
    rbo = new RBO(0.98),
    fs = require('fs');

const INPUT_FILE_1 = './data/Mount_Everest_1.json',
    INPUT_FILE_2 = './data/Mount_Everest_2.json';

let ranking1 = JSON.parse(fs.readFileSync(INPUT_FILE_1), 'utf-8'),
    ranking2 = JSON.parse(fs.readFileSync(INPUT_FILE_2), 'utf-8');

let rboValue = rbo.calculate(ranking1, ranking2);

console.log(rboValue.toFixed(2));