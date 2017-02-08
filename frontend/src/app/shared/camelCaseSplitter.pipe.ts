import {Pipe, PipeTransform} from '@angular/core';
import * as _ from 'lodash';
@Pipe({
	name:'appCamelCaseSplitter'
})

export class CamelCaseSplitterPipe implements PipeTransform{

	transform(value: string): string {
		let result = value.replace(/([A-Z])/g, ' $1');
		return _.capitalize(result);
	}
}
