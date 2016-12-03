import { Component, Input, Inject } from '@angular/core';
import { DiffusionService } from './diffusion.service';
import { Router } from '@angular/router';
import { StackService } from './stack.service';
import * as diffusion from 'diffusion';

@Component({
  template: `<div class="col-md-10">
    <div class="panel panel-default">
        <div class="panel-heading page-header">
            <h4>Login</h4>
        </div>
        <div class="panel-body">
            <form *ngIf="active" #loginForm="ngForm" (ngSubmit)="doLogin()" class="form-horizontal">
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
                        <button class="btn btn-primary" [disabled]="!loginForm.form.valid" type="submit">Login</button>
                    </div>
                </div>
                <div *ngIf="failed" class="form-group">
                    <p class="bg-danger errorMessage col-sm-offset-2 col-sm-8">{{failureMessage}}</p>
                </div>
            </form>
        </div>
    </div>
</div>`
})
export class LoginComponent {
    failed = false;
    failureMessage = '';
    active = true;
    user: any = {
        username: null,
        password: null
    };

    constructor(
        private router: Router,
        private diffusionService: DiffusionService,
        @Inject('diffusion.config') private diffusionConfig: diffusion.Options,
        private stackService: StackService) {
    }

    doLogin() {
        this.failed = false;
        let returnUrl = this.stackService.pop();
        try {
            this.diffusionService.createSession({
                host: this.diffusionConfig.host,
                port: this.diffusionConfig.port,
                secure: this.diffusionConfig.secure,
                principal: this.user.username,
                credentials: this.user.password
            }).then((session) => {
                if (returnUrl) {
                    this.router.navigate([returnUrl]);
                }
                else {
                    this.router.navigate(['/home']);
                }
            },
            (error) => {
                this.failed = true;
                if (error.message) {
                    this.failureMessage = error.message;
                }
                else {
                    this.failureMessage = error.toString();
                }
            });
        }
        catch (e) {
            console.log(e);
        }
    }
}
