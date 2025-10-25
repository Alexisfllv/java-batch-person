# Proyecto Spring Batch - Importación CSV a H2 (Código comentado y mejorado)

Este archivo contiene **todo el código** que me proporcionaste, **ordenado**, **mejorado** y **comentado línea por línea** siguiendo buenas prácticas.  
Está en español y listo para copiar/pegar o descargar.

---

## 1) `application.properties`  
Configuración de la aplicación y la base de datos H2 en memoria.

```properties
# Nombre de la aplicación (útil para logs y métricas)
spring.application.name=SpringBatchDemoV1

# H2 Console (consola web para inspeccionar la BD en memoria)
spring.h2.console.enabled=true
# Ruta donde estará accesible la consola H2
spring.h2.console.path=/h2-console

# Configuración de la fuente de datos (H2 en memoria)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=root
spring.datasource.password=root

# JPA / Hibernate: crear esquema al iniciar y eliminar al cerrar
spring.jpa.hibernate.ddl-auto=create-drop
# Mostrar SQLs en consola (útil para debugging)
spring.jpa.show-sql=true

# Evitar que Spring Batch ejecute jobs automáticamente al arrancar la app
# Lo dejaremos en false para ejecutar el job manualmente desde un endpoint.
spring.batch.job.enabled=false
```

---

## 2) Entidad `Person`  
Entidad JPA que mapea una fila del CSV a una tabla en H2.

> NOTA: He añadido `@Table` y `@Column` opcionales para mayor claridad, y un constructor vacío y uno con campos si lo deseas (buenas prácticas), además de `toString()` para logging. Si no quieres estas anotaciones explícitas, puedes eliminarlas.

```java
package hub.com.springbatchdemov1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Entidad JPA que representa una fila del CSV y será persistida en H2.
 *
 * - Se usa GenerationType.IDENTITY para que el id se genere automáticamente.
 * - Los nombres de columnas son opcionales, aquí se muestran para documentación.
 */
@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Person {

    // Identificador primario (auto-incremental)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Los siguientes campos corresponden a las columnas del CSV.
    // Se usan tipos String para alinearse con el CSV original.
    // Si quisieras, podrías mapear dateOfBirth a LocalDate y añadir conversores.
    @Column(name = "csv_index")
    private String index;      // valor de la columna "index" del CSV

    @Column(name = "user_id")
    private String userId;     // valor de la columna "userId" del CSV

    @Column(name = "first_name")
    private String firstName;  // valor de la columna "firstName"

    @Column(name = "last_name")
    private String lastName;   // valor de la columna "lastName"

    @Column(name = "gender")
    private String gender;     // valor de la columna "gender"

    @Column(name = "email")
    private String email;      // valor de la columna "email"

    @Column(name = "phone")
    private String phone;      // valor de la columna "phone"

    @Column(name = "date_of_birth")
    private String dateOfBirth; // valor de la columna "dateOfBirth"

    @Column(name = "job_title")
    private String jobTitle;   // valor de la columna "jobTitle"
}
```

---

## 3) Repositorio `PersonRepository`  
Interfaz Spring Data JPA para operaciones CRUD y consultas específicas.

```java
package hub.com.springbatchdemov1.repository;

import hub.com.springbatchdemov1.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Person.
 * - Extiende JpaRepository para tener métodos CRUD.
 * - Se definen métodos derivados para obtener la primera o la última persona.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Retorna la primera persona registrada en la BD ordenando por ID ascendente.
     * - Útil para devolver la "primera entidad guardada".
     */
    Person findFirstByOrderByIdAsc();

    /**
     * Retorna la última persona registrada en la BD ordenando por ID descendente.
     * - Útil si quieres la más reciente.
     */
    Person findTopByOrderByIdDesc();
}
```

---

## 4) Processor `PersonProcessor`  
Clase que implementa `ItemProcessor` y transforma datos antes de persistir.

