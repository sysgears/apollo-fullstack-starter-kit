package com.sysgears.upload.resolvers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.upload.dto.File;
import com.sysgears.upload.model.FileMetadata;
import com.sysgears.upload.repository.FileMetadataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class UploadQueryResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@MockBean
	private FileMetadataRepository fileMetadataRepository;

	@Test
	void files() throws IOException {
		FileMetadata jpegMetadata = new FileMetadata(
				"filename",
				"image/jpeg",
				123L,
				"/fake_path/image.jpeg"
		);
		jpegMetadata.setId(1);
		FileMetadata pngMetadata = new FileMetadata(
				"filename2",
				"image/png",
				456L,
				"/fake_path/image.png"
		);
		pngMetadata.setId(2);
		when(fileMetadataRepository.findAll()).thenReturn(List.of(jpegMetadata, pngMetadata));

		GraphQLResponse response = template.postForResource("get-all-files.graphql");
		assertTrue(response.isOk());

		String responseBody = response.getRawResponse().getBody();
		ObjectMapper mapper = new ObjectMapper();
		JsonParser filesJson = mapper.readTree(responseBody).findPath("files").traverse();
		List<File> files = mapper.readValue(filesJson, new TypeReference<>() {
		});

		assertEquals(2, files.size());
		assertEquals(jpegMetadata.getId(), files.get(0).getId());
		assertEquals(jpegMetadata.getName(), files.get(0).getName());
		assertEquals(jpegMetadata.getContentType(), files.get(0).getType());
		assertEquals(jpegMetadata.getSize(), files.get(0).getSize());
		assertEquals(jpegMetadata.getPath(), files.get(0).getPath());
		assertEquals(pngMetadata.getId(), files.get(1).getId());
		assertEquals(pngMetadata.getName(), files.get(1).getName());
		assertEquals(pngMetadata.getContentType(), files.get(1).getType());
		assertEquals(pngMetadata.getSize(), files.get(1).getSize());
		assertEquals(pngMetadata.getPath(), files.get(1).getPath());
	}
}
