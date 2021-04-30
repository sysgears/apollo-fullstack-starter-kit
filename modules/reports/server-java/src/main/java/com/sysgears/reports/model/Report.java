package com.sysgears.reports.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "REPORT")
public class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private int id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "PHONE", nullable = false)
	private String phone;

	@Column(name = "EMAIL", nullable = false)
	private String email;

	public Report(String name, String phone, String email) {
		this.name = name;
		this.phone = phone;
		this.email = email;
	}
}
