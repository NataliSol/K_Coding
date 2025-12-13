package task21;

import java.io.*;

public class TxtWriter {
    public void write(String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.log"))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}