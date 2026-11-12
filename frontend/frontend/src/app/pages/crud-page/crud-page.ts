import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { CrudService, describeError } from '../../core/crud.service';
import { ENTITIES, EntityDef, FieldDef, RefDef, Row, findEntity, humanize } from '../../core/entities';

interface Option {
  id: unknown;
  label: string;
}

const INPUT_TYPES: Record<string, string> = {
  number: 'number',
  date: 'date',
  datetime: 'datetime-local',
  email: 'email',
};

const capitalize = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);

/**
 * Generic list + form page. The route /manage/:resource picks the entity description
 * in core/entities.ts, so the same component serves all 14 entities of the 7 microservices.
 */
@Component({
  selector: 'app-crud-page',
  imports: [FormsModule],
  templateUrl: './crud-page.html',
  styleUrl: './crud-page.css',
  host: { '(document:keydown.escape)': 'closeForm()' },
})
export class CrudPage {
  private readonly api = inject(CrudService);
  private readonly pageTitle = inject(Title);

  /** route parameter :resource (bound by withComponentInputBinding) */
  readonly resource = input.required<string>();

  protected readonly entity = computed(() => findEntity(this.resource()));
  protected readonly columns = computed(() => this.entity()?.fields.filter((f) => f.inTable !== false) ?? []);
  protected readonly formFields = computed(() => this.entity()?.fields.filter((f) => !f.readOnly) ?? []);

  protected readonly rows = signal<Row[]>([]);
  protected readonly refs = signal<Record<string, Row[]>>({});
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly busyId = signal<unknown>(null);
  protected readonly errors = signal<string[]>([]);
  protected readonly formErrors = signal<string[]>([]);
  protected readonly notice = signal('');
  protected readonly filterValue = signal<unknown>(null);
  protected readonly formOpen = signal(false);
  protected readonly editingId = signal<unknown>(null);
  protected readonly formTitle = computed(() => {
    const def = this.entity();
    if (!def) return '';
    return this.editingId() === null ? `Add ${def.singular}` : `Edit ${def.singular}`;
  });

  /** values of the open form, bound with ngModel */
  protected draft: Row = {};
  protected readonly humanize = humanize;

  constructor() {
    // runs on first load and every time the route changes to another entity
    effect(() => {
      const def = this.entity();
      untracked(() => this.open(def));
    });
  }

  private open(def: EntityDef | undefined): void {
    this.rows.set([]);
    this.errors.set([]);
    this.notice.set('');
    this.filterValue.set(null);
    this.formOpen.set(false);
    if (!def) {
      this.pageTitle.setTitle('Page not found - Meteo App');
      return;
    }
    this.pageTitle.setTitle(`${def.title} - Meteo App`);
    this.reload();
    this.loadRefs(def);
  }

  protected reload(): void {
    const def = this.entity();
    if (!def) return;
    const filter = this.filterValue();
    const params = def.filter && filter !== null ? { [def.filter.param]: filter as string | number } : {};
    this.loading.set(true);
    this.api.list(def.endpoint, params).subscribe({
      next: (rows) => {
        if (this.entity()?.key !== def.key) return; // the user already moved to another page
        this.rows.set(rows);
        this.errors.set([]);
        this.loading.set(false);
      },
      error: (err) => {
        if (this.entity()?.key !== def.key) return;
        this.rows.set([]);
        this.errors.set(describeError(err, def.service));
        this.loading.set(false);
      },
    });
  }

  /** Loads the lists used by dropdowns: countries for a city, cities for a station... */
  private loadRefs(def: EntityDef): void {
    const needed = new Set<string>();
    def.fields.forEach((f) => f.ref && needed.add(f.ref.resource));
    if (def.filter) needed.add(def.filter.ref.resource);
    this.refs.set({});
    for (const key of needed) {
      const target = ENTITIES.find((e) => e.key === key);
      if (!target) continue;
      this.api.list(target.endpoint).subscribe({
        next: (list) => this.refs.update((all) => ({ ...all, [key]: list })),
        error: () => this.refs.update((all) => ({ ...all, [key]: [] })),
      });
    }
  }

  protected refOptions(ref: RefDef): Option[] {
    return (this.refs()[ref.resource] ?? []).map((r) => ({ id: r['id'], label: String(r[ref.labelKey] ?? r['id']) }));
  }

  protected refTitle(ref: RefDef): string {
    return ENTITIES.find((e) => e.key === ref.resource)?.title.toLowerCase() ?? ref.resource;
  }

  protected applyFilter(value: unknown): void {
    this.filterValue.set(value);
    this.reload();
  }

  protected display(row: Row, f: FieldDef): string {
    const value = row[f.key];
    if (value === null || value === undefined || value === '') return '';
    switch (f.type) {
      case 'checkbox':
        return value ? 'Yes' : 'No';
      case 'datetime':
        return String(value).replace('T', ' ').slice(0, 16);
      case 'select':
        return humanize(value);
      case 'ref': {
        const ref = f.ref!;
        if (ref.asObject) return String(value[ref.labelKey] ?? value['id'] ?? '');
        const match = (this.refs()[ref.resource] ?? []).find((r) => r['id'] === value);
        return match ? String(match[ref.labelKey]) : `#${value}`;
      }
      default:
        return String(value);
    }
  }

