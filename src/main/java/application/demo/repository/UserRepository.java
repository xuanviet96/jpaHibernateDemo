package application.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import application.demo.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{

}
