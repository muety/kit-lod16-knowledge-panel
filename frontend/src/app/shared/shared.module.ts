import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpService} from './http.service';
import {HttpModule} from '@angular/http';
import {ResultService} from './result.service';
import {LoopObjectPipe} from './loopObject.pipe';
import {CamelCaseSplitterPipe} from './camelCaseSplitter.pipe';
import {RankingService} from './ranking.service';

@NgModule({
	imports: [
		CommonModule,
		HttpModule
	],
	declarations: [LoopObjectPipe,CamelCaseSplitterPipe],
	exports:[LoopObjectPipe,CamelCaseSplitterPipe],
	providers:[HttpService,RankingService, ResultService]
})
export class SharedModule {
}
