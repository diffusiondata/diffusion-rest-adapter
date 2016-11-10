import { Component, Input, Inject } from '@angular/core';
import { DiffusionService } from './diffusion.service';
import { Router } from '@angular/router';
import * as diffusion from 'diffusion';

@Component({
  template: `<div class="col-md-8">
    <div class="row">
        <div class="col-md-12">
            <h3>Login</h3>
            <form *ngIf="active" #loginForm="ngForm" (ngSubmit)="doLogin()" class="form-horizontal">
                <div class="form-group">
                    <label for="username" class="col-sm-2 control-label">Username</label>
                    <div class="col-sm-10">
                        <input id="username" required [(ngModel)]="user.username" name="username" #username="ngModel">
                        <div [hidden]="username.valid || username.pristine"
                             class="alert alert-danger">
                          Name is required
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="password" class="col-sm-2 control-label">Password</label>
                    <div class="col-sm-10">
                        <input id="password" required [(ngModel)]="user.password" name="password" #password="ngModel" type="password">
                        <div [hidden]="password.valid || password.pristine"
                             class="alert alert-danger">
                          Host is required
                        </div>
                    </div>
                </div>
                <div>
                    <button class="btn btn-default" [disabled]="!loginForm.form.valid" type="submit">Login</button>
                </div>
            </form>
        </div>
    </div>
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
