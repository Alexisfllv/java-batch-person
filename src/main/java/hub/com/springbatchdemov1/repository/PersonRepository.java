package hub.com.springbatchdemov1.repository;


import hub.com.springbatchdemov1.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    // Primera persona guardada (id más pequeño)
    Person findFirstByOrderByIdAsc();

    // Última persona guardada (id más grande)
    Person findTopByOrderByIdDesc();
}
