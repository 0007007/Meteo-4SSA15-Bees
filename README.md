# Meteo App - microservices (Spring Boot + Python + Angular)

Seven microservices behind a Spring Cloud Gateway, all registered in a Eureka server, and an Angular
frontend. Six services are Spring Boot (Java), the 7th (`report-service`) is Python/FastAPI.
Port **8080 is never used** (reserved for Oracle).

| Member | Module | Entities | Database | Port |
|---|---|---|---|---|
| Hamza Najjar | `geo-service` | Country, City | PostgreSQL (`geo_db`) | 8081 |
| Aziz Kchaou | `station-service` | WeatherStation, Sensor | PostgreSQL (`station_db`) | 8082 |
| Azer Slimani | `observation-service` | Observation, AirQuality | MongoDB (`observation_db`) | 8083 |
| Ghassen Saidani | `forecast-service` | ForecastModel, Forecast | MongoDB (`forecast_db`) | 8084 |
| Tasnim Samoud | `alert-service` | Alert, Notification | MySQL (`alert_db`) | 8085 |
| Yasmine Hammouda | `user-service` | User, FavoriteLocation | MySQL (`user_db`) | 8086 |
| Team | `report-service` (Python) | Report, ReportSchedule | PostgreSQL (`report_db`) | 8087 |
| Shared | `api-gateway` | - | - | 8090 |
| Shared | `discovery-server` | - | - | 8761 |
| Shared | `frontend` (Angular) | - | - | 4200 |

## Stack
- Java 21 - Spring Boot 4.0.8 - Spring Cloud 2025.1.3 (Oakwood) - Maven multi-module
- Python 3.11+ - FastAPI - SQLAlchemy - reportlab - py-eureka-client (`report-service/`)
- Angular 21 (standalone components, signals) - Node.js 20.19+, 22.12+ or 24 (`frontend/`)

Spring Boot 4 notes: the web starter is `spring-boot-starter-webmvc` (was `spring-boot-starter-web`),
MongoDB properties are `spring.mongodb.*` (were `spring.data.mongodb.*`), and the Eureka client needs
`spring-boot-starter-restclient` to register.

## Run
1. `docker compose up -d` - PostgreSQL :5432, MySQL :3306, MongoDB :27017 and the 7 databases
   (user `meteo` / password `meteo`). Using your own local MySQL/PostgreSQL instead? Change
   `username`/`password` in the service's `application.yml` (or set `DB_USERNAME` / `DB_PASSWORD`).
2. Run `discovery-server` -> http://localhost:8761
3. Run the 6 Spring Boot services (any order). Each appears in the Eureka dashboard.
4. Run `report-service`: see `report-service/README.md` (`pip install -r requirements.txt`, then `python -m app.main`).
5. Run `api-gateway` -> every endpoint is on http://localhost:8090/api/...
6. Run the frontend: `cd frontend && npm install && npm start` -> http://localhost:4200
7. Postman: import `postman/meteo-app.postman_collection.json` and run the folders top to bottom.

From a terminal: `mvn -pl geo-service spring-boot:run` (same for each Java module), or
`mvn clean package` at the root to build every jar.

## Package layout (same in every Java service)
```
com.meteo.<service>
|-- controller   REST endpoints (@RestController)
|-- service      interface + impl/ (business rules)
|-- repository   JpaRepository or MongoRepository
|-- entity       @Entity classes (MySQL/PostgreSQL)  - or document/ for MongoDB
`-- exception    ResourceNotFoundException + GlobalExceptionHandler
```

## Documentation per microservice
Each module has its own `README.md`: entities and fields, REST endpoints with examples, rules and error
codes, links with other services, configuration, code structure, run and test. The same pages are
collected in `docs/services/`.

## Diagrams
`docs/diagrams/` holds the analysis class diagram (+ enumerations), the logical architecture and the physical
architecture as PNG and SVG, plus their sources (`.mmd` Mermaid, `.dot` Graphviz).
