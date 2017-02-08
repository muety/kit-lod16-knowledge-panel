import {Injectable} from '@angular/core';

@Injectable()

export class RankingService{

	geo='wgs84_pos#';

	public processRanking(arr:string[]):string[]{
		let result:string[]=[];
		arr.forEach( value => {

			let str = this.processValue(value);

			if (str.indexOf(this.geo) > -1){
				str = str.substr(this.geo.length);
			}

			// result.forEach( item => {
			// 	if (item != str){
			//
			// 	}
			// });

			result.push(str)
		});
		return result;
	}

	private processValue(value:string):string{

		return value.substr(value.lastIndexOf('/')+1)
	}


}
