package com.sysgears.upload.service;

import com.sysgears.upload.exception.FileNotFoundException;
import com.sysgears.upload.file.FileStorage;
import com.sysgears.upload.model.FileMetadata;
import com.sysgears.upload.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Part;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorage fileStorage;

    public FileMetadata create(Part file) {
        String fileName = file.getSubmittedFileName().replace(" ", "_");
        String path = fileStorage.writeFile(fileName, file);

        return fileMetadataRepository.save(new FileMetadata(fileName, file.getContentType(), file.getSize(), path));
    }

    public void deleteById(Integer id) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id).orElseThrow(()-> new FileNotFoundException(id));

        fileStorage.deleteFile(fileMetadata.getPath());
        fileMetadataRepository.deleteById(id);
    }
}
