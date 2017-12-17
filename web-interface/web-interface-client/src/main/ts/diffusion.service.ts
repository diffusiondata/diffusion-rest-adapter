import { Injectable } from '@angular/core';
import * as diffusion from 'diffusion';

@Injectable()
export class DiffusionService {
    private session: Promise<any>;
    private diffusionConfig: diffusion.SessionOptions;

    constructor() {
        this.session = Promise
            .reject(new Error('Session not yet created'))
            .then(() => {}, (e) => { return e; });
    }

    private immediateAttemptToConnect(): Promise<diffusion.Session> {
        return new Promise((resolve, reject) => {
            diffusion
                .connect(this.diffusionConfig)
                .then((session) => {
                    console.log('Connected');
                    resolve(session);
                },
                (error) => {
                    console.log(error);
                    reject(error);
                });
        });
    }

    private deferAttemptToConnect(): Promise<diffusion.Session> {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                diffusion
                    .connect(this.diffusionConfig)
                    .then((session) => {
                        console.log('Connected');
                        resolve(session);
                    },
                    (error) => {
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

    private internalCreateSession(): Promise<diffusion.Session> {
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
                return this.internalCreateSession();
            }
        });
    }

    createSession(options: diffusion.SessionOptions): Promise<diffusion.Session> {
        this.diffusionConfig = options;
        this.session = this.immediateAttemptToConnect();

        return this.session;
    }

    get(): Promise<diffusion.Session> {
        if (!this.session) {
            this.session = this.checkSession(this.internalCreateSession());
        }

        return this.session;
    }
}
