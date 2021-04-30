package com.sysgears.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @NonNull
    private Integer id;
    @NonNull
    private String name;
    @NonNull
    private String type;
    @NonNull
    private Long size;
    @NonNull
    private String path;
}
