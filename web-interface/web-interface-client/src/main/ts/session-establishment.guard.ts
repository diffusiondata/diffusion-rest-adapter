import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { DiffusionService } from './diffusion.service';

@Injectable()
export class SessionEstablishmentGuard implements CanActivate {
    constructor(private router: Router, private diffusionService: DiffusionService) {}

    canActivate() {
        // If not, they redirect them to the login page
        this.router.navigate(['/login']);
        return false;
    }
}
