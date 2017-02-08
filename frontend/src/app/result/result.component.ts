import {Component, OnInit} from '@angular/core';
import {HttpService} from '../shared/http.service';
import {ResultService} from '../shared/result.service';
import * as _ from 'lodash';

@Component({
	selector: 'app-result',
	templateUrl: './result.component.html',
	styleUrls: ['./result.component.scss']
})
export class ResultComponent implements OnInit {

	private result=null;
	private image=[];
	private resultAbstracts=[];
	private abstractText ='';
	private abstractTextFull = '';
	private id='';

	constructor(private httpService: HttpService) {
	}

	ngOnInit() {
		this.httpService.response$.subscribe( res => {
			this.image=[];
			this.resultAbstracts =[];
			this.abstractText='';
			this.abstractTextFull='';
			this.id='';
			this.result = res;
			this.getId();
			this.displayImg();
			this.displayAbstract();
		})
	}

	displayImg(){
		if(this.result){
			this.result.forEach( item =>{
				if(typeof item['value'] === 'string'){

					let str = item['value'];
					if(_.endsWith(str,'.png')){
						this.image.push(str)
					}else if (_.endsWith(str,'.jpg')){
						this.image.push(str)
					}else if (_.endsWith(str,'.jpeg')){
						this.image.push(str)
					}else if (_.endsWith(str,'.svg')){
						this.image.push(str)
					}else if (_.endsWith(str,'.gif')){
						this.image.push(str)
					}
				}
			});
		}
	}

	displayAbstract(){

		this.result.forEach( item => {
			if (item.key === 'abstract'){
				this.resultAbstracts = item.value;
			}
		});

		// this.resultAbstracts = this.result['abstract'].value;
		let abstractText;
		this.resultAbstracts.forEach( country =>{
			if(country['@language'] === 'en'){
				abstractText = country['@value']
			} else if (country['@language'] === 'de'){
				abstractText = country['@value']
			}
		});

		this.abstractTextFull = abstractText;
		this.abstractText = abstractText.substr(0, 200)+'...';
	}

	getFlagCss(lang){
		let convert = [{lang:'en',flag:'us'},{lang:'zh',flag:'cn'},{lang:'ja',flag:'jp'}];
		let icon ='flag-icon-'+lang;
		convert.forEach( country =>{
			if (lang === country.lang){
				icon = 'flag-icon-'+country.flag
			}
		});
		return icon;
	}

	changeLanguage(country){
		this.abstractText = country['@value'].substr(0,200)+'...';
		this.abstractTextFull =country['@value'];
	}

	getId(){
		this.result.forEach( item => {
			if (item.key === '@id'){
				this.id = item.value
			}
		})
	}

	getType(item){
		console.log(typeof item.value === 'object')
		return typeof item.value === 'object'
	}


}
