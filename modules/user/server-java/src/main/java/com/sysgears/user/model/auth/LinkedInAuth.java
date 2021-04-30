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
@Table(name = "LINKEDIN_AUTH")
public class LinkedInAuth {
    @Id
    @Column(name = "LINKEDIN_ID", nullable = false)
    private String lnId;

    @Column(name = "DISPLAY_NAME")
    private String displayName;
}
