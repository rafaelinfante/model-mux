import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

import { ChatPlayground } from './chat/chat-playground';
import { ComparePanel } from './compare/compare-panel';
import { DevSkills } from './skills/dev-skills';
import { UsageDashboard } from './usage/usage-dashboard';

type Tab = 'chat' | 'compare' | 'usage' | 'skills';

interface TabDef {
  id: Tab;
  label: string;
}

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ChatPlayground, ComparePanel, UsageDashboard, DevSkills],
  templateUrl: './app.html',
})
export class App {
  protected readonly tabs: TabDef[] = [
    { id: 'chat', label: 'Chat' },
    { id: 'compare', label: 'Compare' },
    { id: 'usage', label: 'Usage' },
    { id: 'skills', label: 'Dev skills' },
  ];

  protected readonly active = signal<Tab>('chat');
}
