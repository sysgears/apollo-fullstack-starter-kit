package com.sysgears.upload.resolvers;

import com.sysgears.upload.service.FileService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Part;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UploadMutationResolver implements GraphQLMutationResolver {
    private final FileService fileService;

    public boolean uploadFiles(List<Part> files) {
        for (Part file : files) {
            fileService.create(file);
        }

        return true;
    }

    public boolean removeFile(Integer id) {
        fileService.deleteById(id);

        return true;
    }
}
