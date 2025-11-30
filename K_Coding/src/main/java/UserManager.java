import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    public List<String> allUsers = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger(UserManager.class);

    public void createUser(String username) {
        LOGGER.info("процес створення юзера запущений");
        if (username != null) {
            allUsers.add(username);
            LOGGER.warn("додавання юзера пройшло успішно");
        } else
            LOGGER.warn("додавання юзера пройшло не успішно");
    }

    public void deleteUser(String username) {
        if (username != null) {
            allUsers.remove(username);
        } else
            LOGGER.warn("видалення юзера пройшло не успішно");
    }

    public String findUser(String username) {
        boolean found = false;
        for (String user : allUsers) {
            if (user.equals(username)) {
                System.out.println("Юзер знайдений");
                return user;
            }
        }
        LOGGER.warn("Юзер не знайдений");
        return null;
    }

    public void userUpdate(String username) {
        LOGGER.info("процес створення юзера запущений");
        try {
            findUser(username);
            String cryptoName = username + "12233ew";
        } catch (NullPointerException e) {
            LOGGER.error("юзер не знайдений");
        }
    }
}