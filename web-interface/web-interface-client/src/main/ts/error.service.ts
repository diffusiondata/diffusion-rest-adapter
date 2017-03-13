import { Injectable } from '@angular/core';

@Injectable()
export class ErrorService {
    private listeners = [];

    addErrorListener(errorListener: (errorMessage: String) => any) {
        this.listeners.push(errorListener);
    }

    removeErrorListener(errorListener: (errorMessage: String) => any) {
        this.listeners = this.listeners.filter(element => element !== errorListener);
    }

    onError(error: String) {
        this.listeners.forEach(listener => listener(error));
    }
}
