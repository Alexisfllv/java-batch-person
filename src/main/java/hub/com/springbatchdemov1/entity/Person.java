package hub.com.springbatchdemov1.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //contador

    private String index; //index
    private String userId; //UserId
    private String firstName; // First Name
    private String lastName; // Last Name
    private String gender; // Gender
    private String email; // Email
    private String phone; // Phone
    private String dateOfBirth; // Date of birth
    private String jobTitle; //job Title
}
