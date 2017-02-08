import {Injectable} from '@angular/core';
import * as _ from 'lodash';
declare var validator:any;

@Injectable()
export class ResultService {

	prepostfix:string[] = ['_yago','_dbp','dbp:','is','dbo:','dbr:','yago:','area:','kit:'];

	constructor(){

	}
	process(res,ranking) {
		let jsonLD = res;
		let result = {};

		for(let key in jsonLD){
			let parsedKey = this.processString(key);
			let value = {
				isUrl:false
			};

			if(typeof jsonLD[key] === 'string'){
				value['value'] = this.processString(jsonLD[key]);
				value.isUrl = this.checkUrl(value['value']);
			} else {
				value['value'] = jsonLD[key];
			}

			result[parsedKey] = value;
		}

		return this.processRanking(result,ranking)
	}

	processRanking(res,ranking:string[]){

		let result = [];

		for (let i = 0; i < ranking.length; i++){
			for (let key in res){
				if (key === ranking[i]){
					let type = typeof res[ranking[i]].value;
					let entry = {
						key:ranking[i],
						rank:i+1,
						value:res[ranking[i]].value,
						isUrl:res[ranking[i]].isUrl,
						valueType:type
					};
					result.push(entry);
					delete res[key];
				}
			}
		}

		for( let key in res){
			let type = typeof res[key].value;
			let entry = {
						key:key,
						rank:0,
						value:res[key].value,
						isUrl:res[key].isUrl,
						valueType:type
					};
			result.push(entry)
		}
		console.log(result);
		return result

	}

	processString(value){
		return this.prefixParser(this.postfixParser(value))
	}


	prefixParser(str):string {

		let value:string = str;

		this.prepostfix.forEach( prefix => {
			if(_.startsWith(value,prefix)){
				value = value.substr(prefix.length);
			}
		});
		return value
	}

	postfixParser(str:string):string {

		let value:string = str;

		this.prepostfix.forEach( postfix => {
			if(_.endsWith(value,postfix)){
				value =  _.trimEnd(value, postfix);
			}
		});

		return value
	}

	checkUrl(str:string):boolean{
		let isUrl = false;

		if(this.validURL(str)){isUrl = true}

		return isUrl
	}

	validURL(str) {
		var pattern = new RegExp('https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)');
		var urlPattern = new RegExp("(http|ftp|https)://[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:/~+#-]*[\w@?^=%&amp;/~+#-])?");

		if(!validator.isURL(str)) {
			return false;
		} else {
			return true;
		}
	}



}
