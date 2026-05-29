/**
 * Typed mirrors of the model-mux backend contract (base path /api).
 * These shapes are duplicated from the Spring Boot DTOs; keep them in sync.
 */

export type RouteMode = 'EXPLICIT' | 'COST' | 'FAILOVER';
export type Tier = 'CHEAP' | 'PREMIUM';

export interface ChatRequest {
  prompt: string;
  system?: string;
  maxTokens?: number;
  temperature?: number;
  mode?: RouteMode;
  provider?: string;
  forceFailover: boolean;
}

export interface ChatResponse {
  content: string;
  provider: string;
  providerType: string;
  model: string;
  tier: Tier;
  routeMode: RouteMode;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  costUsd: number;
  latencyMs: number;
  finishReason: string;
  cacheHit: boolean;
  failedOver: boolean;
  correlationId: string;
}

export interface Provider {
  id: string;
  displayName: string;
  type: string;
  model: string;
  tier: Tier;
}

export interface CompareRequest {
  prompt: string;
  system?: string;
  maxTokens?: number;
  temperature?: number;
  providers?: string[];
}

export interface CompareUsage {
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

export interface CompareArm {
  providerId: string;
  displayName: string;
  model: string;
  tier: Tier;
  ok: boolean;
  content: string;
  usage: CompareUsage;
  costUsd: number;
  latencyMs: number;
  error: string | null;
}

export interface CompareResponse {
  prompt: string;
  arms: CompareArm[];
}

export interface UsagePerProvider {
  providerId: string;
  requests: number;
  cost: number;
  tokens: number;
}

export interface UsageBudget {
  enforced: boolean;
  overBudget: boolean;
  action: string;
  limitUsd: number;
  spentUsd: number;
  remainingUsd: number;
}

export interface UsageResponse {
  window: string;
  requests: number;
  costUsd: number;
  tokens: number;
  cacheHits: number;
  failovers: number;
  perProvider: UsagePerProvider[];
  budget: UsageBudget;
}

export type RiskSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string;

export interface PrRisk {
  severity: RiskSeverity;
  area: string;
  detail: string;
}

export interface PrReview {
  summary: string;
  risks: PrRisk[];
  testGaps: string[];
}

export interface StyleCheck {
  checked: boolean;
  compliant: boolean;
  formattedSource: string | null;
  error: string | null;
}

export interface SkillMeta {
  provider: string;
  model: string;
  tier: Tier;
  costUsd: number;
  latencyMs: number;
  cacheHit: boolean;
}

export interface PrSummaryRequest {
  diff: string;
  javaSource?: string;
}

export interface PrSummaryResponse {
  review: PrReview;
  styleCheck: StyleCheck;
  meta: SkillMeta;
}

export interface TestStubRequest {
  javaSource: string;
}

export interface TestStubResponse {
  testSource: string;
  meta: SkillMeta;
}

export interface HealthDigestRequest {
  metrics: string;
}

export interface HealthDigestResponse {
  digest: string;
  meta: SkillMeta;
}

/** RFC 9457 problem+json error body returned by the backend. */
export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
}
