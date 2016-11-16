
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'asEndpointType'})
export class EndpointTypePipe implements PipeTransform {
  transform(value: string, args: string[]): any {
    if (value === 'binary') {
        return 'Binary';
    }
    else if (value === 'json') {
        return 'JSON';
    }
    else if (value === 'string') {
        return 'String';
    }
    else {
        return 'Unknown type';
    }
  }
}