```java
package hub.com.springbatchdemov1.batch;

import hub.com.springbatchdemov1.entity.Person;
import org.springframework.batch.item.ItemProcessor;

/**
 * PersonProcessor: ejemplo de procesamiento por registro.
 * - Convierte firstName y lastName a mayúsculas.
 * - Si quisieras validar o filtrar registros, podrías devolver `null` para
 *   indicarle a Spring Batch que descarte ese item.
 */
public class PersonProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) throws Exception {
        if (person == null) {
            // Si el item es nulo, retornamos null (aunque normalmente el reader no entrega null)
            return null;
        }

        // Defensive programming: verificamos valores nulos antes de operar
        if (person.getFirstName() != null) {
            person.setFirstName(person.getFirstName().toUpperCase());
        }

        if (person.getLastName() != null) {
            person.setLastName(person.getLastName().toUpperCase());
        }

        // Aquí podrías agregar validaciones, normalizaciones o enriquecimiento.
        return person;
    }
}
```

---

## 5) Configuración de Spring Batch (`SpringBatchConfig`)  
Define `Reader`, `LineMapper`, `Processor`, `Writer`, `Step` y `Job`. Comentarios detallados en cada parte.

```java
package hub.com.springbatchdemov1.config;

import hub.com.springbatchdemov1.entity.Person;
import hub.com.springbatchdemov1.repository.PersonRepository;
import hub.com.springbatchdemov1.batch.PersonProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.support.RepositoryItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración de Spring Batch.
 * - Crea los beans necesarios para leer un CSV, procesarlo y persistirlo con JPA.
 * - Se utiliza un chunk-oriented step con tamaño 10.
 */
@Configuration
@RequiredArgsConstructor
public class SpringBatchConfig {

    // Inyectamos el repositorio para usarlo en el writer.
    private final PersonRepository personRepository;

    /**
     * Bean Reader: FlatFileItemReader lee líneas de un recurso (CSV).
     * - Se setea el recurso desde classpath: resources/people-1000.csv
     * - linesToSkip(1) salta la primera línea (header)
     * - lineMapper define cómo transformar cada línea en un objeto Person
     */
    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("people-1000.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper())
                .targetType(Person.class)
                .build();
    }

    /**
     * LineMapper: define tokenización y mapeo de campos CSV -> Person.
     * - DelimitedLineTokenizer separa por comas.
     * - BeanWrapperFieldSetMapper mapea automáticamente los campos por nombre
     *   a los setters de la clase Person.
     */
    private LineMapper<Person> lineMapper() {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(","); // CSV separado por comas
        lineTokenizer.setStrict(false);  // permitir líneas con menos campos sin fallar
        // Nombres de columnas en el mismo orden que el CSV
        lineTokenizer.setNames("index", "userId", "firstName", "lastName",
                "gender", "email", "phone", "dateOfBirth", "jobTitle");

        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        // Indicamos a FieldSetMapper que el objetivo es la clase Person
        fieldSetMapper.setTargetType(Person.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    /**
     * Bean Processor: instancia del PersonProcessor.
     * - Se define como bean para que pueda inyectarse y configurarse si es necesario.
     */
    @Bean
    public PersonProcessor processor() {
        return new PersonProcessor();
    }

    /**
     * Bean Writer: RepositoryItemWriter usa Spring Data JPA para persistir.
     * - setRepository: repositorio que realizará la operación.
     * - setMethodName("save"): llamará a personRepository.save(person).
     */
    @Bean
    public RepositoryItemWriter<Person> writer() {
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(personRepository);
        writer.setMethodName("save");
        return writer;
    }

    /**
     * Bean Job: define el job "importPersons" que ejecuta un único step.
     * - JobRepository es provisto por Spring Batch.
     */
    @Bean
    public Job job(JobRepository jobRepository, Step csvImportStep) {
        return new JobBuilder("importPersons", jobRepository)
                .start(csvImportStep)
                .build();
    }

    /**
     * Bean Step: define el step que realizará la lectura -> procesamiento -> escritura.
     * - chunk(10): procesar en lotes de 10 elementos por transacción.
     * - reader(), processor(), writer(): componentes del pipeline.
     */
    @Bean
    public Step csvImportStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-import-step", jobRepository)
                .<Person, Person>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
```

