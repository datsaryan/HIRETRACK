package com.hiretrack.dto;

import java.util.List;
import java.util.Map;

public class DashboardDtos {

    public record StageFunnelEntry(String stageName, long count) {}

    public record DashboardSummaryResponse(
            long totalOpenJobs,
            long totalActiveApplications,
            long totalHired,
            long totalRejected,
            double averageTimeToHireDays,
            List<StageFunnelEntry> funnel
    ) {}
}
