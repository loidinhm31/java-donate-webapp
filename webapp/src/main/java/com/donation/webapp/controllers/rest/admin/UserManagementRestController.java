package com.donation.webapp.controllers.rest.admin;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.data.handler.export.ExportDto;
import com.donation.data.handler.export.ExportEnum;
import com.donation.data.handler.export.service.UserExportExcelService;
import com.donation.data.handler.service.UserRoleService;
import com.donation.management.dto.UserAccountDto;
import com.donation.management.model.ProvideRoleRequest;
import com.donation.management.model.UserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/manage/users")
public class UserManagementRestController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserExportExcelService userExportExcelService;

    @PostMapping(value = "/role/save", consumes = {"application/json; charset=utf-8"})
    public void updateRole(@RequestBody UserAccountDto userAccountDto) throws InvalidMessageException {
        userRoleService.updateRole(userAccountDto);
    }

    @PostMapping(value = "/role", consumes = {"application/json; charset=utf-8"})
    public void updateRoleForManyUsers(@RequestBody ProvideRoleRequest roleRequest) throws InvalidMessageException {
        userRoleService.updatePermission(roleRequest);
    }

    @PostMapping(value = "/active/{userId}/{isActive}", consumes = {"application/json; charset=utf-8"})
    public void changeActive(@PathVariable String userId,
                             @PathVariable Boolean isActive) throws InvalidMessageException {
        userRoleService.changeActive(userId, isActive);
    }

    @GetMapping(value = "/export-excel")
    public ResponseEntity<ByteArrayResource> downloadExcelForUserList(@RequestParam(required = false) String searchKey,
                                                                      @RequestParam(required = false) String option) throws Exception {

        UserFilter userFilter = UserFilter.builder()
                .searchKey(searchKey)
                .option(option)
                .pageSize(Integer.MAX_VALUE)
                .build();

        List<ExportEnum> columns = new ArrayList<>();
        columns.add(ExportEnum.USERNAME);
        columns.add(ExportEnum.EMAIL);
        columns.add(ExportEnum.FIRST_NAME);
        columns.add(ExportEnum.LAST_NAME);
        columns.add(ExportEnum.DATE_OF_BIRTH);
        columns.add(ExportEnum.LAST_LOGIN_TIME);
        columns.add(ExportEnum.PHONE_NUMBER);
        columns.add(ExportEnum.VERIFIED);
        columns.add(ExportEnum.ACTIVE_OR_DEACTIVATE);


        ExportDto exportDto = new ExportDto();
        exportDto.setFileName("USERS.xlsx");
        exportDto.setColumns(columns);

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", "USERS.xlsx"))
                .body(userExportExcelService.exportExcelUserList(userFilter, exportDto));
    }
}
