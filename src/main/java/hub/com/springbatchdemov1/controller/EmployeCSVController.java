package hub.com.springbatchdemov1.controller;

import hub.com.springbatchdemov1.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employee")
public class EmployeCSVController {


    private final JobLauncher jobLauncher;

    @Qualifier("jobEmployee")   // <--- ESTA ES LA CLAVE
    private final Job jobEmployee;

    @PostMapping("/set")
    public ResponseEntity<String> setEmployee() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(jobEmployee, jobParameters);

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                return ResponseEntity.ok("Employee CSV Procesado ✅");
            }
            return ResponseEntity.badRequest().body("No completó ❌");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error 💥");
        }
    }
}
