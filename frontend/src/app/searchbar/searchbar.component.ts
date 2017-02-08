import {Component, OnInit} from '@angular/core';
import {HttpService} from '../shared/http.service';

declare var jQuery:any;

@Component({
	selector: 'app-searchbar',
	templateUrl: './searchbar.component.html',
	styleUrls: ['./searchbar.component.scss']
})
export class SearchbarComponent implements OnInit {

	private query:string='Karlsruhe';
	private properties:number[]=[5,10,20,30,50];
	private seletectedProperties:number=50;


	constructor(private httpService:HttpService) {
	}

	ngOnInit() {
		jQuery(".dropdown-button").dropdown();
	}

	setProperty(amount:number){
		this.seletectedProperties = amount
	}

	submit(event) {
		if(event.keyCode == 13) {
			this.httpService.get({query:this.query,amount:this.seletectedProperties});
			// rest of your code
		}
	}

	clickSearch(){
		if(this.query && this.query.length !=0){
			this.httpService.get({query:this.query,amount:this.seletectedProperties})
		}
	}

}
