package hub.com.springbatchdemov1.config;

import hub.com.springbatchdemov1.entity.Employee;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {


    @Override
    public Employee process(Employee item) throws Exception {

        if (item == null) {
            return null;
        }

        // verificar null

        if (item.getNombre() != null) {
            item.setNombre(item.getNombre().toUpperCase());
        }

        if (item.getApellido() != null) {
            item.setApellido(item.getApellido().toUpperCase());
        }

        // validaciones

        return item;
    }
}
