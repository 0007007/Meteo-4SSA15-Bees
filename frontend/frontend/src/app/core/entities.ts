/**
 * One description per entity drives the generic list + form page (pages/crud-page).
 * To change what a page shows, edit the fields here: each member owns the 2 entries of their service.
 */

export type Row = Record<string, any>;

export type FieldType = 'text' | 'textarea' | 'email' | 'number' | 'date' | 'datetime' | 'select' | 'checkbox' | 'ref';

export interface RefDef {
  /** key of the entity to pick from, e.g. 'cities' */
  resource: string;
  /** property shown in the dropdown, e.g. 'name' */
  labelKey: string;
  /** true: sent as { "id": 1 } (same service, JPA relation). false: sent as a plain id (other service). */
  asObject: boolean;
}

export interface FieldDef {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
  options?: string[];
  ref?: RefDef;
  default?: unknown;
  /** filled by the server (createdAt, status...): shown in the table, not in the form */
  readOnly?: boolean;
  /** false hides the column in the table (long texts) */
  inTable?: boolean;
  min?: number;
  max?: number;
  unit?: string;
}

export type EntityAction = 'generate' | 'download' | 'run';

export interface EntityDef {
  key: string;
  title: string;
  singular: string;
  endpoint: string;
  service: string;
  language: 'Java' | 'Python';
  database: 'PostgreSQL' | 'MongoDB' | 'MySQL';
  owner: string;
  intro: string;
  fields: FieldDef[];
  filter?: { param: string; label: string; ref: RefDef };
  actions?: EntityAction[];
}

const city = (required = true): FieldDef => ({
  key: 'cityId', label: 'City', type: 'ref', required,
  ref: { resource: 'cities', labelKey: 'name', asObject: false },
});
const cityFilter = { param: 'cityId', label: 'City', ref: { resource: 'cities', labelKey: 'name', asObject: false } };
const lat: FieldDef = { key: 'latitude', label: 'Latitude', type: 'number', required: true, min: -90, max: 90 };
const lon: FieldDef = { key: 'longitude', label: 'Longitude', type: 'number', required: true, min: -180, max: 180 };

