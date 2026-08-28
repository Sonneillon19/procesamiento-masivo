# Procesamiento Masivo de Transacciones

API REST desarrollada con Java 21, Spring Boot 3 y Spring Batch para procesar archivos CSV con transacciones.

## Requisitos

Para ejecutar el proyecto se necesita:

- JDK 21
- Maven
- Docker y Docker Compose
- Postman, Insomnia o una herramienta equivalente para consumir la API

## Base de datos

El proyecto utiliza PostgreSQL.

La base de datos puede levantarse mediante el archivo `docker-compose.yml` incluido en el proyecto:

```bash
docker compose up -d
```

La configuración utilizada por la aplicación se encuentra en:

```text
src/main/resources/application.properties
```

La estructura de las tablas de negocio se encuentra en:

```text
src/main/resources/schema.sql
```

El modelo contiene tres tablas principales:

- `transacciones`: almacena las transacciones procesadas correctamente.
- `lotes_procesamiento`: mantiene información de control y auditoría de cada archivo procesado.
- `errores_procesamiento`: almacena los registros inválidos junto con el motivo del error.

Spring Batch utiliza adicionalmente sus propias tablas de metadatos para mantener información sobre Jobs, Steps y ejecuciones.

## Ejecución

Primero iniciar PostgreSQL:

```bash
docker compose up -d
```

Después ejecutar la aplicación:

```bash
mvn spring-boot:run
```

La API estará disponible en:

```text
http://localhost:8080
```

## Procesamiento del archivo

El archivo CSV esperado utiliza la siguiente estructura:

```csv
id_transaccion,cuenta_origen,cuenta_destino,monto,fecha_hora,tipo_operacion
TX000001,CTA1001,CTA2001,1500.50,2026-08-27T10:00:00,TRANSFERENCIA
```

Los tipos de operación permitidos son:

- `TRANSFERENCIA`
- `PAGO`
- `DEPOSITO`
- `RETIRO`

Se incluye un archivo de ejemplo en:

```text
src/main/resources/docs/ejemplo_carga.csv
```

El archivo contiene tanto registros válidos como inválidos para comprobar la tolerancia a errores.


## API REST

### Procesar archivo

```http
POST /api/procesamientos
```

El archivo debe enviarse como `multipart/form-data` utilizando el parámetro:

```text
archivo
```

Ejemplo de respuesta:

```json
{
  "id": 1,
  "nombreArchivo": "ejemplo_carga.csv",
  "estado": "COMPLETADO",
  "totalRegistros": 20,
  "registrosExitosos": 15,
  "registrosFallidos": 5,
  "fechaInicio": "2026-08-27T10:00:00",
  "fechaFin": "2026-08-27T10:00:01"
}
```

### Consultar procesamiento

```http
GET /api/procesamientos/{id}
```

Permite consultar el estado y las estadísticas de un lote.

### Consultar errores

```http
GET /api/procesamientos/{id}/errores
```

Devuelve los registros que no pudieron procesarse correctamente junto con el motivo del error.

## Colección Postman

Se incluye una colección de Postman con ejemplos para consumir los endpoints de la API.

La colección se encuentra dentro de:

```text
src/main/resources/docs/
```

Importar el archivo `.postman_collection.json` en Postman y configurar el archivo CSV en la petición `POST /api/procesamientos`.

## Detener la base de datos

Para detener PostgreSQL:

```bash
docker compose down
```

Para eliminar también el volumen de datos:

```bash
docker compose down -v
```

## Estrategia de procesamiento
La estrategia utilizada para evitar que errores de datos detengan el proceso consiste en tratar cada registro de forma individual dentro del `ItemProcessor`. Cuando un registro contiene datos inválidos, no se lanza una excepción que detenga el Job; en su lugar, se genera un `ErrorProcesamiento` con el detalle del problema y el procesamiento continúa con los siguientes registros.

El procesamiento se realiza con Spring Batch utilizando el flujo:

```text
CSV --> ItemReader --> ItemProcesor --> PostgreSQL
```

El archivo se lee línea por línea, por lo que no es necesario cargarlo completamente en memoria.

Los registros se procesan en chunks de 100 elementos. Cada chunk funciona como una unidad transaccional.

El `ItemProcessor` valida y transforma cada registro. Entre las validaciones realizadas se encuentran:

- campos obligatorios;
- monto numérico y mayor a cero;
- formato de fecha;
- tipo de operación permitido.

El `ItemWriter` separa las transacciones válidas de los registros con error y utiliza operaciones JDBC batch para reducir la cantidad de operaciones individuales contra PostgreSQL.

También se detectan identificadores de transacción duplicados para evitar que un registro duplicado detenga el procesamiento completo.

## Tolerancia a errores y consistencia

Un registro inválido no detiene el procesamiento del archivo.

Cuando un registro no cumple las validaciones, se almacena en `errores_procesamiento` junto con:

- número de línea
- contenido original
- motivo del error

Los registros válidos continúan procesándose normalmente.

Spring Batch procesa los datos mediante chunks transaccionales. Si ocurre un error de infraestructura durante un chunk, ese chunk se revierte, mientras que los chunks confirmados anteriormente permanecen persistidos.

El Reader utiliza `ExecutionContext` para almacenar su avance y Spring Batch mantiene los metadatos de ejecución en PostgreSQL.

La tabla `lotes_procesamiento` permite consultar el resultado de cada procesamiento, incluyendo:

- estado
- total de registros
- registros exitosos
- registros fallidos
- fecha de inicio
- fecha de finalización.
