package com.sysgears.reports.repository;

import com.sysgears.reports.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
