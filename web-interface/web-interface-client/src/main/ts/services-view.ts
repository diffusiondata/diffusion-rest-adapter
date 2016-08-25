
import m = require('./model');
import sv = require('./service-view');

class ServicesList {
    private parentElement: HTMLElement;

    constructor(parentElement: HTMLElement) {
        this.parentElement = parentElement;
    }
}

export interface ServicesView {
    onModelChange(newModel: m.Model): void;
}

class ServicesViewImpl implements ServicesView {
    private parentElement: HTMLElement;
    private serviceViews: {[key: string]: sv.ServiceView};

    constructor(parentElement: HTMLElement) {
        this.parentElement = parentElement;
    }

    onModelChange(newModel: m.Model): void {
        var currentServiceViews: {[key: string]: boolean};

        for (let service of newModel.services) {
            let serviceView = this.serviceViews[service.name];
            currentServiceViews[service.name] = true;
            if (serviceView) {
                // TODO: update service
            }
            else {
                let newServiceView = new sv.ServiceView(service.name);
                this.serviceViews[service.name] = newServiceView;
            }
        }

        for (let serviceName in this.serviceViews) {
            let serviceView = currentServiceViews[serviceName];
            if (serviceView) {
                delete this.serviceViews[serviceName];
            }
        }
    }
}

export function create(parentElement: HTMLElement): ServicesView {
    var detailElement = document.createElement('div');
    detailElement.className = 'serviceDetail';
    var serviceListElement = document.createElement('div');
    serviceListElement.className = 'serviceList';
    var newServiceElement = document.createElement('div');
    var newServiceButton = document.createElement('button');
    newServiceButton.textContent = 'Create new service';
    newServiceButton.addEventListener('click', function() {
        // Show new service
        detailElement.innerHTML = `<h4>Create new service</h4>`;
    });

    parentElement.appendChild(serviceListElement);
    serviceListElement.appendChild(newServiceElement);
    newServiceElement.appendChild(newServiceButton);
    parentElement.appendChild(detailElement);

    return new ServicesViewImpl(serviceListElement);
}
