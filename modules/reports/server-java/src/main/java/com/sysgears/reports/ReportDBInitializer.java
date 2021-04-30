package com.sysgears.reports;

import com.sysgears.reports.model.Report;
import com.sysgears.reports.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ReportDBInitializer {
	private final ReportRepository repository;

	@EventListener
	public void onApplicationStartedEvent(ApplicationStartedEvent event) {
		long count = repository.count();
		if (count == 0) {
			Report tomJacksonReport = new Report("Tom Jackson", "555-444-333", "tom@gmail.com");
			Report mikeJamesReport = new Report("Mike James", "555-777-888", "mikejames@gmail.com");
			Report janetLarsonReport = new Report("Janet Larson", "555-222-111", "janetlarson@gmail.com");
			Report clarkThompsonReport = new Report("Clark Thompson", "555-444-333", "clark123@gmail.com");
			Report emmaPageReport = new Report("Emma Page", "555-444-333", "emma1page@gmail.com");

			repository.saveAll(
					List.of(
							tomJacksonReport,
							mikeJamesReport,
							janetLarsonReport,
							clarkThompsonReport,
							emmaPageReport
					)
			);
		}
	}
}