---

## 6) Controlador REST `JobController`  
Endpoint para lanzar el Job manualmente y obtener la primera entidad guardada.

```java
package hub.com.springbatchdemov1.controller;

import hub.com.springbatchdemov1.entity.Person;
import hub.com.springbatchdemov1.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.BatchStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JobController:
 * - Expone /jobs/importdata para ejecutar el Job manualmente.
 * - Genera un JobParameter "startAt" con la hora actual para evitar colisión de parámetros.
 * - Si el Job completa correctamente, devuelve la primera entidad guardada.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/jobs")
public class JobController {

    // Inyectamos JobLauncher para lanzar el job y PersonRepository para consultar resultados
    private final JobLauncher jobLauncher;
    private final PersonRepository personRepository;
    private final Job job;

    /**
     * POST /jobs/importdata
     * - Ejecuta el job una sola vez (según JobParameters diferentes).
     * - Retorna 200 OK con la primera persona si la carga fue exitosa.
     * - Retorna 204 No Content si no hay resultados.
     * - Retorna 400 o 500 según el caso.
     */
    @PostMapping("/importdata")
    public ResponseEntity<Person> jobData() {
        // Añadimos un parametro temporal para garantizar unicidad de ejecución
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            // Ejecutar el job y esperar su finalización (jobLauncher.run bloquea hasta finish)
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            // Comprobar el estado final del job
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                // Si finalizó correctamente, consultamos la primera persona guardada
                Person firstPerson = personRepository.findFirstByOrderByIdAsc();

                if (firstPerson != null) {
                    // Devolver la entidad con 200 OK
                    return ResponseEntity.ok(firstPerson);
                } else {
                    // Si no hay datos en BD, devolver 204 No Content
                    return ResponseEntity.noContent().build();
                }
            } else {
                // Si el job terminó con otro estado (FAILED, STOPPED, etc.), devolver 400
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Loguear la excepción (aquí usamos e.printStackTrace(), en producción usar logger)
            e.printStackTrace();
            // Devolver 500 Internal Server Error si ocurre cualquier excepción
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

---

## Sugerencias y buenas prácticas aplicadas
1. **Separación por paquetes**: `entity`, `repository`, `config`, `batch`, `controller` para mantener el proyecto organizado.
2. **Anotaciones de Lombok**: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` para reducir boilerplate.
3. **Validaciones**: En el processor se colocaron checks para evitar `NullPointerException`.
4. **Transaction Management**: Se usa chunk-oriented processing con `PlatformTransactionManager` (proporcionado por Spring Boot).
5. **Control de ejecución del Job**: `spring.batch.job.enabled=false` y uso de `JobLauncher` para ejecutar manualmente.
6. **Logging**: Reemplaza `e.printStackTrace()` por un logger (`slf4j`) en producción.
7. **DTO / API contract**: Si no quieres exponer entidades JPA en la API, crea DTOs y mapea con `MapStruct` o manualmente.
8. **Manejo de errores**: Puedes mejorar devolviendo mensajes de error estandarizados (ej: `ApiError`).

---

## Archivos adicionales sugeridos
- `resources/people-1000.csv` → CSV con encabezado y datos.
- `src/main/resources/application.properties` → configuración incluida arriba.
- `README.md` → este documento.

---

## Descargar este Markdown
He guardado este archivo como `SPRINGBATCH_DOCUMENTED.md` para que lo descargues:

- [Descargar SPRINGBATCH_DOCUMENTED.md](sandbox:/mnt/data/SPRINGBATCH_DOCUMENTED.md)

---

Si quieres, puedo:
- Generar el proyecto Java completo (estructura de carpetas) en un .zip listo para abrir en IntelliJ.
- Convertir este Markdown a PDF.
- Añadir DTOs y control de validación (ej: `@Valid` + `@RequestBody`) al controlador.
- Reemplazar `e.printStackTrace()` por `Logger` y añadir logging configurado.

Dime cuál de las opciones quieres y lo hago de una vez.
