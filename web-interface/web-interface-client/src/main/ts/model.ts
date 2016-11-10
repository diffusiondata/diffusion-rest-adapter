
export interface Endpoint {
    name: string;
    url: string;
    topicPath: string;
    produces: string;
}

export interface BasicAuthentication {
    username: string;
    password: string;
}

export interface Security {
    basic: BasicAuthentication
}

export interface Service {
    name: string;
    host: string;
    port: number;
    secure: boolean;
    endpoints: Endpoint[];
    pollPeriod: number;
    topicPathRoot: string;
    security?: Security;
}

export interface Model {
    services: Service[];
}
