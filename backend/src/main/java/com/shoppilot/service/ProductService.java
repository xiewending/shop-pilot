package com.shoppilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppilot.common.BusinessException;
import com.shoppilot.config.RedisCacheConfig;
import com.shoppilot.dto.ProductQueryRequest;
import com.shoppilot.dto.ProductSaveRequest;
import com.shoppilot.entity.Category;
import com.shoppilot.entity.Product;
import com.shoppilot.mapper.CategoryMapper;
import com.shoppilot.mapper.ProductMapper;
import com.shoppilot.vo.HotProductVO;
import com.shoppilot.vo.PageResult;
import com.shoppilot.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String HOT_PRODUCT_RANK_KEY = "product:hot:rank";
    private static final Duration HOT_PRODUCT_RANK_TTL = Duration.ofDays(7);

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductService(ProductMapper productMapper, CategoryMapper categoryMapper, RedisTemplate<String, Object> redisTemplate) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    public PageResult<ProductVO> page(ProductQueryRequest request) {
        long requestedPage = request.getPage() == null ? 1L : request.getPage();
        long requestedSize = request.getSize() == null ? 10L : request.getSize();
        long page = Math.max(requestedPage, 1L);
        long size = Math.min(Math.max(requestedSize, 1L), 100L);

        Long total = productMapper.selectCount(buildQueryWrapper(request));
        var records = productMapper.selectList(buildQueryWrapper(request)
                .last("limit " + ((page - 1) * size) + ", " + size));

        Map<Long, Category> categories = loadCategoryMap(records);

        return new PageResult<>(
                records.stream().map(product -> toVO(product, categories)).toList(),
                total,
                page,
                size
        );
    }

    @Cacheable(value = RedisCacheConfig.PRODUCT_DETAIL_CACHE, key = "#id")
    public ProductVO getById(Long id) {
        Product product = requireProduct(id);
        Category category = categoryMapper.selectById(product.getCategoryId());
        return toVO(product, category == null ? Map.of() : Map.of(product.getCategoryId(), category));
    }

    public void recordProductView(Long id) {
        incrementHotScore(id);
    }

    @Cacheable(value = RedisCacheConfig.HOT_PRODUCTS_CACHE, key = "#limit")
    public List<HotProductVO> listHotProducts(Integer limit) {
        int size = limit == null ? 10 : Math.min(Math.max(limit, 1), 50);
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_PRODUCT_RANK_KEY, 0, size - 1);
        if (tuples == null || tuples.isEmpty()) {
            return listDefaultHotProducts(size);
        }

        List<Long> ids = tuples.stream()
                .map(tuple -> Long.valueOf(String.valueOf(tuple.getValue())))
                .toList();
        Map<Long, Double> scoreMap = tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> Long.valueOf(String.valueOf(tuple.getValue())),
                        tuple -> tuple.getScore() == null ? 0D : tuple.getScore()
                ));
        Map<Long, Product> productMap = productMapper.selectBatchIds(ids)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Category> categoryMap = loadCategoryMap(new ArrayList<>(productMap.values()));

        return ids.stream()
                .map(productMap::get)
                .filter(product -> product != null && product.getStatus() != null && product.getStatus() == 1)
                .map(product -> toHotVO(product, categoryMap, scoreMap.getOrDefault(product.getId(), 0D)))
                .toList();
    }

    @CacheEvict(value = RedisCacheConfig.HOT_PRODUCTS_CACHE, allEntries = true)
    public Long create(ProductSaveRequest request) {
        ensureCategoryExists(request.getCategoryId());
        Product product = new Product();
        fillProduct(product, request);
        productMapper.insert(product);
        return product.getId();
    }

    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.PRODUCT_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = RedisCacheConfig.HOT_PRODUCTS_CACHE, allEntries = true)
    })
    public void update(Long id, ProductSaveRequest request) {
        ensureCategoryExists(request.getCategoryId());
        Product product = requireProduct(id);
        fillProduct(product, request);
        productMapper.updateById(product);
    }

    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.PRODUCT_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = RedisCacheConfig.HOT_PRODUCTS_CACHE, allEntries = true)
    })
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "商品状态只能是 0 或 1");
        }
        Product product = requireProduct(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.PRODUCT_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = RedisCacheConfig.HOT_PRODUCTS_CACHE, allEntries = true)
    })
    public void delete(Long id) {
        requireProduct(id);
        productMapper.deleteById(id);
        redisTemplate.opsForZSet().remove(HOT_PRODUCT_RANK_KEY, String.valueOf(id));
    }

    private void incrementHotScore(Long id) {
        redisTemplate.opsForZSet().incrementScore(HOT_PRODUCT_RANK_KEY, String.valueOf(id), 1D);
        redisTemplate.expire(HOT_PRODUCT_RANK_KEY, HOT_PRODUCT_RANK_TTL);
    }

    private List<HotProductVO> listDefaultHotProducts(int size) {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getUpdatedAt)
                .orderByDesc(Product::getId)
                .last("limit " + size));
        Map<Long, Category> categoryMap = loadCategoryMap(products);
        return products.stream()
                .map(product -> toHotVO(product, categoryMap, 0D))
                .toList();
    }

    private LambdaQueryWrapper<Product> buildQueryWrapper(ProductQueryRequest request) {
        return new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(request.getKeyword()), Product::getName, request.getKeyword())
                .eq(request.getCategoryId() != null, Product::getCategoryId, request.getCategoryId())
                .eq(request.getStatus() != null, Product::getStatus, request.getStatus())
                .orderByDesc(Product::getUpdatedAt)
                .orderByDesc(Product::getId);
    }

    private Map<Long, Category> loadCategoryMap(List<Product> records) {
        if (records.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(records.stream().map(Product::getCategoryId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private void fillProduct(Product product, ProductSaveRequest request) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    private void ensureCategoryExists(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(400, "商品分类无效");
        }
    }

    private ProductVO toVO(Product product, Map<Long, Category> categories) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        Category category = categories.get(product.getCategoryId());
        vo.setCategoryName(category == null ? "-" : category.getName());
        return vo;
    }

    private HotProductVO toHotVO(Product product, Map<Long, Category> categories, Double score) {
        HotProductVO vo = new HotProductVO();
        BeanUtils.copyProperties(product, vo);
        Category category = categories.get(product.getCategoryId());
        vo.setCategoryName(category == null ? "-" : category.getName());
        vo.setScore(score);
        return vo;
    }
}
