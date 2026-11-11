# Meteo App - frontend (Angular 21)

The web app of the project. It only talks to the **API Gateway** (port 8090), never to a microservice
directly: `npm start` proxies every `/api/**` call to `http://localhost:8090` (see `proxy.conf.json`).

## Run
```bash
cd frontend
npm install
npm start          # http://localhost:4200
```
Needs Node.js 20.19+, 22.12+ or 24. Start the gateway and the microservices first.

## Pages
| Route | What it shows | Data from |
|---|---|---|
| `/` | Weather of a city: current reading, alerts, next days, stations | geo, station, observation, forecast, alert services |
| `/manage/:resource` | List + add / edit / delete for any of the 14 entities | the service that owns the entity |

`/manage/reports` also has **Generate** and **Download**, and `/manage/report-schedules` has **Run now**
(Python report-service).

## Where to change things
```
src/app/
|-- core/entities.ts       one description per entity: fields, labels, dropdowns, filters
|-- core/crud.service.ts   generic REST calls (list, get, create, update, delete, actions)
|-- core/weather.ts        icons, sky colour, date formatting
|-- pages/dashboard/       the weather page
`-- pages/crud-page/       the generic list + form page used by all 14 entities
```
Each member can adjust the 2 entries of their service in `core/entities.ts` (for example add a field
after adding it to the Java entity). A dedicated page can replace the generic one later by adding a route
in `app.routes.ts` before `manage/:resource`.
