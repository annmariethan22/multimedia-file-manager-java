package com.example.mfm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MediaFileManager {

    public List<Path> listFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    public Map<MediaType, List<Path>> groupByMediaType(Path root) throws IOException {
        List<Path> files = listFiles(root);
        return files.stream().collect(Collectors.groupingBy(this::resolveMediaType));
    }

    public List<Path> organizeByType(Path sourceRoot, Path destinationRoot, boolean moveFiles) throws IOException {
        List<Path> movedOrCopied = new ArrayList<>();
        for (Path file : listFiles(sourceRoot)) {
            MediaType type = resolveMediaType(file);
            Path targetFolder = destinationRoot.resolve(type.name().toLowerCase(Locale.ROOT));
            Files.createDirectories(targetFolder);

            Path targetPath = uniqueTargetPath(targetFolder.resolve(file.getFileName()));
            if (moveFiles) {
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            movedOrCopied.add(targetPath);
        }
        return movedOrCopied;
    }

    private Path uniqueTargetPath(Path targetPath) {
        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        String fileName = targetPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex > 0 ? fileName.substring(dotIndex) : "";

        int counter = 1;
        Path candidate;
        do {
            candidate = targetPath.getParent().resolve(baseName + "_" + counter + extension);
            counter++;
        } while (Files.exists(candidate));

        return candidate;
    }

    private MediaType resolveMediaType(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return MediaType.OTHER;
        }
        String extension = fileName.substring(dotIndex + 1);
        return MediaType.fromExtension(extension);
    }
}
