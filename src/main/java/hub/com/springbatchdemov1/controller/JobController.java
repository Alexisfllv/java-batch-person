package hub.com.springbatchdemov1.controller;


import hub.com.springbatchdemov1.entity.Person;
import hub.com.springbatchdemov1.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobs")
public class JobController {

    private final JobLauncher jobLauncher;
    private final PersonRepository personRepository;

    private final Job job;

    @PostMapping("/importdata")
    public ResponseEntity<Person> jobData() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            // Si el job terminó correctamente:
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

                // Obtener la primera persona guardada
                Person firstPerson = personRepository.findFirstByOrderByIdAsc();

                if (firstPerson != null) {
                    return ResponseEntity.ok(firstPerson);
                } else {
                    return ResponseEntity.noContent().build(); // No hay datos en BD aún
                }
            }

            // Si terminó con error u otro estado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
