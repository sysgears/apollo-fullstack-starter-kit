package com.sysgears.post.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "СOMMENT")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    @ManyToOne
    private Post post;

    public Comment(String content) {
        this.content = content;
    }
}
