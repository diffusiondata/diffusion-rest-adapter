import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'service-modal',
    template: `<div class="col-md-8">
    <div class="row">
        <div class="col-md-12">
            <router-outlet></router-outlet>
        </div>
    </div>
</div>`
})
export class ServiceModalComponent { }
