package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页VO
 */
@Data
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long total;
    private Integer page;
    private Integer limit;
    private Integer totalPages;

    public PageVO() {
    }

    public PageVO(List<T> records, Long total, Integer page, Integer limit) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.totalPages = (int) Math.ceil((double) total / limit);
    }
}

