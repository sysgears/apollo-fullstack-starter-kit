package com.sysgears.user.model;

import com.sysgears.user.model.auth.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "USER_AUTH")
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    private int id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private CertificateAuth certificate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private FacebookAuth facebook;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private GoogleAuth google;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private GithubAuth github;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private LinkedInAuth linkedin;
}
