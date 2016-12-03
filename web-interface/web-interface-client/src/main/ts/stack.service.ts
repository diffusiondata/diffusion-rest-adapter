
import { Injectable } from '@angular/core';

@Injectable()
export class StackService {
    private stack: any[] = [];

    push(value: any): void {
        this.stack.push(value);
    }

    pop(): any {
        return this.stack.pop();
    }
}
