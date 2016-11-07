import { Injectable } from '@angular/core';
import * as diffusion from 'diffusion';
import { DiffusionConfigService } from './diffusion-config.service';

@Injectable()
export class DiffusionService {
    private session: Promise<any>;

    constructor(private configService: DiffusionConfigService) {
    }

    private immediateAttemptToConnect(): Promise<diffusion.Session> {
        return this.configService
            .get()
            .then((diffusionConfig) => {
                return diffusion.connect(diffusionConfig);
            })
            .then((session) => {
                console.log('Connected');
                return session;
            }, (error) => {
                console.log(error);
                return error;
            });
    }

    private deferAttemptToConnect(): Promise<diffusion.Session> {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                this.configService
                    .get()
                    .then((diffusionConfig) => {
                        return diffusion.connect(diffusionConfig);
                    })
                    .then((session) => {
                        console.log('Connected');
                        resolve(session);
                    }, (error) => {
                        console.log(error);
                        reject(error);
                    });
            }, 5000);
        });
    }

    private deferCreateSession(): Promise<diffusion.Session> {
        return this.deferAttemptToConnect().catch(() => {
            return this.deferCreateSession();
        });
    }

    private createSession(): Promise<diffusion.Session> {
        return this.immediateAttemptToConnect().catch(() => {
            return this.deferCreateSession();
        });
    }

    private checkSession(promise: Promise<diffusion.Session>) {
        return promise.then((session) => {
            if (session.isConnected()) {
                return session;
            }
            else {
                return this.createSession();
            }
        });
    }

    get(): Promise<diffusion.Session> {
        if (!this.session) {
            this.session = this.checkSession(this.createSession());
        }

        return this.session;
    }
}
