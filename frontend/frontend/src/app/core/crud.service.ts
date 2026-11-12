import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import { Row } from './entities';

type Params = Record<string, string | number | boolean>;

/** Generic REST client: the same five calls work for every entity of every microservice. */
@Injectable({ providedIn: 'root' })
export class CrudService {
  private readonly http = inject(HttpClient);

  list(endpoint: string, params: Params = {}): Observable<Row[]> {
    return this.http.get<Row[]>(API_BASE + endpoint, { params });
  }

  get(endpoint: string, id: unknown): Observable<Row> {
    return this.http.get<Row>(`${API_BASE}${endpoint}/${id}`);
  }

  create(endpoint: string, body: Row): Observable<Row> {
    return this.http.post<Row>(API_BASE + endpoint, body);
  }

  update(endpoint: string, id: unknown, body: Row): Observable<Row> {
    return this.http.put<Row>(`${API_BASE}${endpoint}/${id}`, body);
  }

  delete(endpoint: string, id: unknown): Observable<void> {
    return this.http.delete<void>(`${API_BASE}${endpoint}/${id}`);
  }

  /** POST /{id}/{action}, used by report-service: generate, run */
  action(endpoint: string, id: unknown, action: string): Observable<Row> {
    return this.http.post<Row>(`${API_BASE}${endpoint}/${id}/${action}`, {});
  }

  fileUrl(endpoint: string, id: unknown): string {
    return `${API_BASE}${endpoint}/${id}/download`;
  }
}

/** Turns an HTTP error into sentences a user can act on. */
export function describeError(error: unknown, service: string): string[] {
  if (!(error instanceof HttpErrorResponse)) return ['Something unexpected happened. Check the browser console.'];
  if (error.status === 0) {
    return ['The API gateway does not answer on port 8090. Start api-gateway, then try again.'];
  }
  if (error.status === 503 || error.status === 502) {
    return [`${service} is not reachable through the gateway. Start it and wait until it appears in Eureka (port 8761).`];
  }
  const body = error.error ?? {};
  const lines: string[] = [];
  if (body.detail) lines.push(String(body.detail));
  if (body.errors && typeof body.errors === 'object') {
    for (const [field, message] of Object.entries(body.errors)) lines.push(`${field}: ${message}`);
  }
  if (!lines.length) lines.push(`${service} answered with HTTP ${error.status}.`);
  return lines;
}
