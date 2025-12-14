package task21;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnonymizationService {
    private static final Pattern IPV4 = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern IPV6 = Pattern.compile("\\b[0-9a-fA-F:]{2,}\\b");
    private static final Pattern BRACKETED_IPV6 = Pattern.compile("\\[[0-9a-fA-F:]{2,}\\]");
    private static final Pattern EMAIL = Pattern.compile(
            "\\b[\\w.%+-]+@(?:[\\w.-]+\\.[A-Za-z]{2,}|\\[(?:\\d{1,3}\\.){3}\\d{1,3}\\])\\b"
    );


    public String maskLine(String line) {
        line = replace(line, EMAIL, "[EMAIL]");
        line = replace(line, BRACKETED_IPV6, "[IP]");
        line = replace(line, IPV4, "[IP]");
        line = replace(line, IPV6, "[IP]");
        return line;
    }

    private String replace(String input, Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}