import {importProvidersFrom} from '@angular/core';


import {AppComponent} from './app/app.component';
import {withInterceptorsFromDi, provideHttpClient} from '@angular/common/http';
import {AppRoutingModule} from './app/app-routing.module';
import {FormsModule} from '@angular/forms';
import {BrowserModule, bootstrapApplication} from '@angular/platform-browser';

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(BrowserModule, FormsModule, AppRoutingModule),
    provideHttpClient(withInterceptorsFromDi())
  ]
})
  .catch(err => console.error(err));
