import {Injectable} from '@angular/core';
import {DiffusionService} from './diffusion.service';
import * as diffusion from 'diffusion';

const doubleDataType = diffusion.datatypes.double();
const longDataType = diffusion.datatypes.int64();

export interface Closeable {
    close();
}

@Injectable()
export class MetricsService {
    private areMetricsReady: Promise<void>;

    constructor(private diffusionService: DiffusionService) {
        this.areMetricsReady = new Promise(((resolve, reject) => {
            diffusionService.get().then(session => {
                var registration;
                let pendingRegistration = session
                    .notifications
                    .addListener({
                        onTopicNotification: (path, spec, type) => {
                            if (type === session.notifications.TopicNotificationType.ADDED ||
                                type === session.notifications.TopicNotificationType.SELECTED) {

                                resolve();
                                registration.close();
                            }
                        },
                        onDescendantNotification: () => {},
                        onError: (err) => {
                            reject(err);
                        },
                        onClose: () => {}
                    });
                pendingRegistration
                    .then(reg => registration = reg)
                    .then(() => registration.select('adapter/rest/metrics/poll/requests'), reject);
            }, reject);
        }));
    }

    public createEmptyView(): MetricsView {
        return {
            pollMetrics: MetricsService.createPollMetrics(),
            publicationMetrics: MetricsService.createPublicationMetrics(),
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

    private static createPublicationMetrics(): PublicationMetrics {
        return {
            requests: 0,
            successes: 0,
            failures: 0,
            bytes: 0,
            requestThroughput: 0,
            failureThroughput: 0,
            maximumSuccessfulRequestTime: 0,
            minimumSuccessfulRequestTime: 0,
            successfulRequestTimeNinetiethPercentile: 0,
            meanBytes: 0
        };
    }

    private static createPollMetrics(): PollMetrics {
        return {
            requests: 0,
            successes: 0,
            failures: 0,
            bytes: 0,
            requestThroughput: 0,
            failureThroughput: 0,
            maximumSuccessfulRequestTime: 0,
            minimumSuccessfulRequestTime: 0,
            successfulRequestTimeNinetiethPercentile: 0
        };
    }

    private registerCommonMetrics(session, topicRoot, metrics: CommonMetrics): Closeable {
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

    private registerPublicationMetrics(session, topicRoot, metrics: PublicationMetrics): Closeable {
        let streams = [
            this.registerCommonMetrics(session, topicRoot, metrics),
            session
                .stream(topicRoot + '/bytes')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.bytes = newValue;
                }),
            session
                .stream(topicRoot + '/meanBytesPerPublication')
                .asType(doubleDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.meanBytes = newValue;
                })];
        return {
            close() {
                streams.forEach(stream => stream.close());
            }
        };
    }

    private registerPollMetrics(session, topicRoot, metrics: PollMetrics): Closeable {
        let streams = [
            this.registerCommonMetrics(session, topicRoot, metrics),
            session
                .stream(topicRoot + '/bytes')
                .asType(longDataType)
                .on('value', (topic, specification, newValue) => {
                    metrics.bytes = newValue;
                })];
        return {
            close() {
                streams.forEach(stream => stream.close());
            }
        };
    }

    public metricsReady(): Promise<void> {
        return this.areMetricsReady;
    }

    public getMetricsView(): Promise<MetricsView> {
        return this.diffusionService.get().then((session) => {
            let pollMetrics = MetricsService.createPollMetrics();
            let publicationMetrics = MetricsService.createPublicationMetrics();
            let topicCreationMetrics = MetricsService.createCommonMetrics();

            let streams = [
                this.registerPollMetrics(session, 'adapter/rest/metrics/poll', pollMetrics),
                this.registerPublicationMetrics(session, 'adapter/rest/metrics/publication', publicationMetrics),
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

export interface PublicationMetrics extends CommonMetrics {
    bytes: number,
    meanBytes: number
}

export interface PollMetrics extends CommonMetrics {
    bytes: number
}

export interface MetricsView extends Closeable {
    pollMetrics: PollMetrics;
    publicationMetrics: PublicationMetrics;
    topicCreationMetrics: CommonMetrics;
}

class MetricsViewImpl implements MetricsView {
    constructor(
        private session,
        private streams,
        public pollMetrics: PollMetrics,
        public publicationMetrics: PublicationMetrics,
        public topicCreationMetrics: CommonMetrics) {}

    close() {
        this.streams.forEach(stream => stream.close());
        this.session.unsubscribe('?adapter/rest/metrics/');
    }
}
