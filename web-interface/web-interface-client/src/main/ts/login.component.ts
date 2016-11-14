import { Component, Input, Inject } from '@angular/core';
import { DiffusionService } from './diffusion.service';
import { Router } from '@angular/router';
import * as diffusion from 'diffusion';

@Component({
  template: `<div class="col-md-10">
    <form *ngIf="active" #loginForm="ngForm" (ngSubmit)="doLogin()" class="form-horizontal">
        <div class="form-goup">
            <h3>Login</h3>
        </div>
        <div class="form-group" [class.has-error]="!username.valid && !username.pristine">
            <label for="username" class="col-sm-2 control-label">Username</label>
            <div class="col-sm-4">
                <input id="username" required [(ngModel)]="user.username" name="username" #username="ngModel" class="form-control">
            </div>
            <span class="help-block col-sm-4">The username is required to access the web interface</span>
        </div>
        <div class="form-group" [class.has-error]="!password.valid && !password.pristine">
            <label for="password" class="col-sm-2 control-label">Password</label>
            <div class="col-sm-4">
                <input id="password" required [(ngModel)]="user.password" name="password" #password="ngModel" type="password" class="form-control">
            </div>
            <div class="help-block col-sm-4">The host is required to access the web interface</div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-8">
                <button class="btn btn-default" [disabled]="!loginForm.form.valid" type="submit">Login</button>
            </div>
        </div>
    </form>
</div>`
})
export class LoginComponent {
    active = true;
    user: any = {
        username: null,
        password: null
    };

    constructor(
        private router: Router,
        private diffusionService: DiffusionService,
        @Inject('diffusion.config') private diffusionConfig: diffusion.Options) {
    }

    doLogin() {
        try {
            this.diffusionService.createSession({
                host: this.diffusionConfig.host,
                port: this.diffusionConfig.port,
                secure: this.diffusionConfig.secure,
                principal: this.user.username,
                credentials: this.user.password
            }).then((session) => {
                console.log(session);
                this.router.navigate(['/home']);
            },
            (error) => {
                console.log(error);
            });
        }
        catch (e) {
            console.log(e);
        }
    }
}
