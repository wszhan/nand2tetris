import java.io.File;

/**
 * Project 11 Main method.
 */
public class JackCompiler {

    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            String path = args[0];
            File file = new File(path);

            JackAnalyzer analyzer = new JackAnalyzer(file);
        }
    }
}
