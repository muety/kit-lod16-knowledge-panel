import {Component} from '@angular/core';
import {RankingService} from './shared/ranking.service';
import {ResultService} from './shared/result.service';
import {HttpService} from './shared/http.service';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss']
})
export class AppComponent {
	constructor(
		private httpService:HttpService,
		private rankingService:RankingService,
	    private resultService:ResultService

	){}
}
