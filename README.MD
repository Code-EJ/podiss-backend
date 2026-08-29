# PodIss Backend

Backend service for the PodISS podcast platform.

## About

The PodISS Backend provides the server-side components required by the PodISS platform, including application logic, data persistence, web services, authentication, and integration with the platform's database.

The application is built with Java and Spring Boot and uses a relational database through Spring Data JPA.

## Technologies

* Java 21
* Spring Boot 3.4.1
* Spring Web
* Spring Data JPA
* Spring Security
* JSON Web Tokens (JWT)
* MariaDB
* H2 Database
* Lombok
* Maven

## Requirements

Before running the project, make sure the following software is installed:

* JDK 21
* Maven 3.9+
* MariaDB
* IntelliJ IDEA (optional)

Verify the Java installation:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

## Getting Started

Clone the repository:

```bash
git clone https://github.com/Code-EJ/pod-iss-backend.git
cd pod-iss-backend
```

## Environment Variables

The application uses environment variables for configuration. Create a `.env` file in the project root.

Example:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=podiss
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SECRET=your_secret
```

> The actual variables required by the application may vary depending on the active Spring profile.

The `.env` file may contain sensitive information and **must not be committed to the repository**.

Make sure `.env` is included in `.gitignore`:

```gitignore
.env
```

## Running the Application

There are two recommended ways to run the application locally.

### Option 1 — IntelliJ IDEA

IntelliJ IDEA does not automatically load a `.env` file into the application's environment in every configuration. The recommended approach is to configure the `.env` file in the Run/Debug Configuration.

#### Step 1 — Open the project

Open the project directory in IntelliJ IDEA.

Make sure IntelliJ detects:

* Java 21
* Maven
* The project's `pom.xml`

#### Step 2 — Create a Run Configuration

Open:

```text
Run → Edit Configurations...
```

Click:

```text
+ → Spring Boot
```

Configure the application:

* **Name:** `PodIss Backend`
* **Main class:** select the project's Spring Boot main class
* **Use classpath of module:** select the backend module

Set the active Spring profile to `dev`.

In **Active profiles**, enter:

```text
dev
```

#### Step 3 — Configure the `.env` file

If your IntelliJ IDEA version supports `.env` files directly, locate the **Environment variables** section and configure the `.env` file as the environment source.

Alternatively, if the IDE provides the **EnvFile** option, enable:

```text
Enable EnvFile
```

Then add the project's `.env` file:

```text
.env
```

The resulting configuration should load the variables defined in:

```text
<project-root>/.env
```

For example:

```text
pod-iss-backend/
├── .env
├── pom.xml
├── src/
└── ...
```

#### Step 4 — Run the application

Click **Run** ▶️ in IntelliJ IDEA.

The application will start using:

```text
Spring profile: dev
Environment variables: .env
```

You can also use **Debug** if you need to debug the application.

### Option 2 — Maven from the Terminal

The project can also be started directly through Maven.

#### Linux / macOS / Git Bash

From the project root:

```bash
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or as a single command:

```bash
export $(grep -v '^#' .env | xargs) && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Windows PowerShell

PowerShell does not support the `export` command used above.

You can load the `.env` variables and start Maven with:

```powershell
Get-Content .env | Where-Object { $_ -and $_ -notmatch '^#' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name, $value)
}

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

If your `.env` contains values with spaces or more complex shell syntax, configure the environment variables directly in the terminal or IntelliJ instead.

#### Windows CMD

CMD does not have native `.env` loading. The recommended approach on Windows is to use IntelliJ IDEA, PowerShell, Git Bash, or another shell capable of loading the variables.

## Build the Project

To compile and package the application:

```bash
mvn clean install
```

To perform a complete build including tests:

```bash
mvn clean verify
```

## Testing

Run the test suite with:

```bash
mvn test
```

## Running with a Specific Spring Profile

The development profile can be explicitly selected with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

When using IntelliJ IDEA, configure the active profile as:

```text
dev
```

## Configuration

Environment-specific configuration should not be committed to the repository when it contains sensitive information.

Credentials, authentication secrets, database passwords, and other sensitive configuration values must be provided through environment variables or external configuration mechanisms.

The recommended local development setup is:

```text
.env
   ↓
Environment variables
   ↓
Spring Boot
   ↓
application-dev configuration
```

Never commit real database credentials, JWT secrets, API keys, or other sensitive values.

## Versioning

This project follows Semantic Versioning:

```text
MAJOR.MINOR.PATCH
```

For example:

```text
1.0.0
1.1.0
1.1.1
```

* **MAJOR** — incompatible or breaking changes.
* **MINOR** — new functionality that remains backward compatible.
* **PATCH** — backward-compatible bug fixes.

## Contributing

This repository is maintained by Code Soluções em Tecnologia Júnior.

Access to the source code does not grant permission to copy, modify, distribute, sublicense, publish, or otherwise use the software outside the scope expressly authorized by Code Soluções em Tecnologia Júnior or the applicable project agreement.

Contributions must follow the project's development standards and may require prior authorization from the project maintainers.

## License

This project is proprietary software.

Unauthorized use, reproduction, distribution, or modification is prohibited.

See the `LICENSE` file for the applicable terms.

## Maintainers

### **Code Soluções em Tecnologia Júnior**

* Enzo Rocha Leite Diniz Ribas — Developer, Tech Lead, and Chief Information and Technology Officer.