export const ENTITIES: EntityDef[] = [
  // ---------------------------------------------------------------- geo-service (Member 1)
  {
    key: 'countries', title: 'Countries', singular: 'country', endpoint: '/api/countries',
    service: 'geo-service', language: 'Java', database: 'PostgreSQL', owner: 'Member 1',
    intro: 'Countries group the cities. A country with cities cannot be deleted.',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'isoCode', label: 'ISO code', type: 'text', required: true },
      { key: 'continent', label: 'Continent', type: 'text' },
    ],
  },
  {
    key: 'cities', title: 'Cities', singular: 'city', endpoint: '/api/cities',
    service: 'geo-service', language: 'Java', database: 'PostgreSQL', owner: 'Member 1',
    intro: 'Every station, forecast, alert and report points to a city.',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'country', label: 'Country', type: 'ref', required: true, ref: { resource: 'countries', labelKey: 'name', asObject: true } },
      lat, lon,
      { key: 'population', label: 'Population', type: 'number', min: 0 },
      { key: 'timezone', label: 'Time zone', type: 'text', default: 'Africa/Tunis' },
    ],
    filter: { param: 'countryId', label: 'Country', ref: { resource: 'countries', labelKey: 'name', asObject: true } },
  },
  // ---------------------------------------------------------------- station-service (Member 2)
  {
    key: 'stations', title: 'Weather stations', singular: 'station', endpoint: '/api/stations',
    service: 'station-service', language: 'Java', database: 'PostgreSQL', owner: 'Member 2',
    intro: 'Stations record the observations. Delete their sensors before deleting a station.',
    fields: [
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'name', label: 'Name', type: 'text', required: true },
      city(),
      lat, lon,
      { key: 'altitude', label: 'Altitude', type: 'number', unit: 'm' },
      { key: 'installationDate', label: 'Installed on', type: 'date' },
      { key: 'status', label: 'Status', type: 'select', required: true, default: 'ACTIVE', options: ['ACTIVE', 'MAINTENANCE', 'INACTIVE'] },
    ],
    filter: cityFilter,
  },
  {
    key: 'sensors', title: 'Sensors', singular: 'sensor', endpoint: '/api/sensors',
    service: 'station-service', language: 'Java', database: 'PostgreSQL', owner: 'Member 2',
    intro: 'Each sensor belongs to one weather station.',
    fields: [
      { key: 'serialNumber', label: 'Serial number', type: 'text', required: true },
      { key: 'station', label: 'Station', type: 'ref', required: true, ref: { resource: 'stations', labelKey: 'name', asObject: true } },
      { key: 'type', label: 'Type', type: 'select', required: true, options: ['TEMPERATURE', 'HUMIDITY', 'PRESSURE', 'WIND_SPEED', 'WIND_DIRECTION', 'PRECIPITATION', 'UV'] },
      { key: 'unit', label: 'Unit', type: 'text' },
      { key: 'lastCalibrationDate', label: 'Last calibration', type: 'date' },
      { key: 'active', label: 'Active', type: 'checkbox', default: true },
    ],
    filter: { param: 'stationId', label: 'Station', ref: { resource: 'stations', labelKey: 'name', asObject: true } },
  },
  // ---------------------------------------------------------------- observation-service (Member 3)
  {
    key: 'observations', title: 'Observations', singular: 'observation', endpoint: '/api/observations',
    service: 'observation-service', language: 'Java', database: 'MongoDB', owner: 'Member 3',
    intro: 'Measurements recorded by a station at a given time.',
    fields: [
      { key: 'stationId', label: 'Station', type: 'ref', required: true, ref: { resource: 'stations', labelKey: 'name', asObject: false } },
      { key: 'observedAt', label: 'Observed at', type: 'datetime', required: true },
      { key: 'temperature', label: 'Temperature', type: 'number', unit: '°C' },
      { key: 'humidity', label: 'Humidity', type: 'number', unit: '%', min: 0, max: 100 },
      { key: 'pressure', label: 'Pressure', type: 'number', unit: 'hPa', min: 0 },
      { key: 'windSpeed', label: 'Wind', type: 'number', unit: 'km/h', min: 0 },
      { key: 'windDirection', label: 'Wind direction', type: 'number', unit: '°', min: 0, max: 360 },
      { key: 'precipitation', label: 'Rain', type: 'number', unit: 'mm', min: 0 },
    ],
    filter: { param: 'stationId', label: 'Station', ref: { resource: 'stations', labelKey: 'name', asObject: false } },
  },
  {
    key: 'air-quality', title: 'Air quality', singular: 'air quality measurement', endpoint: '/api/air-quality',
    service: 'observation-service', language: 'Java', database: 'MongoDB', owner: 'Member 3',
    intro: 'Air quality index (0 to 500) and pollutants per city.',
    fields: [
      city(),
      { key: 'measuredAt', label: 'Measured at', type: 'datetime', required: true },
      { key: 'aqi', label: 'AQI', type: 'number', required: true, min: 0, max: 500 },
      { key: 'level', label: 'Level', type: 'select', options: ['GOOD', 'MODERATE', 'UNHEALTHY_FOR_SENSITIVE', 'UNHEALTHY', 'VERY_UNHEALTHY', 'HAZARDOUS'] },
      { key: 'pm25', label: 'PM2.5', type: 'number', unit: 'µg/m³', min: 0 },
      { key: 'pm10', label: 'PM10', type: 'number', unit: 'µg/m³', min: 0 },
      { key: 'o3', label: 'O₃', type: 'number', unit: 'µg/m³', min: 0 },
      { key: 'no2', label: 'NO₂', type: 'number', unit: 'µg/m³', min: 0 },
    ],
    filter: cityFilter,
  },
  // ---------------------------------------------------------------- forecast-service (Member 4)
  {
    key: 'forecast-models', title: 'Forecast models', singular: 'forecast model', endpoint: '/api/forecast-models',
    service: 'forecast-service', language: 'Java', database: 'MongoDB', owner: 'Member 4',
    intro: 'The numerical models that produce the forecasts.',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'provider', label: 'Provider', type: 'text' },
      { key: 'version', label: 'Version', type: 'text' },
      { key: 'resolutionKm', label: 'Resolution', type: 'number', unit: 'km', min: 0 },
      { key: 'horizonDays', label: 'Horizon', type: 'number', unit: 'days', min: 1, max: 30 },
      { key: 'description', label: 'Description', type: 'textarea', inTable: false },
    ],
  },
  {
    key: 'forecasts', title: 'Forecasts', singular: 'forecast', endpoint: '/api/forecasts',
    service: 'forecast-service', language: 'Java', database: 'MongoDB', owner: 'Member 4',
    intro: 'Daily forecast for a city, produced by a forecast model.',
    fields: [
      city(),
      { key: 'forecastDate', label: 'Date', type: 'date', required: true },
      { key: 'condition', label: 'Condition', type: 'select', required: true, options: ['SUNNY', 'PARTLY_CLOUDY', 'CLOUDY', 'RAIN', 'THUNDERSTORM', 'SNOW', 'FOG', 'WINDY', 'SANDSTORM'] },
      { key: 'minTemperature', label: 'Min', type: 'number', unit: '°C' },
      { key: 'maxTemperature', label: 'Max', type: 'number', unit: '°C' },
      { key: 'precipitationProbability', label: 'Chance of rain', type: 'number', unit: '%', min: 0, max: 100 },
      { key: 'windSpeed', label: 'Wind', type: 'number', unit: 'km/h', min: 0 },
      { key: 'modelId', label: 'Model', type: 'ref', required: true, ref: { resource: 'forecast-models', labelKey: 'name', asObject: false } },
      { key: 'createdAt', label: 'Created', type: 'datetime', readOnly: true },
    ],
    filter: cityFilter,
  },
  // ---------------------------------------------------------------- alert-service (Member 5)
  {
    key: 'alerts', title: 'Alerts', singular: 'alert', endpoint: '/api/alerts',
    service: 'alert-service', language: 'Java', database: 'MySQL', owner: 'Member 5',
    intro: 'Weather warnings for a city, with vigilance level yellow, orange or red.',
    fields: [
      { key: 'title', label: 'Title', type: 'text', required: true },
      city(),
      { key: 'type', label: 'Type', type: 'select', required: true, options: ['HEAT_WAVE', 'STORM', 'HEAVY_RAIN', 'FLOOD', 'STRONG_WIND', 'SNOW', 'SANDSTORM', 'FOREST_FIRE_RISK'] },
      { key: 'severity', label: 'Severity', type: 'select', required: true, options: ['YELLOW', 'ORANGE', 'RED'] },
      { key: 'startTime', label: 'Starts', type: 'datetime', required: true },
      { key: 'endTime', label: 'Ends', type: 'datetime' },
      { key: 'active', label: 'Active', type: 'checkbox', default: true },
      { key: 'description', label: 'Description', type: 'textarea', inTable: false },
    ],
    filter: cityFilter,
  },
  {
    key: 'notifications', title: 'Notifications', singular: 'notification', endpoint: '/api/notifications',
    service: 'alert-service', language: 'Java', database: 'MySQL', owner: 'Member 5',
    intro: 'Messages sent to a user about an alert.',
    fields: [
      { key: 'alert', label: 'Alert', type: 'ref', required: true, ref: { resource: 'alerts', labelKey: 'title', asObject: true } },
      { key: 'userId', label: 'User', type: 'ref', required: true, ref: { resource: 'users', labelKey: 'email', asObject: false } },
      { key: 'channel', label: 'Channel', type: 'select', required: true, options: ['EMAIL', 'SMS', 'PUSH'] },
      { key: 'status', label: 'Status', type: 'select', required: true, default: 'PENDING', options: ['PENDING', 'SENT', 'FAILED', 'READ'] },
      { key: 'message', label: 'Message', type: 'textarea', required: true },
      { key: 'createdAt', label: 'Created', type: 'datetime', readOnly: true },
    ],
    filter: { param: 'userId', label: 'User', ref: { resource: 'users', labelKey: 'email', asObject: false } },
  },
  // ---------------------------------------------------------------- user-service (Member 6)
  {
    key: 'users', title: 'Users', singular: 'user', endpoint: '/api/users',
    service: 'user-service', language: 'Java', database: 'MySQL', owner: 'Member 6',
    intro: 'People who follow cities and receive alerts.',
    fields: [
      { key: 'firstName', label: 'First name', type: 'text', required: true },
      { key: 'lastName', label: 'Last name', type: 'text', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'preferredUnit', label: 'Unit', type: 'select', required: true, default: 'CELSIUS', options: ['CELSIUS', 'FAHRENHEIT'] },
      { key: 'role', label: 'Role', type: 'select', required: true, default: 'USER', options: ['USER', 'ADMIN'] },
      { key: 'createdAt', label: 'Joined', type: 'datetime', readOnly: true },
    ],
  },
  {
    key: 'favorites', title: 'Favorite places', singular: 'favorite place', endpoint: '/api/favorites',
    service: 'user-service', language: 'Java', database: 'MySQL', owner: 'Member 6',
    intro: 'Cities a user follows, with or without alerts.',
    fields: [
      { key: 'user', label: 'User', type: 'ref', required: true, ref: { resource: 'users', labelKey: 'email', asObject: true } },
      city(),
      { key: 'label', label: 'Label', type: 'text' },
      { key: 'alertsEnabled', label: 'Alerts on', type: 'checkbox', default: true },
      { key: 'addedAt', label: 'Added', type: 'datetime', readOnly: true },
    ],
    filter: { param: 'userId', label: 'User', ref: { resource: 'users', labelKey: 'email', asObject: true } },
  },
  // ---------------------------------------------------------------- report-service (Python, 7th microservice)
  {
    key: 'reports', title: 'Reports', singular: 'report', endpoint: '/api/reports',
    service: 'report-service', language: 'Python', database: 'PostgreSQL', owner: 'Team',
    intro: 'Create a report, then generate it: the service collects the data from the other services and writes a CSV or PDF.',
    fields: [
      { key: 'title', label: 'Title', type: 'text', required: true },
      city(),
      { key: 'reportType', label: 'Content', type: 'select', required: true, default: 'SUMMARY', options: ['SUMMARY', 'OBSERVATIONS', 'FORECASTS', 'ALERTS'] },
      { key: 'format', label: 'Format', type: 'select', required: true, default: 'PDF', options: ['PDF', 'CSV'] },
      { key: 'periodStart', label: 'From', type: 'date', required: true },
      { key: 'periodEnd', label: 'To', type: 'date', required: true },
      { key: 'scheduleId', label: 'Schedule', type: 'ref', ref: { resource: 'report-schedules', labelKey: 'name', asObject: false }, inTable: false },
      { key: 'status', label: 'Status', type: 'select', readOnly: true },
      { key: 'generatedAt', label: 'Generated', type: 'datetime', readOnly: true },
      { key: 'errorMessage', label: 'Error', type: 'text', readOnly: true, inTable: false },
    ],
    filter: cityFilter,
    actions: ['generate', 'download'],
  },
  {
    key: 'report-schedules', title: 'Report schedules', singular: 'schedule', endpoint: '/api/report-schedules',
    service: 'report-service', language: 'Python', database: 'PostgreSQL', owner: 'Team',
    intro: 'A schedule describes a recurring report. "Run now" creates and generates the report of its last period.',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true },
      city(),
      { key: 'reportType', label: 'Content', type: 'select', required: true, default: 'SUMMARY', options: ['SUMMARY', 'OBSERVATIONS', 'FORECASTS', 'ALERTS'] },
      { key: 'format', label: 'Format', type: 'select', required: true, default: 'PDF', options: ['PDF', 'CSV'] },
      { key: 'frequency', label: 'Frequency', type: 'select', required: true, default: 'WEEKLY', options: ['DAILY', 'WEEKLY', 'MONTHLY'] },
      { key: 'recipientEmail', label: 'Send to', type: 'email' },
      { key: 'active', label: 'Active', type: 'checkbox', default: true },
    ],
    filter: cityFilter,
    actions: ['run'],
  },
];

export interface NavGroup {
  label: string;
  service: string;
  items: EntityDef[];
}

const groupLabels: [string, string][] = [
  ['Places', 'geo-service'],
  ['Stations', 'station-service'],
  ['Measurements', 'observation-service'],
  ['Forecasts', 'forecast-service'],
  ['Alerts', 'alert-service'],
  ['People', 'user-service'],
  ['Reports', 'report-service'],
];

export const NAV_GROUPS: NavGroup[] = groupLabels.map(([label, service]) => ({
  label,
  service,
  items: ENTITIES.filter((e) => e.service === service),
}));

export function findEntity(key: string): EntityDef | undefined {
  return ENTITIES.find((e) => e.key === key);
}

const ACRONYMS = new Set(['PDF', 'CSV', 'SMS', 'UV']);

/** 'PARTLY_CLOUDY' -> 'Partly cloudy' (acronyms such as PDF stay as they are) */
export function humanize(value: unknown): string {
  if (value === null || value === undefined) return '';
  if (ACRONYMS.has(String(value))) return String(value);
  const text = String(value).replace(/_/g, ' ').toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}
