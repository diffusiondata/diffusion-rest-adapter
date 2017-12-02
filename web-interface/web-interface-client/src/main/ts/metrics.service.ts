import {Injectable} from '@angular/core';
import {DiffusionService} from './diffusion.service';
import * as diffusion from 'diffusion';

const doubleDataType = diffusion.datatypes.double();
const longDataType = diffusion.datatypes.int64();

@Injectable()
export class MetricsService {
    constructor(private diffusionService: DiffusionService) {
    }

    public createEmptyView(): MetricsView {
        return {
            pollMetrics: MetricsService.createCommonMetrics(),
            publicationMetrics: MetricsService.createCommonMetrics(),
            topicCreationMetrics: MetricsService.createCommonMetrics(),
            close: () => {}
        }
    }

    private static createCommonMetrics(): CommonMetrics {
        return {
            requests: 0,
            successes: 0,
            failures: 0,
            requestThroughput: 0,
            failureThroughput: 0,
            maximumSuccessfulRequestTime: 0,
            minimumSuccessfulRequestTime: 0,
            successfulRequestTimeNinetiethPercentile: 0
        };
    }

    private registerCommonMetrics(session, topicRoot, metrics) {
        let streams = [
            session
                .stream(topicRoot + '/requests')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.requests = newValue;
                }),
            session
                .stream(topicRoot + '/successes')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.successes = newValue;
                }),
            session
                .stream(topicRoot + '/failures')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.failures = newValue;
                }),
            session
                .stream(topicRoot + '/requestThroughput')
                .asType(doubleDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.requestThroughput = newValue;
                }),
            session
                .stream(topicRoot + '/failureThroughput')
                .asType(doubleDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.failureThroughput = newValue;
                }),
            session
                .stream(topicRoot + '/maximumSuccessfulRequestTime')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.maximumSuccessfulRequestTime = newValue;
                }),
            session
                .stream(topicRoot + '/minimumSuccessfulRequestTime')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.minimumSuccessfulRequestTime = newValue;
                }),
            session
                .stream(topicRoot + '/successfulRequestTimeNinetiethPercentile')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.successfulRequestTimeNinetiethPercentile = newValue;
                })];
        return {
            close() {
                streams.forEach(stream => stream.close());
            }
        };
    }

    public getMetricsView(): Promise<MetricsView> {
        return this.diffusionService.get().then((session) => {
            let pollMetrics = MetricsService.createCommonMetrics();
            let publicationMetrics = MetricsService.createCommonMetrics();
            let topicCreationMetrics = MetricsService.createCommonMetrics();

            let streams = [
                this.registerCommonMetrics(session, 'adapter/rest/metrics/poll', pollMetrics),
                this.registerCommonMetrics(session, 'adapter/rest/metrics/publication', publicationMetrics),
                this.registerCommonMetrics(session, 'adapter/rest/metrics/topicCreation', topicCreationMetrics)];
            session.subscribe('?adapter/rest/metrics/');

            return new MetricsViewImpl(session, streams, pollMetrics, publicationMetrics, topicCreationMetrics);
        });
    }
}

export interface CommonMetrics {
    requests: number,
    successes: number,
    failures: number,
    requestThroughput: number;
    failureThroughput: number;
    maximumSuccessfulRequestTime: number;
    minimumSuccessfulRequestTime: number;
    successfulRequestTimeNinetiethPercentile: number;
}

export interface MetricsView {
    pollMetrics: CommonMetrics;
    publicationMetrics: CommonMetrics;
    topicCreationMetrics: CommonMetrics;

    close();
}

class MetricsViewImpl implements MetricsView {
    constructor(
        private session,
        private streams,
        public pollMetrics: CommonMetrics,
        public publicationMetrics: CommonMetrics,
        public topicCreationMetrics: CommonMetrics) {}

    close() {
        this.streams.forEach(stream => stream.close());
        this.session.unsubscribe('?adapter/rest/metrics/');
    }
}
