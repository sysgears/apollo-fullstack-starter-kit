package com.sysgears.upload.repository;

import com.sysgears.upload.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Integer> {
}
