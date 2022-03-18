package com.donation.data.handler.service;

import com.donation.common.core.exception.DataProcessNotFoundException;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, List<Integer>> calculateBarChart();

    Map<String, List<Float>> calculateLineChart(String projectId) throws DataProcessNotFoundException;
}
