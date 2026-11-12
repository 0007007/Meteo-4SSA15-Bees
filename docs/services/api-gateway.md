api-gateway is the single entry point of the backend. It receives every request on port 8090, matches the path to one of seven routes and forwards it to a live instance found in Eureka. Clients, the Angular app included, never call a business service directly.

## At a glance

| Item | Value |
| --- | --- |
| Owner | Shared (team) |
| Stack | Java 21, Spring Boot 4.0.8, Spring Cloud Gateway (reactive, WebFlux), Eureka client, Actuator |
| Port | 8090 (8080 is left to Oracle) |
| Database | none |
| Eureka name | `api-gateway` |
| Base package | `com.meteo.gateway` |

## Routes

| Route id | Paths | Target |
| --- | --- | --- |
| geo-service | `/api/countries/**`, `/api/cities/**` | `lb://geo-service` |
| station-service | `/api/stations/**`, `/api/sensors/**` | `lb://station-service` |
| observation-service | `/api/observations/**`, `/api/air-quality/**` | `lb://observation-service` |
| forecast-service | `/api/forecast-models/**`, `/api/forecasts/**` | `lb://forecast-service` |
| alert-service | `/api/alerts/**`, `/api/notifications/**` | `lb://alert-service` |
| user-service | `/api/users/**`, `/api/favorites/**` | `lb://user-service` |
| report-service | `/api/reports/**`, `/api/report-schedules/**` | `lb://report-service` |

`lb://` means load-balanced through Eureka: the gateway asks the registry for the instances of that name and picks one, so a second instance of a service is used with no change here. The path is forwarded as it is: `GET /api/cities/3` on 8090 reaches `GET /api/cities/3` on geo-service. A route matches `/api/cities` and every path below it.

## CORS

The gateway allows requests from `http://localhost:4200` with any method and header. During `npm start` the Angular dev server already forwards `/api` to 8090, so the browser sees one origin; the CORS rule matters when the built app is served from another address.

## Errors

Business errors (400, 404, 409) come from the services and pass through unchanged. The gateway adds its own:

| Status | When |
| --- | --- |
| 404 | no route matches the path, for example `/api/unknown` |
| 503 | the route matches but Eureka has no instance of the target service: it is stopped or still starting |

## Configuration

`src/main/resources/application.yml`:

```yaml
server:
  port: 8090   # 8080 is left to Oracle

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: geo-service
              uri: lb://geo-service
              predicates:
                - Path=/api/countries/**,/api/cities/**
            - id: station-service
              uri: lb://station-service
              predicates:
                - Path=/api/stations/**,/api/sensors/**
            - id: observation-service
              uri: lb://observation-service
              predicates:
                - Path=/api/observations/**,/api/air-quality/**
            - id: forecast-service
              uri: lb://forecast-service
              predicates:
                - Path=/api/forecast-models/**,/api/forecasts/**
            - id: alert-service
              uri: lb://alert-service
              predicates:
                - Path=/api/alerts/**,/api/notifications/**
            - id: user-service
              uri: lb://user-service
              predicates:
                - Path=/api/users/**,/api/favorites/**
            - id: report-service          # 7th microservice, Python (FastAPI) - registered in Eureka
              uri: lb://report-service      # if Eureka registration fails, use: http://localhost:8087
              predicates:
                - Path=/api/reports/**,/api/report-schedules/**
          globalcors:
            cors-configurations:
              '[/**]':
                allowed-origins: "http://localhost:4200"   # Angular frontend (npm start)
                allowed-methods: "*"
                allowed-headers: "*"

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
```

| Variable | Default | Used for |
| --- | --- | --- |
| EUREKA_URL | http://localhost:8761/eureka/ | registry address |

To add a route for a new service, copy one entry under `routes`, give it a new `id`, its paths and `lb://<spring.application.name of the service>`.

## Monitoring

- `http://localhost:8090/actuator/health` returns `{"status":"UP"}`.
- `http://localhost:8090/actuator/gateway/routes` lists the routes the gateway has loaded.

## Code structure

The gateway has no controller: the routes in `application.yml` are the whole behaviour.

```
api-gateway/
├── src/
│   └── main/
│       ├── java/com/meteo/gateway/
│       │   └── ApiGatewayApplication.java
│       └── resources/
│           └── application.yml
└── pom.xml
```

## Run and test

1. Start discovery-server and at least one business service.
2. Run `ApiGatewayApplication` from IntelliJ, or `mvn -pl api-gateway spring-boot:run` from the project root. Start it last, so the services are already registered.
3. Call `GET http://localhost:8090/api/countries`: the answer is the same as `GET http://localhost:8081/api/countries`.
4. Stop geo-service and call again: once Eureka has removed it, which can take a minute or two, the gateway answers 503.
