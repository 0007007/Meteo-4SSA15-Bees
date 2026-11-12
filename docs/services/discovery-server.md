discovery-server is the service registry (Netflix Eureka). Every service registers its name and address at startup and renews it every 30 seconds; the gateway asks the registry where to send each request. It must start first.

## At a glance

| Item | Value |
| --- | --- |
| Owner | Shared (team) |
| Stack | Java 21, Spring Boot 4.0.8, Spring Cloud Netflix Eureka Server |
| Port | 8761 |
| Database | none: the registry lives in memory and is rebuilt as services register |
| Dashboard | `http://localhost:8761` |
| Base package | `com.meteo.discovery` |

## What registers

Eight applications appear on the dashboard when everything runs. The registry does not register itself.

| Name on the dashboard | Service | Port | Registered by |
| --- | --- | --- | --- |
| `GEO-SERVICE` | geo-service | 8081 | Spring Eureka client |
| `STATION-SERVICE` | station-service | 8082 | Spring Eureka client |
| `OBSERVATION-SERVICE` | observation-service | 8083 | Spring Eureka client |
| `FORECAST-SERVICE` | forecast-service | 8084 | Spring Eureka client |
| `ALERT-SERVICE` | alert-service | 8085 | Spring Eureka client |
| `USER-SERVICE` | user-service | 8086 | Spring Eureka client |
| `REPORT-SERVICE` | report-service | 8087 | py-eureka-client (Python) |
| `API-GATEWAY` | api-gateway | 8090 | Spring Eureka client |

## How registration works

1. At startup, each service sends its name, host, port and health URL to `http://localhost:8761/eureka/`.
2. Every 30 seconds it renews its lease (heartbeat).
3. An instance that stops renewing is removed after its lease expires (90 seconds by default).
4. The gateway keeps a copy of the registry and refreshes it every 30 seconds, so a new service can take up to a minute to be reachable through 8090.

## Configuration

`src/main/resources/application.yml`:

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false   # the registry does not register itself
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    enable-self-preservation: false   # dev only: removes stopped services quickly
```

| Setting | Why |
| --- | --- |
| `register-with-eureka: false` | the registry does not list itself |
| `fetch-registry: false` | it has no need for a copy of its own registry |
| `enable-self-preservation: false` | development only: stopped services disappear quickly; keep it `true` in production |
| `defaultZone` | the URL every client uses, also set in each service as `EUREKA_URL` |

## Troubleshooting

- A service is missing from the dashboard: check its `EUREKA_URL`, then its logs; clients retry on their own once the registry is up.
- The dashboard shows a red warning that self-preservation is turned off: expected with this development setting.
- The gateway answers 503 right after a service starts: wait for the registration and the gateway's next refresh, up to about a minute.

## Code structure

The whole server is one annotated class, `@EnableEurekaServer`, plus its configuration.

```
discovery-server/
├── src/
│   └── main/
│       ├── java/com/meteo/discovery/
│       │   └── DiscoveryServerApplication.java
│       └── resources/
│           └── application.yml
└── pom.xml
```

## Run and test

1. Run `DiscoveryServerApplication` from IntelliJ, or `mvn -pl discovery-server spring-boot:run` from the project root, before any other service.
2. Open `http://localhost:8761`: the "Instances currently registered" list is empty at first.
3. Start the other services; each name appears with its status UP within about 30 seconds.
