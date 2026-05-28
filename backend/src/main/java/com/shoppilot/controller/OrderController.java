package com.shoppilot.controller;

import com.shoppilot.common.ApiResponse;
import com.shoppilot.dto.OrderQueryRequest;
import com.shoppilot.dto.OrderStatusRequest;
import com.shoppilot.service.OrderService;
import com.shoppilot.vo.OrderDetailVO;
import com.shoppilot.vo.OrderStatusOption;
import com.shoppilot.vo.OrderVO;
import com.shoppilot.vo.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<PageResult<OrderVO>> page(OrderQueryRequest request) {
        return ApiResponse.success(orderService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.detail(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        orderService.updateStatus(id, request.getStatus());
        return ApiResponse.success(null);
    }

    @GetMapping("/status-options")
    public ApiResponse<List<OrderStatusOption>> statusOptions() {
        return ApiResponse.success(orderService.listStatusOptions());
    }
}
