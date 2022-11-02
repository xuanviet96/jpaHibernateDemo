package application.demo.repository;

import application.demo.model.User;
import org.springframework.data.repository.CrudRepository;

public interface testInterface extends CrudRepository<User, Integer> {
}
