import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Це інформаційне повідомлення");
        LOGGER.info("Це повідомлення про помилку");
        UserManager manager=new UserManager();
        manager.createUser("hjhj");
        manager.createUser(null);
        manager.deleteUser("ree");
        manager.deleteUser(null);
        manager.findUser("dsds");
        manager.userUpdate("");
    }
}