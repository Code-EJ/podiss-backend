# PodISS Backend

Backend service for the PodISS podcast platform.

## About

The PodISS Backend provides the server-side components required by the PodISS platform, including application logic, data persistence, web services, authentication, and integration with the platform's database.

The application is built with Java and Spring Boot and uses a relational database through Spring Data JPA.

## Technologies

- Java 21
- Spring Boot 3.4.1
- Spring Web
- Spring Data JPA
- Spring Security
- JSON Web Tokens (JWT)
- MariaDB
- H2 Database
- Lombok
- Maven

## Requirements

Before running the project, make sure the following software is installed:

- JDK 21
- Maven 3.9+
- MariaDB

Verify the Java installation with:

```bash
java -version
```

### Verify Maven with:

```bash
mvn -version
```

## Getting Started

Clone the repository:

```bash
git clone https://github.com/Code-EJ/pod-iss-backend.git
cd pod-iss-backend
```

### Build the project:

```bash
mvn clean install
```

### Run the application:
```bash
mvn spring-boot:run
```

## Testing

Run the test suite with:

```bash
mvn test
```

To perform a complete build including tests:

```bash
mvn clean verify
```

## Configuration

Environment-specific configuration should not be committed to the repository when it contains sensitive information.

Credentials, authentication secrets, database passwords, and other sensitive configuration values must be provided through appropriate environment variables or external configuration mechanisms.

## Versioning

This project follows Semantic Versioning:

MAJOR.MINOR.PATCH

For example:
```
1.0.0
1.1.0
1.1.1
```

- MAJOR — incompatible or breaking changes.
- MINOR — new functionality that remains backward compatible.
- PATCH — backward-compatible bug fixes.

## Contributing

This repository is maintained by Code Soluções em Tecnologia Júnior.

Access to the source code does not grant permission to copy, modify, distribute, sublicense, publish, or otherwise use the software outside the scope expressly authorized by Code Soluções em Tecnologia Júnior or the applicable project agreement.

Contributions must follow the project's development standards and may require prior authorization from the project maintainers.

## License

This project is proprietary software.

Unauthorized use, reproduction, distribution, or modification is prohibited.

See the LICENSE file for the applicable terms.

## Maintainers

### **Code Soluções em Tecnologia Júnior**

- Enzo Rocha Leite Diniz Ribas — Developer, Tech Lead, and Chief Information and Technology Officer.