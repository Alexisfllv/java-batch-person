package hub.com.springbatchdemov1.config;


import hub.com.springbatchdemov1.Mapper.EmployeeFieldSetMapper;
import hub.com.springbatchdemov1.entity.Employee;
import hub.com.springbatchdemov1.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EmployeeBatchConfig {

    private final EmployeeRepository employeeRepository;




    // reader
    @Bean
    public FlatFileItemReader<Employee> readerEmployee() {
        return new FlatFileItemReaderBuilder<Employee>()
                .name("employeeItemReader")
                .resource(new ClassPathResource("employee.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapperEmployee())
                .targetType(Employee.class)
                .build();
    }


    // processor
    @Bean
    public EmployeeProcessor processorEmployee() {
        return new EmployeeProcessor();
    }

    // writer
    @Bean
    public RepositoryItemWriter<Employee> writerEmployee() {
        RepositoryItemWriter<Employee> writer = new RepositoryItemWriter<>();
        writer.setRepository(employeeRepository);
        writer.setMethodName("save");
        return writer;
    }

    // step principal
    @Bean
    public Job jobEmployee(JobRepository jobRepository, Step csvImportStepEmployee) {
        return new JobBuilder("importEmplyee",jobRepository)
                .start(csvImportStepEmployee)
                .build();
    }


    // token de mapeo
    private LineMapper<Employee> lineMapperEmployee() {
        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

        lineTokenizer.setDelimiter(","); // separado por comas
        lineTokenizer.setStrict(false); // permitir lineas con menos campos sin fallar

        // mismo orden de csv
        lineTokenizer.setNames(
                "id_cliente",
                "nombre",
                "apellido",
                "email"
                ,"fecha_registro"
        );

        lineMapper.setLineTokenizer(lineTokenizer);

        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();

        // Aquí reemplazamos el BeanWrapperFieldSetMapper
        lineMapper.setFieldSetMapper(new EmployeeFieldSetMapper());

        return lineMapper;
    }

    // csv module
    @Bean
    public Step csvImportStepEmployee(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-import-employee-step",jobRepository)
                .<Employee,Employee> chunk(10,transactionManager)
                .reader(readerEmployee())
                .processor(processorEmployee())
                .writer(writerEmployee())
                .build();
    }


}
