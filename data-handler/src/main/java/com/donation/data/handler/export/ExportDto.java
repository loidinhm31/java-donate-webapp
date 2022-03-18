package com.donation.data.handler.export;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ExportDto {
    String fileName;
    List<ExportEnum> columns = new ArrayList<>();
}
