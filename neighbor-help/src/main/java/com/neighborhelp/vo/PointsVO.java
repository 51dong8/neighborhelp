package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 积分信息VO
 */
@Data
public class PointsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer totalPoints;
    private Integer availablePoints;
    private Integer frozenPoints;
    private Integer level;
    private Integer nextLevelPoints;
}

