
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
                    <label for="poll-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="poll-request" class="form-control-static col-sm-6">{{view.pollMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="poll-failure" class="form-control-static col-sm-6">{{view.pollMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="poll-max" class="form-control-static col-sm-6">{{view.pollMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="poll-min" class="form-control-static col-sm-6">{{view.pollMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="poll-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="poll-90" class="form-control-static col-sm-6">{{view.pollMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
                    <label for="publication-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="publication-request" class="form-control-static col-sm-6">{{view.publicationMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="publication-failure" class="form-control-static col-sm-6">{{view.publicationMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="publication-max" class="form-control-static col-sm-6">{{view.publicationMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="publication-min" class="form-control-static col-sm-6">{{view.publicationMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="publication-90" class="form-control-static col-sm-6">{{view.publicationMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
                    <label for="publication-request" class="col-sm-6 control-label">Request throughput</label>
                    <p id="publication-request" class="form-control-static col-sm-6">{{view.topicCreationMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-failure" class="col-sm-6 control-label">Failure throughput</label>
                    <p id="publication-failure" class="form-control-static col-sm-6">{{view.topicCreationMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-max" class="col-sm-6 control-label">Max. successful request time</label>
                    <p id="publication-max" class="form-control-static col-sm-6">{{view.topicCreationMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-min" class="col-sm-6 control-label">Min. successful request time</label>
                    <p id="publication-min" class="form-control-static col-sm-6">{{view.topicCreationMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group col-sm-4">
                    <label for="publication-90" class="col-sm-6 control-label">Successful request time 90th percentile</label>
                    <p id="publication-90" class="form-control-static col-sm-6">{{view.topicCreationMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
