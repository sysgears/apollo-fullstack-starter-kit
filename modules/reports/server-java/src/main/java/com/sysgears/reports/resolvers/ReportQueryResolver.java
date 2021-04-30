package com.sysgears.reports.resolvers;

import com.sysgears.reports.model.Report;
import com.sysgears.reports.repository.ReportRepository;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ReportQueryResolver implements GraphQLQueryResolver {
	private final ReportRepository reportRepository;

	public CompletableFuture<List<Report>> report() {
		return CompletableFuture.supplyAsync(reportRepository::findAll);
	}
}
