import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {Subject} from 'rxjs';
import {ResultService} from './result.service';
import {RankingService} from './ranking.service';

@Injectable()
export class HttpService {

	private baseUrl:string='http://80.241.215.122:8899/api/infobox';
	private rankingUrl:string='http://80.241.215.122:8899/api/ranking';
	public response$:Subject<any> = new Subject<any>();
	public ranking$:Subject<string[]> = new Subject();

	constructor(private http:Http, private resultService:ResultService, private rankingService:RankingService) {

	}

	get(query){
		this.http.get(`${this.rankingUrl}?q=${query.query}&top=50`).subscribe( res => {

				let ranking = this.rankingService.processRanking(res.json());

				this.http.get(`${this.baseUrl}${this.queryHelper(query)}`).subscribe( res =>{

					this.response$.next(this.resultService.process(res.json(), ranking));
				})
			}
		)
	}

	queryHelper(query){
		if(query.amount){
			return `?q=${query.query}&top=${query.amount}`
		} else{
			return `?q=${query.query}`
		}

	}
}
