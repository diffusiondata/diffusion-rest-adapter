import { Injectable } from '@angular/core';
import * as diffusion from 'diffusion';
import {Session, SessionOptions} from "diffusion";

@Injectable()
export class DiffusionService {
    private session: Promise<any>;
    private diffusionConfig: SessionOptions;

    constructor() {
        this.session = Promise
            .reject(new Error('Session not yet created'))
            .then(() => {}, (e) => { return e; });
    }

    private immediateAttemptToConnect(): Promise<Session> {
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

    private deferAttemptToConnect(): Promise<Session> {
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

    private deferCreateSession(): Promise<Session> {
        return this.deferAttemptToConnect().catch(() => {
            return this.deferCreateSession();
        });
    }

    private internalCreateSession(): Promise<Session> {
        return this.immediateAttemptToConnect().catch(() => {
            return this.deferCreateSession();
        });
    }

    private checkSession(promise: Promise<Session>) {
        return promise.then((session) => {
            if (session.isConnected()) {
                return session;
            }
            else {
                return this.internalCreateSession();
            }
        });
    }

    createSession(options: SessionOptions): Promise<Session> {
        this.diffusionConfig = options;
        this.session = this.immediateAttemptToConnect();

        return this.session;
    }

    get(): Promise<Session> {
        if (!this.session) {
            this.session = this.checkSession(this.internalCreateSession());
        }

        return this.session;
    }
}
