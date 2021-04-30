package com.sysgears.upload.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "FILES")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CONTENT_TYPE", nullable = false)
    private String contentType;

    @Column(name = "SIZE", nullable = false)
    private Long size;

    @Column(name = "PATH", nullable = false)
    private String path;

    public FileMetadata(String name, String contentType, Long size, String path) {
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.path = path;
    }
}
