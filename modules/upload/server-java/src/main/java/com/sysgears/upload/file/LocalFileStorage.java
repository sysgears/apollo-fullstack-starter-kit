package com.sysgears.upload.file;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Part;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class LocalFileStorage implements FileStorage {
    private static final String FILES_DIRECTORY = "files";

    @Override
    @SneakyThrows
    public String writeFile(String fileName, Part part) {
        final Path filePath = Paths.get(FILES_DIRECTORY).resolve(fileName);
        filePath.getParent().toFile().mkdirs(); //create directories if not exist

        File file = filePath.toFile();

        part.write(file.getAbsolutePath());
        log.debug("File write to '{}'", file.getPath());

        return file.getPath();
    }

    @Override
    @SneakyThrows
    public void deleteFile(String filePath) {
        log.debug("Deleting file '{}'", filePath);
        Files.deleteIfExists(Paths.get(filePath));
    }
}
