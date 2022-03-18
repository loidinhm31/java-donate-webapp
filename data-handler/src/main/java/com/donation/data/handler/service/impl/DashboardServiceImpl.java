package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.management.entity.Project;
import com.donation.common.management.repository.ProjectRepository;
import com.donation.data.handler.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Map<String, List<Integer>> calculateBarChart() {
        LinkedHashMap<String, List<Integer>> barChartMap = new LinkedHashMap<>();

        ProjectStatusEnum[] statusEnumList = ProjectStatusEnum.values();

        int numberOfMonths = 12;
        for (int i = numberOfMonths - 1; i >= 0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -i);

            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer year = calendar.get(Calendar.YEAR);
            List<Integer> countOfEachStatus = new ArrayList<>();

            for (ProjectStatusEnum statusEnum : statusEnumList) {
                Integer numberOfProjects = projectRepository.countByMonthAndYearAndStatus(statusEnum.name(), month, year);
                countOfEachStatus.add(numberOfProjects);
            }
            String timeKey = new SimpleDateFormat("MMM-yyyy").format(calendar.getTime());
            barChartMap.put(timeKey, countOfEachStatus);
        }
        return barChartMap;
    }

    @Override
    public Map<String, List<Float>> calculateLineChart(String projectId) throws DataProcessNotFoundException {
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty()) {
            throw new DataProcessNotFoundException();
        }
        Project project = optionalProject.get();

        // Calendar for From date
        Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.setTime(project.getStartTime());
        calendarFrom.set(Calendar.DAY_OF_MONTH, 1);

        Date toDate;

        if (project.getStatus().equals(ProjectStatusEnum.CLOSED)) {
            toDate = project.getUpdatedAt();
        } else {
            toDate = new Date();
        }
        // Calendar for To date
        Calendar calendarTo = Calendar.getInstance();
        calendarTo.setTime(toDate);
        calendarTo.set(Calendar.DAY_OF_MONTH, calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH));

        int a = 12 * calendarFrom.get(Calendar.YEAR) + calendarFrom.get(Calendar.MONTH);
        int b = 12 * calendarTo.get(Calendar.YEAR) + calendarTo.get(Calendar.MONTH);
        int months = Math.abs(a-b);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        LinkedHashMap<String, List<Float>> lineChartMap = new LinkedHashMap<>();
        for (int i = 0; i <= months; i++) {

            String timeKey = new SimpleDateFormat("MMM-yyyy").format(calendarFrom.getTime());
            System.out.println(timeKey);

            Integer month = calendarFrom.get(Calendar.MONTH) + 1;
            Integer year = calendarFrom.get(Calendar.YEAR);
            System.out.println("Query for: " + month + " " + year);

            // Create a new calendar for total to current month
            Calendar lastDayCal = Calendar.getInstance();
            lastDayCal.setTime(calendarFrom.getTime());
            lastDayCal.set(Calendar.DAY_OF_MONTH, calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH));

            System.out.println("New cal from: " + simpleDateFormat.format(calendarFrom.getTime()));
            System.out.println("Query total from: " + simpleDateFormat.format(project.getStartTime()) + " to " + simpleDateFormat.format(lastDayCal.getTime()));
            System.out.println("Option: " + simpleDateFormat.format(calendarTo.getTime()));

            Float totalMoneyOfMonth = projectRepository
                    .sumDonationTimeByProjectAndMonthAndYear(projectId, month, year);
            Float accumulatedMoney = projectRepository
                    .sumDonationTimeByProjectInRangeTime(projectId,
                        simpleDateFormat.format(project.getStartTime()), simpleDateFormat.format(lastDayCal.getTime()));

            List<Float> moneys = new ArrayList<>();
            moneys.add(Objects.nonNull(totalMoneyOfMonth) ? totalMoneyOfMonth : 0);
            moneys.add(Objects.nonNull(accumulatedMoney) ? accumulatedMoney : 0);

            lineChartMap.put(timeKey, moneys);

            // Add one month
            calendarFrom.add(Calendar.MONTH, 1);

        }

        return lineChartMap;
    }
}
