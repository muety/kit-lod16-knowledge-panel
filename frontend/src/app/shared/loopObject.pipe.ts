import {Pipe, PipeTransform} from '@angular/core';
@Pipe({
	name:'appLoopObjectPipe'
})

export class LoopObjectPipe implements PipeTransform{

	transform(obj: any): any {

		let arr =[];

		for( let key in obj){
			arr.push({key:key, value:obj[key]})
		}

		return arr;
	}
}
