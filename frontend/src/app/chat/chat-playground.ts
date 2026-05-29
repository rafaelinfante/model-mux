import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { GatewayApi } from '../api/gateway-api';
import { ChatResponse, Provider, RouteMode } from '../api/gateway.models';
import { formatUsd, tierClasses } from '../shared/format';

@Component({
  selector: 'app-chat-playground',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './chat-playground.html',
})
export class ChatPlayground {
  private readonly api = inject(GatewayApi);

  protected readonly modes: RouteMode[] = ['EXPLICIT', 'COST', 'FAILOVER'];

  protected readonly prompt = signal('');
  protected readonly mode = signal<RouteMode>('COST');
  protected readonly provider = signal('');
  protected readonly temperature = signal<number | null>(null);
  protected readonly maxTokens = signal<number | null>(null);
  protected readonly forceFailover = signal(false);

  protected readonly providers = signal<Provider[]>([]);
  protected readonly result = signal<ChatResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly canSend = computed(
    () => this.prompt().trim().length > 0 && !this.loading(),
  );

  protected readonly tierClasses = tierClasses;
  protected readonly formatUsd = formatUsd;

  constructor() {
    this.api.providers().subscribe({
      next: (list) => {
        this.providers.set(list);
        if (list.length > 0) {
          this.provider.set(list[0].id);
        }
      },
      // A missing provider list should not block the rest of the playground.
      error: () => this.providers.set([]),
    });
  }

  protected send(): void {
    if (!this.canSend()) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    const mode = this.mode();
    this.api
      .chat({
        prompt: this.prompt().trim(),
        mode,
        provider: mode === 'EXPLICIT' ? this.provider() || undefined : undefined,
        temperature: this.temperature() ?? undefined,
        maxTokens: this.maxTokens() ?? undefined,
        forceFailover: this.forceFailover(),
      })
      .subscribe({
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
