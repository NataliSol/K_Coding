package optional;

import java.util.Optional;


public class UserRepository {

    public Optional<User> findUserByName(String name) {
        if (name.equals("Alice")) {
            return Optional.of(new User("Alice"));
        } else {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        UserRepository repository = new UserRepository();

        // 1. Alice – ifPresent()
        repository.findUserByName("Alice")
                .ifPresent(user -> System.out.println("Знайдено користувача: " + user.name));

        // 2. Bob – orElse()
        User userBob = repository.findUserByName("Bob")
                .orElse(new User("Користувача не знайдено"));
        System.out.println(userBob.name);

        // 3. Charlie – orElseThrow()
        User userCharlie = repository.findUserByName("Charlie")
                .orElseThrow(() -> new IllegalArgumentException("Користувач не існує"));
    }
}