import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';

import { GatewayApi } from '../api/gateway-api';
import { UsageResponse } from '../api/gateway.models';
import { formatUsd } from '../shared/format';

@Component({
  selector: 'app-usage-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
  templateUrl: './usage-dashboard.html',
})
export class UsageDashboard {
  private readonly api = inject(GatewayApi);

  protected readonly data = signal<UsageResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Fraction of budget spent, clamped to [0, 1] for the progress bar width. */
  protected readonly budgetPct = computed(() => {
    const b = this.data()?.budget;
    if (!b || !b.enforced || b.limitUsd <= 0) {
      return 0;
    }
    return Math.min(1, Math.max(0, b.spentUsd / b.limitUsd));
  });

  protected readonly formatUsd = formatUsd;

  constructor() {
    this.refresh();
  }

  protected refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.usage(24).subscribe({
      next: (res) => {
        this.data.set(res);
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message);
        this.loading.set(false);
      },
    });
  }
}
