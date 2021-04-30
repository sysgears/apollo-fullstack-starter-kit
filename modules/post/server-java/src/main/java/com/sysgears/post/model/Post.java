package com.sysgears.post.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "POST")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "POST_COMMENT",
            joinColumns = { @JoinColumn(name = "POST_ID", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", referencedColumnName = "id") })
    private Set<Comment> comments = new HashSet<>();

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
    }
}
