
import {Component, OnDestroy, OnInit} from '@angular/core';
import {MetricsService, MetricsView} from "./metrics.service";

@Component({
    selector: 'metrics',
    template: `<services-list></services-list>
<div class="col-md-8">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Poll metrics</h3>
        </div>
        <div class="panel-body">
            <div class="form-horizontal">
                <div class="form-group col-sm-4">
                    <label for="poll-requests" class="col-sm-6 control-label">Requests</label>
                    <p id="poll-requests" class="form-control-static col-sm-6">{{view.pollMetrics.requests}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-successes" class="col-sm-6 control-label">Successful requests</label>
                    <p id="poll-successes" class="form-control-static col-sm-6">{{view.pollMetrics.successes}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-failures" class="col-sm-6 control-label">Failed requests</label>
                    <p id="poll-failures" class="form-control-static col-sm-6">{{view.pollMetrics.failures}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="poll-request" class="form-control-static col-sm-6">{{view.pollMetrics.requestThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="poll-failure" class="form-control-static col-sm-6">{{view.pollMetrics.failureThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="poll-max" class="form-control-static col-sm-6">{{view.pollMetrics.maximumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="poll-min" class="form-control-static col-sm-6">{{view.pollMetrics.minimumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="poll-90" class="form-control-static col-sm-6">{{view.pollMetrics.successfulRequestTimeNinetiethPercentile}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-bytes" class="col-sm-6 control-label">Polled bytes</label>
                    <p id="poll-bytes" class="form-control-static col-sm-6">{{view.pollMetrics.bytes}}</p>
                </div>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Publication metrics</h3>
        </div>
        <div class="panel-body">
            <div class="form-horizontal">
                <div class="form-group col-sm-4">
                    <label for="publication-requests" class="col-sm-6 control-label">Requests</label>
                    <p id="publication-requests" class="form-control-static col-sm-6">{{view.publicationMetrics.requests}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-successes" class="col-sm-6 control-label">Successful requests</label>
                    <p id="publication-successes" class="form-control-static col-sm-6">{{view.publicationMetrics.successes}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-failures" class="col-sm-6 control-label">Failed requests</label>
                    <p id="publication-failures" class="form-control-static col-sm-6">{{view.publicationMetrics.failures}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="publication-request" class="form-control-static col-sm-6">{{view.publicationMetrics.requestThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="publication-failure" class="form-control-static col-sm-6">{{view.publicationMetrics.failureThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="publication-max" class="form-control-static col-sm-6">{{view.publicationMetrics.maximumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="publication-min" class="form-control-static col-sm-6">{{view.publicationMetrics.minimumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="publication-90" class="form-control-static col-sm-6">{{view.publicationMetrics.successfulRequestTimeNinetiethPercentile}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-bytes" class="col-sm-6 control-label">Published bytes</label>
                    <p id="publication-bytes" class="form-control-static col-sm-6">{{view.publicationMetrics.bytes}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-mean-bytes" class="col-sm-6 control-label">Mean bytes per publication</label>
                    <p id="publication-mean-bytes" class="form-control-static col-sm-6">{{view.publicationMetrics.meanBytes}}</p>
                </div>
            </div>
        </div>
    </div>
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Topic creation metrics</h3>
        </div>
        <div class="panel-body">
            <div class="form-horizontal">
                <div class="form-group col-sm-4">
                    <label for="tc-requests" class="col-sm-6 control-label">Requests</label>
                    <p id="tc-requests" class="form-control-static col-sm-6">{{view.topicCreationMetrics.requests}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-successes" class="col-sm-6 control-label">Successful requests</label>
                    <p id="tc-successes" class="form-control-static col-sm-6">{{view.topicCreationMetrics.successes}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-failures" class="col-sm-6 control-label">Failed requests</label>
                    <p id="tc-failures" class="form-control-static col-sm-6">{{view.topicCreationMetrics.failures}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="tc-request" class="form-control-static col-sm-6">{{view.topicCreationMetrics.requestThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="tc-failure" class="form-control-static col-sm-6">{{view.topicCreationMetrics.failureThroughput}} /s</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="tc-max" class="form-control-static col-sm-6">{{view.topicCreationMetrics.maximumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="tc-min" class="form-control-static col-sm-6">{{view.topicCreationMetrics.minimumSuccessfulRequestTime}} ms</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="tc-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="tc-90" class="form-control-static col-sm-6">{{view.topicCreationMetrics.successfulRequestTimeNinetiethPercentile}} ms</p>
                </div>
            </div>
        </div>
    </div>
</div>`
})
export class MetricsComponent implements OnInit, OnDestroy {
    private view: MetricsView;

    constructor(private metricsService: MetricsService) {
        this.view = this.metricsService.createEmptyView();
    }

    ngOnInit(): void {
        this
            .metricsService
            .getMetricsView()
            .then(view => {
                this.view = view;
            });
    }

    ngOnDestroy(): void {
        this.view.close();
    }
}
