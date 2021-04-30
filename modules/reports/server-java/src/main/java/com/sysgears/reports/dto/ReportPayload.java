package com.sysgears.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportPayload {
	@NonNull
	private Integer id;
	@NonNull
	private String name;
	@NonNull
	private String phone;
	@NonNull
	private String email;
}
