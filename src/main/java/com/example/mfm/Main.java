package com.example.mfm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
            return;
        }

        MediaFileManager manager = new MediaFileManager();
        String command = args[0].toLowerCase();

        switch (command) {
            case "list" -> {
                Path root = Path.of(args[1]);
                List<Path> files = manager.listFiles(root);
                files.forEach(System.out::println);
            }
            case "group" -> {
                Path root = Path.of(args[1]);
                Map<MediaType, List<Path>> grouped = manager.groupByMediaType(root);
                grouped.forEach((type, paths) -> {
                    System.out.println(type + ":");
                    paths.forEach(path -> System.out.println("  - " + path));
                });
            }
            case "organize" -> {
                if (args.length < 3) {
                    printUsage();
                    return;
                }
                Path source = Path.of(args[1]);
                Path destination = Path.of(args[2]);
                boolean moveFiles = args.length > 3 && "--move".equalsIgnoreCase(args[3]);
                List<Path> changed = manager.organizeByType(source, destination, moveFiles);
                System.out.printf("%s %d files.%n", moveFiles ? "Moved" : "Copied", changed.size());
            }
            default -> printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar app.jar list <path>");
        System.out.println("  java -jar app.jar group <path>");
        System.out.println("  java -jar app.jar organize <sourcePath> <destinationPath> [--move]");
    }
}
