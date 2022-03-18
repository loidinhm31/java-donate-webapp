package com.donation.webapp.controllers.rest;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.data.handler.service.FollowerProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowRestController {

    @Autowired
    private FollowerProjectService followProjectService;

    @PostMapping("/follower/{projectId}")
    public ResponseEntity<Boolean> followProject(@PathVariable String projectId,
                                                 @RequestParam Boolean follow) throws InvalidMessageException {
        boolean val = followProjectService.followProject(projectId, follow);

        return ResponseEntity.ok()
                .body(val);
    }
}
