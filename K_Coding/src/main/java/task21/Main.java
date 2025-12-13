package task21;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        TxtReader reader = new TxtReader();
        TxtWriter writer = new TxtWriter();
        AnonymizationService service = new AnonymizationService();

        String text = reader.readTxt();
        String s = service.maskLine(text);
        writer.write(s);
    }

}
