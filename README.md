# TitanAxis

TitanAxis is a desktop application that relies on a MariaDB database. Connection details are now supplied at runtime from environment variables or from `src/main/resources/config.properties` when the variables are not present.
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Prerequisites

- Java 21
- Maven 3+


## Environment variables

Set the following variables to configure the application:

- `DATABASE_URL` – JDBC connection URL
- `DATABASE_USER` – database user
- `DATABASE_PASSWORD` – database user's password
- `ADMIN_PASSWORD` – initial password for the built-in `admin` account

These variables can be placed in a `.env` file or configured in your deployment environment. When not provided, the values defined in `config.properties` are used. The `persistence.xml` file no longer stores credentials so they must be provided by one of these methods.

## Running with Docker

The project includes a `docker-compose.yml` that starts a MariaDB instance. You can create a `.env` file based on `.env.sample` to override the default credentials.
After starting the database you can run the shaded JAR locally:

```bash
docker compose up -d
java -jar target/TitanAxis-1.0-SNAPSHOT-shaded.jar
```
## Building with Maven

Compile and package the application using:

```bash
mvn clean package
```

This will also execute the unit tests.

The command above produces a shaded JAR under `target/`. Run the
application with:

```bash
java -jar target/TitanAxis-1.0-SNAPSHOT-shaded.jar
```

## Running tests

To run the JUnit tests only:

```bash
mvn test
```

The `docker-compose.yml` can be used to start a local MariaDB instance as described above.

