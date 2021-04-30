package com.sysgears.upload.file;

import javax.servlet.http.Part;

public interface FileStorage {

    String writeFile(String fileName, Part part);

    void deleteFile(String filePath);
}
