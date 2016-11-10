import { Component, Input } from '@angular/core';

@Component({
  selector: 'root',
  template: `<div id="root" class="container">
    <div class="row">
        <router-outlet></router-outlet>
    </div>
</div>`
})
export class RootComponent {
}
