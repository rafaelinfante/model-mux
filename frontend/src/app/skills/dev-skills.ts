import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GatewayApi } from '../api/gateway-api';
import {
  HealthDigestResponse,
  PrSummaryResponse,
  TestStubResponse,
} from '../api/gateway.models';
import { formatUsd, severityClasses } from '../shared/format';

@Component({
  selector: 'app-dev-skills',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './dev-skills.html',
})
export class DevSkills {
  private readonly api = inject(GatewayApi);

  // PR summary panel state.
  protected readonly diff = signal('');
  protected readonly prJavaSource = signal('');
  protected readonly prResult = signal<PrSummaryResponse | null>(null);
  protected readonly prLoading = signal(false);
  protected readonly prError = signal<string | null>(null);

  // Test-stub panel state.
  protected readonly stubSource = signal('');
  protected readonly stubResult = signal<TestStubResponse | null>(null);
  protected readonly stubLoading = signal(false);
  protected readonly stubError = signal<string | null>(null);

  // Health digest panel state.
  protected readonly metrics = signal('');
  protected readonly digestResult = signal<HealthDigestResponse | null>(null);
  protected readonly digestLoading = signal(false);
  protected readonly digestError = signal<string | null>(null);

  protected readonly severityClasses = severityClasses;
  protected readonly formatUsd = formatUsd;

  protected runPrSummary(): void {
    if (this.diff().trim().length === 0 || this.prLoading()) {
      return;
    }
    this.prLoading.set(true);
    this.prError.set(null);
    this.prResult.set(null);
    this.api
      .prSummary({
        diff: this.diff(),
        javaSource: this.prJavaSource().trim() || undefined,
      })
      .subscribe({
        next: (res) => {
          this.prResult.set(res);
          this.prLoading.set(false);
        },
        error: (err: Error) => {
          this.prError.set(err.message);
          this.prLoading.set(false);
        },
      });
  }

  protected runTestStub(): void {
    if (this.stubSource().trim().length === 0 || this.stubLoading()) {
      return;
    }
    this.stubLoading.set(true);
    this.stubError.set(null);
    this.stubResult.set(null);
    this.api.testStub({ javaSource: this.stubSource() }).subscribe({
      next: (res) => {
        this.stubResult.set(res);
        this.stubLoading.set(false);
      },
      error: (err: Error) => {
        this.stubError.set(err.message);
        this.stubLoading.set(false);
      },
    });
  }

  protected runHealthDigest(): void {
    if (this.metrics().trim().length === 0 || this.digestLoading()) {
      return;
    }
    this.digestLoading.set(true);
    this.digestError.set(null);
    this.digestResult.set(null);
    this.api.healthDigest({ metrics: this.metrics() }).subscribe({
      next: (res) => {
        this.digestResult.set(res);
        this.digestLoading.set(false);
      },
      error: (err: Error) => {
        this.digestError.set(err.message);
        this.digestLoading.set(false);
      },
    });
  }
}
