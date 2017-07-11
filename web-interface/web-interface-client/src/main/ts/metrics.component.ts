
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'metrics',
    template: `<services-list></services-list>
<div class="col-md-8">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Metrics</h3>
        </div>
        <div class="panel-body">
            <div id="visualization"></div>
        </div>
    </div>
</div>`
})
export class MetricsComponent implements OnInit {

    ngOnInit(): void {
        // DOM element where the Timeline will be attached
        var container = document.getElementById('visualization');

        // Create a DataSet (allows two way data-binding)
        var items = new vis.DataSet([
            {id: 1, content: 'www.example.org/hello', className: 'poll-event', start: new Date(2017, 7, 11, 0, 0, 0, 0)},
            {id: 2, content: 'example/data', className: 'publish-event', start: new Date(2017, 7, 11, 0, 0, 1, 0)}
        ]);

        // Configuration for the Timeline
        var options = {
            selectable: false
        };

        // Create a Timeline
        var timeline = new vis.Timeline(container, items, options);
    }
}
