import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';

import {
  ChatRequest,
  ChatResponse,
  CompareRequest,
  CompareResponse,
  HealthDigestRequest,
  HealthDigestResponse,
  PrSummaryRequest,
  PrSummaryResponse,
  ProblemDetail,
  Provider,
  TestStubRequest,
  TestStubResponse,
  UsageResponse,
} from './gateway.models';

/** Relative base path so the bundle works identically behind nginx in production. */
const BASE = '/api';

/**
 * Thin typed wrapper over the model-mux REST endpoints. Every call rethrows a
 * normalized message sourced from the RFC 9457 `detail` field so components can
 * surface a single human-readable string.
 */
@Injectable({ providedIn: 'root' })
export class GatewayApi {
  private readonly http = inject(HttpClient);

  providers(): Observable<Provider[]> {
    return this.http.get<Provider[]>(`${BASE}/providers`).pipe(catchError(toMessage));
  }

  chat(req: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${BASE}/chat`, req).pipe(catchError(toMessage));
  }

  compare(req: CompareRequest): Observable<CompareResponse> {
    return this.http.post<CompareResponse>(`${BASE}/compare`, req).pipe(catchError(toMessage));
  }

  usage(hours: number): Observable<UsageResponse> {
    return this.http
      .get<UsageResponse>(`${BASE}/usage`, { params: { hours } })
      .pipe(catchError(toMessage));
  }

  prSummary(req: PrSummaryRequest): Observable<PrSummaryResponse> {
    return this.http
      .post<PrSummaryResponse>(`${BASE}/skills/pr-summary`, req)
      .pipe(catchError(toMessage));
  }

  testStub(req: TestStubRequest): Observable<TestStubResponse> {
    return this.http
      .post<TestStubResponse>(`${BASE}/skills/test-stub`, req)
      .pipe(catchError(toMessage));
  }

  healthDigest(req: HealthDigestRequest): Observable<HealthDigestResponse> {
    return this.http
      .post<HealthDigestResponse>(`${BASE}/skills/health-digest`, req)
      .pipe(catchError(toMessage));
  }
}

/** Collapse an HttpErrorResponse into an Error carrying the backend's `detail`. */
function toMessage(err: HttpErrorResponse): Observable<never> {
  const problem = err.error as ProblemDetail | string | null;
  let message: string;
  if (problem && typeof problem === 'object' && (problem.detail || problem.title)) {
    message = problem.detail ?? problem.title ?? 'Request failed';
  } else if (typeof problem === 'string' && problem.trim()) {
    message = problem;
  } else if (err.status === 0) {
    message = 'Cannot reach the model-mux backend at /api. Is it running on :8080?';
  } else {
    message = err.message || `Request failed with status ${err.status}`;
  }
  return throwError(() => new Error(message));
}
