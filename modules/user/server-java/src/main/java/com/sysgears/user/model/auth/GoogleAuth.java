package com.sysgears.user.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "GOOGLE_AUTH")
public class GoogleAuth {
    @Id
    @Column(name = "GOOGLE_ID", nullable = false)
    private String googleId;

    @Column(name = "DISPLAY_NAME")
    private String displayName;
}
