package com.sysgears.upload.resolvers;

import com.sysgears.upload.dto.File;
import com.sysgears.upload.repository.FileMetadataRepository;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UploadQueryResolver implements GraphQLQueryResolver {
	private final FileMetadataRepository fileMetadataRepository;

	public CompletableFuture<List<File>> files() {
		return CompletableFuture.supplyAsync(() ->
				fileMetadataRepository.findAll().stream()
						.map(file -> new File(
								file.getId(),
								file.getName(),
								file.getContentType(),
								file.getSize(),
								file.getPath()
						))
						.collect(Collectors.toList())
		);
	}
}
