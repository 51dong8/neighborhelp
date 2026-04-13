package com.neighborhelp.service.support;

import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.entity.Request;

/**
 * 需求状态手动变更规则（非订单流程触发的 PATCH 状态）。
 * 订单接单/完成/取消仍由 OrderServiceImpl 内聚处理。
 */
public final class RequestStatusRules {

    private RequestStatusRules() {
    }

    public static void validateManualStatusTransition(Request request, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态不能为空");
        }
        String ns = newStatus.trim().toLowerCase();
        if ("active".equals(ns)) {
            throw new BusinessException(
                    ResultCode.REQUEST_STATUS_ERROR,
                    "禁止手动将需求设为 active；重新上架仅能通过取消订单等系统流程触发"
            );
        }

        String cur = request.getStatus() == null ? "" : request.getStatus().trim().toLowerCase();
        if ("completed".equals(cur) || "cancelled".equals(cur)) {
            throw new BusinessException(ResultCode.REQUEST_STATUS_ERROR, "已结束的需求不可再变更状态");
        }

        boolean taken = request.getAcceptedBy() != null || "accepted".equals(cur);
        if (taken) {
            throw new BusinessException(
                    ResultCode.REQUEST_STATUS_ERROR,
                    "需求已被接单，请使用订单相关接口完成或取消，勿直接改需求状态"
            );
        }

        if (!"cancelled".equals(ns) && !"completed".equals(ns)) {
            throw new BusinessException(ResultCode.REQUEST_STATUS_ERROR, "仅支持将未接单需求设为 cancelled 或 completed");
        }
    }

    public static void assertDeletable(Request request) {
        if (!"active".equals(request.getStatus())) {
            throw new BusinessException("只能删除活跃状态的需求");
        }
        if (request.getAcceptedBy() != null) {
            throw new BusinessException("已有接单记录的需求请先取消订单后再删除");
        }
    }
}
