package com.sysgears.user.model.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CERTIFICATE_AUTH")
public class CertificateAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    private int id;

    @Column(name = "SERIAL")
    private String serial;

    public CertificateAuth(String serial) {
        this.serial = serial;
    }
}
