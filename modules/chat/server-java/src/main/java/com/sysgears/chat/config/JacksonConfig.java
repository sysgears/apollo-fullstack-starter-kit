package com.sysgears.chat.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.Part;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Part.class, new PartDeserializer());
		objectMapper.registerModule(module);
		return objectMapper;
	}

	// Mock deserializer for Part to use Part in DTO as in graphQL input
	public static class PartDeserializer extends JsonDeserializer<Part> {
		@Override
		public Part deserialize(JsonParser p, DeserializationContext ctxt) {
			return null;
		}
	}
}
