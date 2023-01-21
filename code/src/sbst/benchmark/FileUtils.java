package sbst.benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static void copyDirectory(File sourceDirectory, File destinationDirectory)
            throws IOException {
        Path start = sourceDirectory.toPath();
        Path destPath = destinationDirectory.toPath();
        Files.walk(start)
                .forEach(source -> {
                    Path destination = destPath.resolve(start.relativize(source));
                    try {
                        if (Files.exists(destination) && Files.isDirectory(destination)){
                            // skip creation of existing folders, merge the content
                        }else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void copyFileToDirectory(File sourceFile, File destinationDirectory)
            throws IOException {
        Files.copy(sourceFile.toPath(), destinationDirectory.toPath().resolve(sourceFile.getName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public static void deleteQuietly(File file) {
        if (file == null) {
            return;
        }
        try (Stream<Path> stream = Files.walk(file.toPath())) {
            stream
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception e) {
            // swallow any exception
        }
    }

    public static void deleteDirectory(File file) {
        deleteQuietly(file);
    }

    public static List<File> listFiles(File dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir.toPath())) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }
}
