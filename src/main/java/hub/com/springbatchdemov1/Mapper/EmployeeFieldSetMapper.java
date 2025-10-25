package hub.com.springbatchdemov1.Mapper;

import hub.com.springbatchdemov1.entity.Employee;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeFieldSetMapper implements FieldSetMapper<Employee> {

    @Override
    public Employee mapFieldSet(FieldSet fieldSet) {
        Employee e = new Employee();

        e.setIdCliente(fieldSet.readLong("id_cliente"));
        e.setNombre(fieldSet.readString("nombre"));
        e.setApellido(fieldSet.readString("apellido"));
        e.setEmail(fieldSet.readString("email"));

        // 👇 Aquí convertimos String -> LocalDate
        String fecha = fieldSet.readString("fecha_registro");
        e.setFechaRegistro(LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return e;
    }
}
