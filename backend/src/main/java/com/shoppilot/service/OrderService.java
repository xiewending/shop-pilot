package com.shoppilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppilot.common.BusinessException;
import com.shoppilot.dto.OrderQueryRequest;
import com.shoppilot.entity.OrderItem;
import com.shoppilot.entity.OrderRecord;
import com.shoppilot.mapper.OrderItemMapper;
import com.shoppilot.mapper.OrderMapper;
import com.shoppilot.vo.OrderDetailVO;
import com.shoppilot.vo.OrderItemVO;
import com.shoppilot.vo.OrderStatusOption;
import com.shoppilot.vo.OrderVO;
import com.shoppilot.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {

    private static final int PENDING_PAYMENT = 0;
    private static final int PENDING_SHIPMENT = 1;
    private static final int SHIPPED = 2;
    private static final int COMPLETED = 3;
    private static final int CANCELLED = 4;

    private static final Map<Integer, String> STATUS_TEXT = Map.of(
            PENDING_PAYMENT, "待付款",
            PENDING_SHIPMENT, "待发货",
            SHIPPED, "已发货",
            COMPLETED, "已完成",
            CANCELLED, "已取消"
    );

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_PAYMENT, Set.of(PENDING_SHIPMENT, CANCELLED),
            PENDING_SHIPMENT, Set.of(SHIPPED, CANCELLED),
            SHIPPED, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public PageResult<OrderVO> page(OrderQueryRequest request) {
        long requestedPage = request.getPage() == null ? 1L : request.getPage();
        long requestedSize = request.getSize() == null ? 10L : request.getSize();
        long page = Math.max(requestedPage, 1L);
        long size = Math.min(Math.max(requestedSize, 1L), 100L);

        Long total = orderMapper.selectCount(buildQueryWrapper(request));
        List<OrderRecord> records = orderMapper.selectList(buildQueryWrapper(request)
                .last("limit " + ((page - 1) * size) + ", " + size));

        return new PageResult<>(
                records.stream().map(this::toVO).toList(),
                total,
                page,
                size
        );
    }

    public OrderDetailVO detail(Long id) {
        OrderRecord order = requireOrder(id);
        List<OrderItemVO> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, id)
                        .orderByAsc(OrderItem::getId))
                .stream()
                .map(this::toItemVO)
                .toList();

        OrderDetailVO detail = new OrderDetailVO();
        BeanUtils.copyProperties(toVO(order), detail);
        detail.setItems(items);
        return detail;
    }

    public void updateStatus(Long id, Integer nextStatus) {
        if (!STATUS_TEXT.containsKey(nextStatus)) {
            throw new BusinessException(400, "订单状态无效");
        }

        OrderRecord order = requireOrder(id);
        Integer currentStatus = order.getStatus();
        Set<Integer> allowedNextStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNextStatuses.contains(nextStatus)) {
            throw new BusinessException(400, "不允许这样流转订单状态");
        }

        order.setStatus(nextStatus);
        orderMapper.updateById(order);
    }

    public List<OrderStatusOption> listStatusOptions() {
        return List.of(
                new OrderStatusOption(PENDING_PAYMENT, STATUS_TEXT.get(PENDING_PAYMENT)),
                new OrderStatusOption(PENDING_SHIPMENT, STATUS_TEXT.get(PENDING_SHIPMENT)),
                new OrderStatusOption(SHIPPED, STATUS_TEXT.get(SHIPPED)),
                new OrderStatusOption(COMPLETED, STATUS_TEXT.get(COMPLETED)),
                new OrderStatusOption(CANCELLED, STATUS_TEXT.get(CANCELLED))
        );
    }

    private LambdaQueryWrapper<OrderRecord> buildQueryWrapper(OrderQueryRequest request) {
        return new LambdaQueryWrapper<OrderRecord>()
                .and(StringUtils.hasText(request.getKeyword()), wrapper -> wrapper
                        .like(OrderRecord::getOrderNo, request.getKeyword())
                        .or()
                        .like(OrderRecord::getCustomerName, request.getKeyword())
                        .or()
                        .like(OrderRecord::getCustomerPhone, request.getKeyword()))
                .eq(request.getStatus() != null, OrderRecord::getStatus, request.getStatus())
                .orderByDesc(OrderRecord::getCreatedAt)
                .orderByDesc(OrderRecord::getId);
    }

    private OrderRecord requireOrder(Long id) {
        OrderRecord order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private OrderVO toVO(OrderRecord order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(STATUS_TEXT.getOrDefault(order.getStatus(), "未知"));
        return vo;
    }

    private OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
}
