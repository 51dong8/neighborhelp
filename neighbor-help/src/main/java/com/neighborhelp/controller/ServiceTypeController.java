package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.entity.Request;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.RequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务类型控制器
 */
@Tag(name = "服务类型管理", description = "服务类型配置和统计")
@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final RequestService requestService;

    private static List<Map<String, Object>> buildStaticServiceTypes() {
        List<Map<String, Object>> serviceTypes = new ArrayList<>();

        Map<String, Object> packageType = new HashMap<>();
        packageType.put("id", "package");
        packageType.put("name", "帮我取快递");
        packageType.put("icon", "📦");
        packageType.put("iconClass", "package-icon");
        packageType.put("description", "帮忙代收快递包裹，安全保管");
        serviceTypes.add(packageType);

        Map<String, Object> petType = new HashMap<>();
        petType.put("id", "pet");
        petType.put("name", "帮我照顾宠物");
        petType.put("icon", "🐕");
        petType.put("iconClass", "pet-icon");
        petType.put("description", "照顾宠物日常护理，陪伴玩耍");
        serviceTypes.add(petType);

        Map<String, Object> repairType = new HashMap<>();
        repairType.put("id", "repair");
        repairType.put("name", "帮我家电维修");
        repairType.put("icon", "🔧");
        repairType.put("iconClass", "repair-icon");
        repairType.put("description", "家电故障维修，专业技术服务");
        serviceTypes.add(repairType);

        Map<String, Object> shoppingType = new HashMap<>();
        shoppingType.put("id", "shopping");
        shoppingType.put("name", "帮我买菜");
        shoppingType.put("icon", "🛒");
        shoppingType.put("iconClass", "shopping-icon");
        shoppingType.put("description", "帮忙购买生活用品，新鲜送达");
        serviceTypes.add(shoppingType);

        Map<String, Object> cleaningType = new HashMap<>();
        cleaningType.put("id", "cleaning");
        cleaningType.put("name", "帮我家清洁");
        cleaningType.put("icon", "🧹");
        cleaningType.put("iconClass", "cleaning-icon");
        cleaningType.put("description", "家庭清洁整理，专业家政服务");
        serviceTypes.add(cleaningType);

        Map<String, Object> cookingType = new HashMap<>();
        cookingType.put("id", "cooking");
        cookingType.put("name", "帮我做美食");
        cookingType.put("icon", "🍳");
        cookingType.put("iconClass", "cooking-icon");
        cookingType.put("description", "代做各种美食料理，美味可口");
        serviceTypes.add(cookingType);

        return serviceTypes;
    }

    @Operation(summary = "获取服务类型列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getServiceTypes() {
        return Result.success(buildStaticServiceTypes());
    }

    @Operation(summary = "获取服务类型详情")
    @GetMapping("/{serviceTypeId}")
    public Result<Map<String, Object>> getServiceTypeDetail(@PathVariable String serviceTypeId) {
        return buildStaticServiceTypes().stream()
                .filter(m -> serviceTypeId.equals(m.get("id")))
                .findFirst()
                .map(Result::success)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
    }

    @Operation(summary = "获取服务类型统计")
    @GetMapping("/stats")
    public Result<Map<String, Integer>> getServiceTypeStats() {
        Map<String, Integer> stats = new HashMap<>();

        // 统一按各服务类型统计“active”状态的需求数量
        String[] types = {"package", "pet", "repair", "shopping", "cleaning", "cooking"};
        for (String type : types) {
            long count = requestService.lambdaQuery()
                    .eq(Request::getServiceType, type)
                    .eq(Request::getStatus, "active")
                    .count();
            stats.put(type, (int) count);
        }

        return Result.success(stats);
    }

    @Operation(summary = "获取服务类型的需求列表")
    @GetMapping("/{serviceTypeId}/requests")
    public Result<PageVO<RequestVO>> getServiceTypeRequests(
            @PathVariable String serviceTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        PageVO<RequestVO> pageVO = requestService.getRequestList(serviceTypeId, status, page, limit, "latest");
        return Result.success(pageVO);
    }
}

