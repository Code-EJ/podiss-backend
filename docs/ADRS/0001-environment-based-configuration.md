# ADR-0001: Environment-Based Configuration

**Status:** Accepted  
**Date:** 2026-08-29

## Context

The PodIss backend requires different configurations for development and production environments.

These environments have different requirements for database access, SSL, CORS, logging, and database schema management.

Previously, environment-specific configuration and sensitive credentials were stored directly in the application configuration. This approach increases security risks and makes the application harder to configure and deploy across different environments.

Sensitive information such as database credentials and SSL credentials must not be stored in source code or committed to the repository.

## Decision

The project will use Spring Boot profiles and environment variables to manage environment-specific configuration.

The configuration will be divided into the following files:

    src/main/resources/
    ├── application.yaml
    ├── application-dev.yml
    └── application-prod.yml

### Shared Configuration

The `application.yaml` file contains configuration shared across all environments.

It must not contain environment-specific secrets or credentials.

### Development Configuration

The `application-dev.yml` file contains configuration used during local development.

The development environment may use:

- Local frontend origins.
- Development database configuration.
- HTTP communication.
- `ddl-auto: update`.
- SQL logging.

The development profile can be activated with:

    mvn spring-boot:run -Dspring-boot.run.profiles=dev

When using IntelliJ IDEA, the `dev` profile can be configured through the Run/Debug Configuration using the following VM option:

    -Dspring-boot.run.profiles=dev

Environment variables can be loaded locally through a `.env` file using the EnvFile plugin.

### Production Configuration

The `application-prod.yml` file contains configuration required by the production environment.

Production configuration includes:

- HTTPS.
- External SSL keystore configuration.
- Production database configuration.
- `ddl-auto: validate`.
- Disabled SQL logging.
- Configurable CORS origins.

The production profile can be activated with:

    mvn spring-boot:run -Dspring-boot.run.profiles=prod

Production environment variables must be provided by the deployment environment.

## Environment Variables

Sensitive and environment-specific values are provided through environment variables.

The application currently uses the following variables:

    DB_URL
    DB_USERNAME
    DB_PASSWORD

    SSL_KEY_STORE
    SSL_KEY_STORE_PASSWORD
    SSL_KEY_ALIAS

    CORS_ALLOWED_ORIGINS

These variables must be configured externally and must not contain hardcoded credentials in the repository.

## Local Development

For local development, environment variables may be stored in a `.env` file.

The `.env` file is intended only for local development and must not be committed to Git.

A `.env.example` file should be provided as a template containing the required variables without their actual values.

Example:

    DB_URL=
    DB_USERNAME=
    DB_PASSWORD=

    SSL_KEY_STORE=
    SSL_KEY_STORE_PASSWORD=
    SSL_KEY_ALIAS=server

    CORS_ALLOWED_ORIGINS=http://localhost:5173

## Production Environment

Production secrets must be provided by the deployment environment.

The production server should define the required environment variables independently from the Git repository.

SSL keystores and other sensitive files must also be managed outside version control.

## Security

The following types of files must not be committed when they contain sensitive information:

    .env
    .env.*
    *.p12
    *.pfx
    *.jks

Sensitive credentials must never be hardcoded in Java source files, Spring configuration files, or other version-controlled files.

If a credential is accidentally committed, it must be considered compromised and should be rotated.

## Consequences

### Positive

- Sensitive credentials are removed from source configuration.
- Development and production configurations are clearly separated.
- The same application can run in different environments without modifying the source code.
- Production configuration can be managed independently from the repository.
- Local development configuration can remain outside version control.
- The configuration strategy is documented and reproducible.

### Negative

- Developers must configure the required environment variables before running the application.
- Missing environment variables can prevent the application from starting.
- Production deployment requires correct environment configuration.
- Developers need to understand which Spring profile is active.

## Alternatives Considered

### Single Configuration File

A single configuration file containing both development and production settings was rejected.

This approach mixes environment-specific concerns and increases the risk of exposing production configuration.

### Hardcoded Credentials

Storing database credentials, SSL passwords, or other secrets directly in configuration files was rejected.

Sensitive credentials should not be version-controlled.

### Separate Git Branches for Configuration

Maintaining different configuration files through separate Git branches was rejected.

Environment configuration should be determined by the runtime environment rather than by the source-code branch.

## Result

Spring Boot profiles and environment variables are established as the standard configuration strategy for the PodIss backend.

The `dev` and `prod` profiles provide environment-specific configuration, while sensitive values are supplied externally through environment variables.