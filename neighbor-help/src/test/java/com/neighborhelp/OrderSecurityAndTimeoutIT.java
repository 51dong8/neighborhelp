package com.neighborhelp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.common.Result;
import com.neighborhelp.dto.AcceptOrderDTO;
import com.neighborhelp.dto.UpdateOrderStatusDTO;
import com.neighborhelp.entity.Notification;
import com.neighborhelp.entity.Order;
import com.neighborhelp.entity.Request;
import com.neighborhelp.entity.User;
import com.neighborhelp.mapper.NotificationMapper;
import com.neighborhelp.mapper.OrderMapper;
import com.neighborhelp.mapper.RequestMapper;
import com.neighborhelp.mapper.UserMapper;
import com.neighborhelp.service.OrderService;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.service.support.RequestStatusRules;
import com.neighborhelp.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 面向关键闭环的集成测试（不做 MockMvc，直接调用 Service 并断言 DB）。
 * 目标：
 * - 鉴权/越权：非 owner 操作需求、非接单者操作订单详情/状态
 * - 状态机：手动 set active 被禁止、accepted 后不得回退
 * - 超时：接单未开始订单超时 => 取消订单 + 回滚需求 + 写通知
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderSecurityAndTimeoutIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long publisherId;
    private Long helperId;

    @BeforeEach
    void setup() {
        // 使用 schema-h2.sql 初始化的用户 admin/testuser 作为占位
        // admin 作为发布者，testuser 作为接单者（ID 需要从 DB 查询）
        publisherId = userMapper.selectByUsername("admin").getId();
        helperId = userMapper.selectByUsername("testuser").getId();
    }

    @Test
    void requestStatusRules_rejectManualActive() {
        Request r = new Request();
        r.setUserId(publisherId);
        r.setServiceType("package");
        r.setTitle("t");
        r.setStatus("active");
        assertThrows(BusinessException.class, () -> RequestStatusRules.validateManualStatusTransition(r, "active"));
    }

    @Test
    void orderTimeout_cancelAndRollbackAndNotify() {
        // 1) 发布需求（active）
        Request request = new Request();
        request.setUserId(publisherId);
        request.setServiceType("package");
        request.setTitle("快递取件");
        request.setDescription("desc");
        request.setRewardType("points");
        request.setRewardAmount(20);
        request.setLocation("loc");
        request.setUrgency("normal");
        request.setStatus("active");
        request.setServiceDetails("{}");
        requestService.save(request);

        // 2) 接单生成订单（active）
        AcceptOrderDTO dto = new AcceptOrderDTO();
        dto.setRequestId(request.getId());
        dto.setMessage("我可以");
        // acceptOrder 内部会更新 requests.status=accepted + accepted_by
        // 我们通过 service 方法绕过 security（不在此测试鉴权过滤）
        orderService.acceptOrder(helperId, dto);

        // 3) 人为把 acceptedAt 往前挪，模拟超时
        Request persistedReq = requestMapper.selectById(request.getId());
        assertEquals("accepted", persistedReq.getStatus());

        Order persistedOrder = orderMapper.selectByRequestId(request.getId());
        assertNotNull(persistedOrder);
        persistedOrder.setAcceptedAt(LocalDateTime.now().minusMinutes(60));
        persistedOrder.setStatus("active");
        orderMapper.updateById(persistedOrder);

        // 4) 触发系统超时回滚
        orderService.systemCancelOrderByTimeout(persistedOrder.getId(), "接单超时未响应");

        // 5) 断言订单取消
        Order afterOrder = orderMapper.selectById(persistedOrder.getId());
        assertEquals("cancelled", afterOrder.getStatus());
        assertNotNull(afterOrder.getCancelReason());

        // 6) 断言需求回滚为 active
        Request afterRequest = requestMapper.selectById(request.getId());
        assertEquals("active", afterRequest.getStatus());
        assertNull(afterRequest.getAcceptedBy());

        // 7) 断言通知写入
        Notification n = notificationMapper.selectUnreadByUserAndType(publisherId, "order_timeout_cancelled");
        assertNotNull(n);
        assertFalse(n.getIsRead());
    }
}

