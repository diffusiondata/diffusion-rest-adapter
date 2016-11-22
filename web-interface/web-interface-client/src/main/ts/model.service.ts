import { Injectable, Inject } from '@angular/core';
import { Model, Service, Endpoint } from './model';
import { DiffusionService } from './diffusion.service';
import { RequestContext } from './request-context';

@Injectable()
export class ModelService {
    private session;
    private context: RequestContext;
    model: Model = {
        services: []
    };

    constructor(private diffusionService: DiffusionService) {
    }

    private init(): Promise<any> {
        return this.diffusionService.get().then((session) => {
            if (this.session != session) {
                this.session = session;
                this.context = new RequestContext(session, 'adapter/rest/model/store');
            }
            return session;
        });
    }

    getModel(): Promise<Model> {
        return this.init()
            .then(() => this.context.request({
                type: 'list-services'
            }))
            .then((response) => {
                this.model.services = response
                return this.model;
            });
    }

    getService(name: string): Promise<Service> {
        return this.init()
            .then(() => this.getModel())
            .then((model) => Promise.resolve(model.services.find((element) => element.name === name)));
    }

    createService(service: Service): Promise<void> {
        return this.init()
            .then(() => this.context.request({
                type: 'create-service',
                service: service
            }))
            .then(() => {
                var serviceIdx = this
                    .model
                    .services
                    .findIndex((element) => element.name === service.name);

                if (serviceIdx === -1) {
                    var serviceCopy = JSON.parse(JSON.stringify(service));
                    if (serviceCopy.security && serviceCopy.security.basic) {
                        serviceCopy.security.basic.password = null;
                    }
                    this.model.services.push(serviceCopy);
                }
            });
    }

    createEndpoint(serviceName: string, endpoint: Endpoint): Promise<void> {
        return this.init()
            .then(() => this.context.request({
                type: 'create-endpoint',
                serviceName: serviceName,
                endpoint: endpoint
            }))
            .then(() => {
                var service = this
                    .model
                    .services
                    .find((element) => element.name === serviceName);

                var endpointIdx = service
                    .endpoints
                    .findIndex((element) => element.name === endpoint.name);

                if (endpointIdx === -1) {
                    service
                        .endpoints
                        .push(JSON.parse(JSON.stringify(endpoint)));
                }
            });
    }

    deleteService(serviceName: string): Promise<void> {
        return this.init()
            .then(() => this.context.request({
                type: 'delete-service',
                serviceName: serviceName
            }))
            .then(() => {
                let serviceIndex = this
                    .model
                    .services
                    .findIndex((element) => element.name === serviceName);

                if (serviceIndex !== -1) {
                    this.model.services.splice(serviceIndex, 1);
                }
            });
    }

    deleteEndpoint(serviceName: string, endpointName: string): Promise<void> {
        return this.init()
            .then(() => this.context.request({
                type: 'delete-endpoint',
                serviceName: serviceName,
                endpointName: endpointName
            }))
            .then(() => {
                let service = this
                    .model
                    .services
                    .find((element) => element.name === serviceName);

                if (!service) {
                    return;
                }

                let endpointIndex = service
                    .endpoints
                    .findIndex((element) => element.name === endpointName);

                if (endpointIndex !== -1) {
                    service.endpoints.splice(endpointIndex, 1);
                }
            });
    }
}
