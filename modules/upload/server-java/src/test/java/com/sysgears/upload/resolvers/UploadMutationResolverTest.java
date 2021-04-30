package com.sysgears.upload.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.upload.model.FileMetadata;
import com.sysgears.upload.repository.FileMetadataRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class UploadMutationResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private FileMetadataRepository fileMetadataRepository;

	@Test
	void uploadFiles() throws Exception {
		List<File> files = prepare("file1", "file2");

		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("operations", "{ \"query\": \"mutation uploadFiles($files: [FileUpload!]!) { uploadFiles(files: $files)  }\", \"variables\": { \"files\": [null, null] } }");
		params.add("map", "{\"0\": [\"variables.files.0\"], \"1\": [\"variables.files.1\"]}");
		params.add("0", new FileSystemResource(files.get(0)));
		params.add("1", new FileSystemResource(files.get(1)));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		ResponseEntity<String> response = restTemplate.exchange(
				"/graphql",
				HttpMethod.POST,
				new HttpEntity<>(params, headers),
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<FileMetadata> all = fileMetadataRepository.findAll();
		assertEquals(2, all.size());
		assertEquals(files.get(0).getName(), all.get(0).getName());
		assertEquals(files.get(1).getName(), all.get(1).getName());

		// remove directories and files after test
		FileUtils.deleteDirectory(files.get(0).getParentFile());
		FileUtils.deleteDirectory(new File(all.get(0).getPath()).getParentFile());
	}

	@Test
	void removeFile() throws Exception {
		File file = prepare("file1").get(0);

		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("operations", "{ \"query\": \"mutation uploadFiles($files: [FileUpload!]!) { uploadFiles(files: $files)  }\", \"variables\": { \"files\": [null] } }");
		params.add("map", "{\"0\": [\"variables.files.0\"]}");
		params.add("0", new FileSystemResource(file));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		restTemplate.exchange(
				"/graphql",
				HttpMethod.POST,
				new HttpEntity<>(params, headers),
				String.class);

		List<FileMetadata> all = fileMetadataRepository.findAll();

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", all.get(0).getId());

		template.perform("remove-file.graphql", node);

		assertFalse(fileMetadataRepository.existsById(all.get(0).getId()));

		FileUtils.deleteDirectory(file.getParentFile());
	}

	private List<File> prepare(String... filenames) throws IOException {
		List<File> files = new ArrayList<>();
		Paths.get("src/test/resources/upload-files/").toFile().mkdirs(); //create directories if not exist
		for (String filename : filenames) {
			FileWriter writer = new FileWriter("src/test/resources/upload-files/" + filename + ".txt");
			writer.write("some file content");
			writer.close();
			files.add(new File("src/test/resources/upload-files/" + filename + ".txt"));
		}
		return files;
	}
}
