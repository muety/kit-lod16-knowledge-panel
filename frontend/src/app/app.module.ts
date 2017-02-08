import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {NavbarComponent} from './navbar/navbar.component';
import {SearchbarComponent} from './searchbar/searchbar.component';
import {ResultComponent} from './result/result.component';
import {SharedModule} from './shared/shared.module';

@NgModule({
	declarations: [
		AppComponent,
		NavbarComponent,
		SearchbarComponent,
		ResultComponent,

	],
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		SharedModule,
		ReactiveFormsModule
	],
	providers: [],
	bootstrap: [AppComponent]
})
export class AppModule {
}
