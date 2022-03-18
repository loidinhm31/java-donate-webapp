package com.donation.webapp.controllers.rest.admin;

import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.data.handler.service.impl.DashboardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage/dashboard")
public class DashBoardController {
    @Autowired
    private DashboardServiceImpl dashboardService;

    @GetMapping("/bar-chart")
    public ResponseEntity<Map<String, List<Integer>>> loadBarChar() {

        Map<String, List<Integer>> barChartMap = dashboardService.calculateBarChart();

        return ResponseEntity.ok().body(barChartMap);
    }

    @GetMapping("/line-chart")
    public ResponseEntity<Map<String, List<Float>>> loadBarChar(@RequestParam String projectId) throws DataProcessNotFoundException {

        Map<String, List<Float>> lineChartMap = dashboardService.calculateLineChart(projectId);

        return ResponseEntity.ok().body(lineChartMap);
    }
}
