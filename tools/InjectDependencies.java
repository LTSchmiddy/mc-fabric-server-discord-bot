import java.nio.file.*;
import java.util.HashMap;
import java.io.*;

public class InjectDependencies {
    public static void main(String[] argv) {
        // for (String i : argv) {
        //     System.out.println(i);
        // };

        InjectToZipFile(argv[0], argv[1], argv[2]);
        System.out.println("JDA Injection successful");
    }

    public static void InjectToZipFile(String archive, String target, String destination) {
        Path zipFilePath = Paths.get(archive);
        Path myFilePath = Paths.get(target);

        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, new HashMap<String, String>(), null)) {
            Path fileInsideZipPath = fs.getPath(destination);
            if (Files.exists(fileInsideZipPath)){
                Files.delete(fileInsideZipPath);
            }
            Files.copy(myFilePath, fileInsideZipPath);
            fs.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
