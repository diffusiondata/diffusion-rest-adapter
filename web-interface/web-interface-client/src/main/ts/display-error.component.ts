import { Component, OnInit, OnDestroy } from '@angular/core';

import { ErrorService } from './error.service';

@Component({
    selector: 'display-error',
    template: `<div *ngIf="hasError" class="alert alert-danger">
<span>{{errorMessage}}</span>
</div>`
})
export class DisplayErrorComponent implements OnInit, OnDestroy {
    hasError: boolean;
    errorMessage: String;

    constructor(private errorService: ErrorService) {
        this.hasError = false;
    }

    ngOnInit(): void {
        this.errorService.addErrorListener(this.setError.bind(this));
    }

    ngOnDestroy(): void {
        this.errorService.removeErrorListener(this.setError.bind(this));
    }

    setError(errorMessage: String) {
        this.hasError = true;
        this.errorMessage = errorMessage;
    }
}
