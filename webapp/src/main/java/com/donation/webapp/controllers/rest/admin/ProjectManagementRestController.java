package com.donation.webapp.controllers.rest.admin;

import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.data.handler.service.ProjectService;
import com.donation.management.dto.ProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/projects")
public class ProjectManagementRestController {
    @Autowired
    private ProjectService projectService;

    @PostMapping("/time-extend")
    public ResponseEntity<String> updateProjectTime(@RequestBody ProjectDto projectDto) throws DataProcessNotFoundException, InvalidMessageException {
        projectService.extendTimeProject(projectDto);
        return ResponseEntity.ok().body("UPDATED");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteInitProject(@RequestParam String projectId) throws DataProcessNotFoundException {
        projectService.deleteInitProject(projectId);
        return ResponseEntity.ok().body("DELETED");
    }
}
