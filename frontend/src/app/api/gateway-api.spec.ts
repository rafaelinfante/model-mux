import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { GatewayApi } from './gateway-api';
import { ChatResponse, Provider } from './gateway.models';

describe('GatewayApi', () => {
  let api: GatewayApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GatewayApi, provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(GatewayApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs providers from /api/providers', () => {
    const expected: Provider[] = [
      { id: 'openai', displayName: 'OpenAI', type: 'openai', model: 'gpt-4o-mini', tier: 'CHEAP' },
    ];
    let received: Provider[] | undefined;
    api.providers().subscribe((p) => (received = p));

    const req = httpMock.expectOne('/api/providers');
    expect(req.request.method).toBe('GET');
    req.flush(expected);

    expect(received).toEqual(expected);
  });

  it('POSTs the chat request body to /api/chat', () => {
    const response = { content: 'hi', provider: 'openai' } as ChatResponse;
    let received: ChatResponse | undefined;
    api.chat({ prompt: 'hello', mode: 'COST', forceFailover: false }).subscribe((r) => (received = r));

    const req = httpMock.expectOne('/api/chat');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ prompt: 'hello', mode: 'COST', forceFailover: false });
    req.flush(response);

    expect(received).toEqual(response);
  });

  it('passes the hours query param to /api/usage', () => {
    api.usage(24).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/usage');
    expect(req.request.params.get('hours')).toBe('24');
    req.flush({});
  });

  it('surfaces the RFC 9457 detail field as the error message', () => {
    let message: string | undefined;
    api.chat({ prompt: 'x', forceFailover: false }).subscribe({
      error: (e: Error) => (message = e.message),
    });

    const req = httpMock.expectOne('/api/chat');
    req.flush(
      { title: 'Bad Request', detail: 'temperature must be <= 2', status: 400 },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(message).toBe('temperature must be <= 2');
  });
});
