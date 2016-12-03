import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { DiffusionService } from './diffusion.service';
import { StackService } from './stack.service';

@Injectable()
export class SessionEstablishmentGuard implements CanActivate {
    constructor(
        private router: Router,
        private diffusionService: DiffusionService,
        private stackService: StackService) {}

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        return this.diffusionService
            .get()
            .then((session) => {
                if (session.isConnected()) {
                    return true;
                }
                else {
                    // If not, they redirect them to the login page
                    this.stackService.push(state.url);
                    this.router.navigate(['/login']);
                    return false;
                }
            })
            .catch((error) => {
                // If not, they redirect them to the login page
                this.stackService.push(state.url);
                this.router.navigate(['/login']);
                return false;
            });
    }
}