  protected inputType(f: FieldDef): string {
    return INPUT_TYPES[f.type] ?? 'text';
  }

  protected openCreate(): void {
    const def = this.entity();
    if (!def) return;
    const draft: Row = {};
    for (const f of this.formFields()) draft[f.key] = f.default ?? (f.type === 'checkbox' ? false : null);
    // adding a city while the list is filtered on a country: pre-select that country
    const filter = this.filterValue();
    if (def.filter && filter !== null) {
      const field = def.fields.find((x) => x.ref?.resource === def.filter!.ref.resource);
      if (field) draft[field.key] = filter;
    }
    this.draft = draft;
    this.editingId.set(null);
    this.formErrors.set([]);
    this.formOpen.set(true);
  }

  protected openEdit(row: Row): void {
    const draft: Row = {};
    for (const f of this.formFields()) {
      const value = row[f.key];
      if (f.type === 'ref' && f.ref?.asObject) draft[f.key] = value?.['id'] ?? null;
      else if (f.type === 'datetime') draft[f.key] = value ? String(value).slice(0, 16) : null;
      else draft[f.key] = value ?? (f.type === 'checkbox' ? false : null);
    }
    this.draft = draft;
    this.editingId.set(row['id']);
    this.formErrors.set([]);
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
  }

  protected save(): void {
    const def = this.entity();
    if (!def) return;
    const missing = this.formFields().filter(
      (f) => f.required && f.type !== 'checkbox' && [null, undefined, ''].includes(this.draft[f.key]),
    );
    if (missing.length) {
      this.formErrors.set(missing.map((f) => `${f.label} is required.`));
      return;
    }
    const id = this.editingId();
    const body = this.toPayload();
    const request = id === null ? this.api.create(def.endpoint, body) : this.api.update(def.endpoint, id, body);
    this.saving.set(true);
    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.notice.set(`${capitalize(def.singular)} ${id === null ? 'created' : 'saved'}.`);
        this.reload();
      },
      error: (err) => {
        this.saving.set(false);
        this.formErrors.set(describeError(err, def.service));
      },
    });
  }

  /** Converts the form values to the JSON the microservice expects. */
  private toPayload(): Row {
    const body: Row = {};
    for (const f of this.formFields()) {
      let value = this.draft[f.key];
      if (value === '' || value === undefined) value = null;
      if (f.type === 'number' && value !== null) value = Number(value);
      if (f.type === 'checkbox') value = !!value;
      if (f.type === 'ref' && f.ref?.asObject) value = value === null ? null : { id: value };
      body[f.key] = value;
    }
    return body;
  }

  protected remove(row: Row): void {
    const def = this.entity();
    if (!def || !confirm(`Delete this ${def.singular}? This cannot be undone.`)) return;
    this.api.delete(def.endpoint, row['id']).subscribe({
      next: () => {
        this.notice.set(`${capitalize(def.singular)} deleted.`);
        this.reload();
      },
      error: (err) => this.errors.set(describeError(err, def.service)),
    });
  }

  /** report-service: collects the data from the other services and writes the file */
  protected generate(row: Row): void {
    const def = this.entity();
    if (!def) return;
    this.busyId.set(row['id']);
    this.errors.set([]);
    this.api.action(def.endpoint, row['id'], 'generate').subscribe({
      next: (updated) => {
        this.busyId.set(null);
        this.rows.update((rows) => rows.map((r) => (r['id'] === updated['id'] ? updated : r)));
        this.notice.set(
          updated['status'] === 'GENERATED'
            ? `Report generated: ${updated['fileName']}. Use Download to open it.`
            : `Generation failed: ${updated['errorMessage']}`,
        );
      },
      error: (err) => {
        this.busyId.set(null);
        this.errors.set(describeError(err, def.service));
      },
    });
  }

  /** report-service: creates and generates the report of the schedule's last period */
  protected run(row: Row): void {
    const def = this.entity();
    if (!def) return;
    this.busyId.set(row['id']);
    this.errors.set([]);
    this.api.action(def.endpoint, row['id'], 'run').subscribe({
      next: (report) => {
        this.busyId.set(null);
        this.notice.set(
          report['status'] === 'GENERATED'
            ? `Report ${report['id']} created and generated. Open Reports to download it.`
            : `Report ${report['id']} created, but generation failed: ${report['errorMessage']}`,
        );
      },
      error: (err) => {
        this.busyId.set(null);
        this.errors.set(describeError(err, def.service));
      },
    });
  }

  protected fileUrl(row: Row): string {
    return this.api.fileUrl(this.entity()?.endpoint ?? '', row['id']);
  }
}
