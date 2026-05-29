import { Tier } from '../api/gateway.models';

/** Render a USD amount with enough precision to show sub-cent token costs. */
export function formatUsd(value: number | null | undefined): string {
  if (value == null || Number.isNaN(value)) {
    return '$0.000000';
  }
  // Small LLM costs need 6 decimals; larger totals read better with 4.
  const decimals = Math.abs(value) >= 0.01 ? 4 : 6;
  return `$${value.toFixed(decimals)}`;
}

/** Tailwind class pair (bg + text) keyed by routing tier for consistent badges. */
export function tierClasses(tier: Tier | string | null | undefined): string {
  return tier === 'PREMIUM'
    ? 'bg-violet-100 text-violet-700 ring-1 ring-violet-200'
    : 'bg-emerald-100 text-emerald-700 ring-1 ring-emerald-200';
}

/** Severity-colored classes for PR review risk badges. */
export function severityClasses(severity: string | null | undefined): string {
  switch ((severity ?? '').toUpperCase()) {
    case 'CRITICAL':
      return 'bg-red-100 text-red-700 ring-1 ring-red-200';
    case 'HIGH':
      return 'bg-orange-100 text-orange-700 ring-1 ring-orange-200';
    case 'MEDIUM':
      return 'bg-amber-100 text-amber-700 ring-1 ring-amber-200';
    case 'LOW':
      return 'bg-slate-100 text-slate-600 ring-1 ring-slate-200';
    default:
      return 'bg-slate-100 text-slate-600 ring-1 ring-slate-200';
  }
}
