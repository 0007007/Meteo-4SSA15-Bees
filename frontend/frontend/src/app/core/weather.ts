/** Presentation helpers for weather values (icons, sky colour, dates). */

const ICONS: Record<string, string> = {
  SUNNY: '☀️',
  PARTLY_CLOUDY: '⛅',
  CLOUDY: '☁️',
  RAIN: '🌧️',
  THUNDERSTORM: '⛈️',
  SNOW: '❄️',
  FOG: '🌫️',
  WINDY: '💨',
  SANDSTORM: '🌪️',
};

export function conditionIcon(condition: unknown): string {
  return ICONS[String(condition)] ?? '🌡️';
}

/** Value of data-sky on the sky panel; the CSS picks the gradient. */
export function skyOf(condition: unknown): string {
  const value = String(condition ?? '');
  return value in ICONS ? value.toLowerCase().replace('_', '-') : 'none';
}

const SEVERITY_ORDER: Record<string, number> = { RED: 0, ORANGE: 1, YELLOW: 2 };

export function bySeverity(a: Record<string, any>, b: Record<string, any>): number {
  return (SEVERITY_ORDER[a['severity']] ?? 3) - (SEVERITY_ORDER[b['severity']] ?? 3);
}

/** Today as YYYY-MM-DD in the browser's time zone. */
export function todayIso(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/** '2026-11-10T14:05:00' -> '10 Nov, 14:05' */
export function shortDateTime(value: unknown): string {
  if (!value) return '';
  const text = String(value);
  const date = new Date(text.length > 16 ? text.slice(0, 19) : text);
  if (Number.isNaN(date.getTime())) return text.replace('T', ' ').slice(0, 16);
  const day = date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
  const time = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
  return `${day}, ${time}`;
}

/** '2026-11-11' -> 'Today' or 'Wed 11 Nov' */
export function dayLabel(isoDate: unknown): string {
  const text = String(isoDate ?? '');
  if (text === todayIso()) return 'Today';
  const date = new Date(`${text}T12:00:00`);
  if (Number.isNaN(date.getTime())) return text;
  return date.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' });
}

export function rounded(value: unknown): string {
  return value === null || value === undefined || value === '' ? '–' : String(Math.round(Number(value)));
}
