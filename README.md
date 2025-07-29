# TitanAxis

TitanAxis is a desktop application that relies on a MariaDB database. The application will read connection details from the environment when available and fall back to `src/main/resources/config.properties` otherwise.

## Environment variables

Set the following variables to configure the database connection:

- `DATABASE_URL` – JDBC connection URL
- `DATABASE_USER` – database user
- `DATABASE_PASSWORD` – database user's password

These variables can be placed in a `.env` file or configured in your deployment environment. When not provided, the values defined in `config.properties` are used.

## Running with Docker

The project includes a `docker-compose.yml` that starts a MariaDB instance. You can create a `.env` file based on `.env.sample` to override the default credentials.

```bash
docker compose up -d
```
