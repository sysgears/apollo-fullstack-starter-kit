package com.sysgears.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "USERS")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "ROLE", nullable = false)
    private String role;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private UserProfile profile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    private UserAuth auth;

    public User(String username,
                String password,
                String role,
                Boolean isActive,
                String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
