
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
                <div class="form-group">
                    <label for="url" class="col-sm-2 control-label">Request throughput</label>
                    <p id="url" class="form-control-static col-sm-4">{{view.pollMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Failure throughput</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.pollMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Max. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.pollMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Min. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.pollMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Successful request time 90th percentile</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.pollMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
                <div class="form-group">
                    <label for="url" class="col-sm-2 control-label">Request throughput</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.publicationMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Failure throughput</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.publicationMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Max. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.publicationMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Min. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.publicationMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Successful request time 90th percentile</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.publicationMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
                <div class="form-group">
                    <label for="url" class="col-sm-2 control-label">Request throughput</label>
                    <p id="url" class="form-control-static col-sm-4">{{view.topicCreationMetrics.requestThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Failure throughput</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.topicCreationMetrics.failureThroughput}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Max. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.topicCreationMetrics.maximumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Min. successful request time</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.topicCreationMetrics.minimumSuccessfulRequestTime}}</p>
                </div>
                <div class="form-group">
                    <label for="topicPath" class="col-sm-2 control-label">Successful request time 90th percentile</label>
                    <p id="topicPath" class="form-control-static col-sm-4">{{view.topicCreationMetrics.successfulRequestTimeNinetiethPercentile}}</p>
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
