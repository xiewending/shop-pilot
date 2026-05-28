package com.shoppilot.controller;

import com.shoppilot.common.ApiResponse;
import com.shoppilot.dto.ProductQueryRequest;
import com.shoppilot.dto.ProductSaveRequest;
import com.shoppilot.dto.ProductStatusRequest;
import com.shoppilot.service.ProductService;
import com.shoppilot.vo.HotProductVO;
import com.shoppilot.vo.PageResult;
import com.shoppilot.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResult<ProductVO>> page(ProductQueryRequest request) {
        return ApiResponse.success(productService.page(request));
    }

    @GetMapping("/hot")
    public ApiResponse<List<HotProductVO>> hotProducts(@RequestParam(required = false) Integer limit) {
        return ApiResponse.success(productService.listHotProducts(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVO> detail(@PathVariable Long id) {
        ProductVO product = productService.getById(id);
        productService.recordProductView(id);
        return ApiResponse.success(product);
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        productService.update(id, request);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusRequest request) {
        productService.updateStatus(id, request.getStatus());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success(null);
    }
}
