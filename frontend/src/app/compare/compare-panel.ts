import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GatewayApi } from '../api/gateway-api';
import { CompareArm, CompareResponse } from '../api/gateway.models';
import { formatUsd, tierClasses } from '../shared/format';

@Component({
  selector: 'app-compare-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './compare-panel.html',
})
export class ComparePanel {
  private readonly api = inject(GatewayApi);

  protected readonly prompt = signal('');
  protected readonly result = signal<CompareResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly canSend = computed(
    () => this.prompt().trim().length > 0 && !this.loading(),
  );

  /** providerId of the fastest successful arm, used to badge the winner. */
  protected readonly fastestId = computed(() => bestArm(this.result(), (a) => a.latencyMs));
  /** providerId of the cheapest successful arm. */
  protected readonly cheapestId = computed(() => bestArm(this.result(), (a) => a.costUsd));

  protected readonly tierClasses = tierClasses;
  protected readonly formatUsd = formatUsd;

  protected send(): void {
    if (!this.canSend()) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    this.api.compare({ prompt: this.prompt().trim() }).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message);
        this.loading.set(false);
      },
    });
  }
}

/** Return the providerId of the OK arm minimizing `pick`, or null if none. */
function bestArm(
  result: CompareResponse | null,
  pick: (arm: CompareArm) => number,
): string | null {
  const ok = result?.arms.filter((a) => a.ok) ?? [];
  if (ok.length < 2) {
    return null;
  }
  return ok.reduce((best, arm) => (pick(arm) < pick(best) ? arm : best)).providerId;
}
