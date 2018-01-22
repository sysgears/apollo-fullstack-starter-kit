import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Subscription } from 'rxjs/Subscription';

import { AlertItem, AlertType } from '../../common/components/Alert';

const alertClasses = [
  {
    type: AlertType.SUCCESS,
    className: 'ant-alert-success'
  },
  {
    type: AlertType.INFO,
    className: 'ant-alert-info'
  },
  {
    type: AlertType.WARNING,
    className: 'ant-alert-warning'
  },
  {
    type: AlertType.ERROR,
    className: 'ant-alert-error'
  }
];

@Component({
  selector: 'alert',
  template: `
    <div *ngFor="let alert of alerts" data-show="true" class="{{'ant-alert ant-alert-no-icon ' + alert.type}}">
      <span class="ant-alert-message">{{alert.message}}</span>
      <span class="ant-alert-description"></span>
      <a class="ant-alert-close-icon" (click)="onClose(alert)">
        <i class="anticon anticon-cross"></i>
      </a>
    </div>
  `
})
export default class Alert implements OnInit, OnDestroy {
  @Input() public subject: Subject<AlertItem>;
  public alerts: AlertItem[] = [];
  private subscription: Subscription;

  public ngOnInit(): void {
    this.subscription = this.subject.subscribe((alert: any) => {
      setTimeout(() => this.onClose(alert), 5000);
      alert.type = this.getClassByType(alert.type);
      this.alerts.push(alert);
    });
  }

  public ngOnDestroy(): void {
    this.unsubscribe(this.subscription);
  }

  public onClose(alert: AlertItem) {
    const index = this.alerts.indexOf(alert);
    this.alerts.splice(index, 1);
  }

  private getClassByType(type: AlertType) {
    return alertClasses.find((alertClass: any) => alertClass.type === type).className;
  }

  private unsubscribe = (...subscriptions: Subscription[]) => {
    subscriptions.forEach((subscription: Subscription) => {
      if (subscription) {
        subscription.unsubscribe();
        subscription = null;
      }
    });
  };
}
