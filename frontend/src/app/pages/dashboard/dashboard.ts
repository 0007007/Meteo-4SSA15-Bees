import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { CrudService, describeError } from '../../core/crud.service';
import { Row, humanize } from '../../core/entities';
import { bySeverity, conditionIcon, dayLabel, rounded, shortDateTime, skyOf, todayIso } from '../../core/weather';

interface StationReading {
  station: Row;
  obs: Row | null;
}

/**
 * Weather of one city, assembled from 4 microservices through the gateway:
 * geo-service (cities), station-service (stations), observation-service (observations, air quality),
 * forecast-service (forecasts) and alert-service (alerts).
 */
@Component({
  selector: 'app-dashboard',
  imports: [FormsModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly api = inject(CrudService);

  protected readonly cities = signal<Row[]>([]);
  protected readonly cityId = signal<unknown>(null);
  protected readonly loadingCities = signal(true);
  protected readonly loading = signal(false);
  protected readonly readings = signal<StationReading[]>([]);
  protected readonly forecasts = signal<Row[]>([]);
  protected readonly alerts = signal<Row[]>([]);
  protected readonly airQuality = signal<Row | null>(null);
  protected readonly problems = signal<Record<string, string>>({});

  private readonly today = todayIso();

  protected readonly city = computed(() => this.cities().find((c) => c['id'] === this.cityId()) ?? null);

  /** most recent observation among the city's stations */
  protected readonly latest = computed(() => {
    const withObs = this.readings().filter((r) => r.obs);
    withObs.sort((a, b) => String(b.obs!['observedAt']).localeCompare(String(a.obs!['observedAt'])));
    return withObs[0] ?? null;
  });

  protected readonly upcoming = computed(() =>
    this.forecasts()
      .filter((f) => String(f['forecastDate']) >= this.today)
      .sort((a, b) => String(a['forecastDate']).localeCompare(String(b['forecastDate']))),
  );
  protected readonly todayForecast = computed(() => this.upcoming()[0] ?? null);
  protected readonly nextDays = computed(() => this.upcoming().slice(0, 7));
  protected readonly sky = computed(() => skyOf(this.todayForecast()?.['condition']));

  protected readonly activeAlerts = computed(() => {
    const now = new Date().toISOString().slice(0, 16);
    return this.alerts()
      .filter((a) => a['active'] && (!a['endTime'] || String(a['endTime']).slice(0, 16) >= now))
      .sort(bySeverity);
  });

  protected readonly problemList = computed(() => [...new Set(Object.values(this.problems()))]);

  protected readonly humanize = humanize;
  protected readonly icon = conditionIcon;
  protected readonly round = rounded;
  protected readonly when = shortDateTime;
  protected readonly day = dayLabel;

  constructor() {
    this.api.list('/api/cities').subscribe({
      next: (cities) => {
        this.cities.set(cities);
        this.loadingCities.set(false);
        if (cities.length) this.selectCity(cities[0]['id']);
      },
      error: (err) => {
        this.loadingCities.set(false);
        this.problems.set({ cities: describeError(err, 'geo-service')[0] });
      },
    });
  }

  protected selectCity(id: unknown): void {
    this.cityId.set(id);
    this.readings.set([]);
    this.forecasts.set([]);
    this.alerts.set([]);
    this.airQuality.set(null);
    this.problems.set({});
    this.loading.set(true);
    const cityId = id as number;

    forkJoin({
      stations: this.safe(this.api.list('/api/stations', { cityId }), 'stations', 'station-service'),
      forecasts: this.safe(this.api.list('/api/forecasts', { cityId }), 'forecasts', 'forecast-service'),
      alerts: this.safe(this.api.list('/api/alerts', { cityId }), 'alerts', 'alert-service'),
      air: this.safe(this.api.list('/api/air-quality', { cityId }), 'air', 'observation-service'),
    })
      .pipe(
        switchMap((data) => {
          const perStation = data.stations.map((s) =>
            this.safe(this.api.list('/api/observations', { stationId: s['id'] }), 'observations', 'observation-service'),
          );
          const observations$: Observable<Row[][]> = perStation.length ? forkJoin(perStation) : of([]);
          return observations$.pipe(map((lists) => ({ ...data, lists })));
        }),
      )
      .subscribe(({ stations, forecasts, alerts, air, lists }) => {
        if (this.cityId() !== id) return; // another city was picked meanwhile
        this.readings.set(stations.map((station, i) => ({ station, obs: this.newest(lists[i] ?? [], 'observedAt') })));
        this.forecasts.set(forecasts);
        this.alerts.set(alerts);
        this.airQuality.set(this.newest(air, 'measuredAt'));
        this.loading.set(false);
      });
  }

  /** one failing service must not break the whole page: log the problem, continue with [] */
  private safe(source: Observable<Row[]>, key: string, service: string): Observable<Row[]> {
    return source.pipe(
      catchError((err) => {
        this.problems.update((all) => ({ ...all, [key]: describeError(err, service)[0] }));
        return of([] as Row[]);
      }),
    );
  }

  private newest(rows: Row[], dateKey: string): Row | null {
    return rows.reduce<Row | null>(
      (best, row) => (!best || String(row[dateKey]) > String(best[dateKey]) ? row : best),
      null,
    );
  }
}
