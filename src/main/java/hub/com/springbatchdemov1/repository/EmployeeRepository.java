package hub.com.springbatchdemov1.repository;

import hub.com.springbatchdemov1.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
