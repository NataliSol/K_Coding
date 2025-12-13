package task21;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TxtReader {

    public String readTxt() throws IOException {
        StringBuilder text = new StringBuilder();
        try
                (BufferedReader reader = new BufferedReader(new FileReader("input.log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
