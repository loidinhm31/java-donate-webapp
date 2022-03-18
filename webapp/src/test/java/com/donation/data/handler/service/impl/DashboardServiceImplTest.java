package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.management.entity.Project;
import com.donation.common.management.repository.ProjectRepository;
import com.donation.data.handler.service.DashboardService;
import com.donation.data.handler.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceImplTest {
    @InjectMocks
    private DashboardService dashboardServiceTest = new DashboardServiceImpl();

    @Mock
    private ProjectRepository projectRepository;

    @Test
    public void testCalculateBarChart() {

        dashboardServiceTest.calculateBarChart();
        verify(projectRepository, times(48)) // 12 month * 4 status
                .countByMonthAndYearAndStatus(any(), any(), any());
    }

    @Test
    public void testCalculateLineChart() throws DataProcessNotFoundException, ParseException {
        String projectId = "NC-2100001";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.ofNullable(Project.builder()
                        .projectId(projectId)
                        .status(ProjectStatusEnum.CLOSED)
                        .startTime(simpleDateFormat.parse("01-01-2021"))
                        .updatedAt(simpleDateFormat.parse("27-06-2021"))
                        .build()));

        dashboardServiceTest.calculateLineChart(projectId);

        verify(projectRepository, times(6))
                .sumDonationTimeByProjectAndMonthAndYear(any(), any(), any());

        verify(projectRepository, times(6))
                .sumDonationTimeByProjectInRangeTime(any(), any(), any());
    }
}
