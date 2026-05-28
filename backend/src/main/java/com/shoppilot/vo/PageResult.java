package com.shoppilot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Long page;
    private Long size;
}
