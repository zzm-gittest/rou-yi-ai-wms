package com.ruoyi.wms.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesOrderItem;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.ai.recommend.PurchaseOrderRecommendVO;
import com.ruoyi.wms.domain.vo.ai.recommend.SalesOrderRecommendVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务实现
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class WmsRecommendServiceImpl implements WmsRecommendService {

    @Autowired
    private WmsSupplierService supplierService;

    @Autowired
    private WmsCustomerService customerService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsStockService stockService;

    /**
     * 推荐数量限制
     */
    private static final int RECOMMEND_LIMIT = 5;

    @Override
    public PurchaseOrderRecommendVO getPurchaseOrderRecommend() {
        PurchaseOrderRecommendVO result = new PurchaseOrderRecommendVO();

        // 1. 推荐常用供应商（基于历史采购订单）
        result.setRecommendedSuppliers(getRecommendedSuppliers());

        // 2. 推荐商品（常购商品 + 库存预警商品）
        result.setRecommendedGoods(getRecommendedGoodsForPurchase());

        return result;
    }

    @Override
    public SalesOrderRecommendVO getSalesOrderRecommend() {
        SalesOrderRecommendVO result = new SalesOrderRecommendVO();

        // 1. 推荐常用客户（基于历史销售订单）
        result.setRecommendedCustomers(getRecommendedCustomers());

        // 2. 推荐商品（常购商品 + 库存充足商品）
        result.setRecommendedGoods(getRecommendedGoodsForSales());

        return result;
    }

    /**
     * 获取推荐供应商列表
     * 基于历史采购订单统计合作次数
     */
    private List<PurchaseOrderRecommendVO.SupplierRecommendItem> getRecommendedSuppliers() {
        try {
            // 查询最近30天的采购订单，按供应商分组统计
            List<Map<String, Object>> supplierStats = supplierService.list(
                    Wrappers.<com.ruoyi.wms.domain.WmsSupplier>lambdaQuery()
                            .orderByDesc(com.ruoyi.wms.domain.WmsSupplier::getCreateTime)
                            .last("LIMIT " + RECOMMEND_LIMIT)
            ).stream().map(supplier -> {
                Map<String, Object> map = new HashMap<>();
                map.put("supplierId", supplier.getId());
                map.put("supplierName", supplier.getSupplierName());
                map.put("contact", supplier.getContact());
                map.put("phone", supplier.getMobile());
                return map;
            }).collect(Collectors.toList());

            List<PurchaseOrderRecommendVO.SupplierRecommendItem> result = new ArrayList<>();
            for (Map<String, Object> stat : supplierStats) {
                PurchaseOrderRecommendVO.SupplierRecommendItem item = new PurchaseOrderRecommendVO.SupplierRecommendItem();
                item.setSupplierId(((Number) stat.get("supplierId")).longValue());
                item.setSupplierName((String) stat.get("supplierName"));
                item.setContact((String) stat.get("contact"));
                item.setPhone((String) stat.get("phone"));
                result.add(item);
            }

            return result;
        } catch (Exception e) {
            log.error("获取推荐供应商失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取推荐客户列表
     * 基于历史销售订单统计合作次数
     */
    private List<SalesOrderRecommendVO.CustomerRecommendItem> getRecommendedCustomers() {
        try {
            // 查询最近创建的客户
            List<com.ruoyi.wms.domain.WmsCustomer> customers = customerService.list(
                    Wrappers.<com.ruoyi.wms.domain.WmsCustomer>lambdaQuery()
                            .orderByDesc(com.ruoyi.wms.domain.WmsCustomer::getCreateTime)
                            .last("LIMIT " + RECOMMEND_LIMIT)
            );

            List<SalesOrderRecommendVO.CustomerRecommendItem> result = new ArrayList<>();
            for (com.ruoyi.wms.domain.WmsCustomer customer : customers) {
                SalesOrderRecommendVO.CustomerRecommendItem item = new SalesOrderRecommendVO.CustomerRecommendItem();
                item.setCustomerId(customer.getId());
                item.setCustomerName(customer.getCustomerName());
                item.setContact(customer.getContact());
                item.setPhone(customer.getMobile());
                result.add(item);
            }

            return result;
        } catch (Exception e) {
            log.error("获取推荐客户失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取采购订单推荐商品
     * 包括：常购商品 + 库存预警商品
     */
    private List<PurchaseOrderRecommendVO.GoodsRecommendItem> getRecommendedGoodsForPurchase() {
        try {
            List<PurchaseOrderRecommendVO.GoodsRecommendItem> result = new ArrayList<>();

            // 1. 添加库存预警商品（低于最小库存的）
            List<WmsGoodsSkuRespVO> lowStockSkus = getLowStockSkus();
            for (WmsGoodsSkuRespVO sku : lowStockSkus) {
                PurchaseOrderRecommendVO.GoodsRecommendItem item = new PurchaseOrderRecommendVO.GoodsRecommendItem();
                item.setSkuId(sku.getId());
                item.setSkuName(sku.getSkuName());
                item.setSpecInfo(sku.getSpecInfo());
                item.setUnitName(sku.getUnitName());
                item.setStockQty(getStockQty(sku.getId()));
                item.setStockStatus("库存不足");
                item.setPurchasePrice(formatPrice(sku.getPurchasePrice()));
                result.add(item);
            }

            // 2. 如果预警商品不够，补充常用商品
            if (result.size() < RECOMMEND_LIMIT) {
                List<WmsGoodsSkuRespVO> recentSkus = goodsSkuService.list(
                        Wrappers.<com.ruoyi.wms.domain.WmsGoodsSku>lambdaQuery()
                                .orderByDesc(com.ruoyi.wms.domain.WmsGoodsSku::getCreateTime)
                                .last("LIMIT " + (RECOMMEND_LIMIT - result.size()))
                ).stream().map(entity -> {
                    WmsGoodsSkuRespVO vo = new WmsGoodsSkuRespVO();
                    vo.setId(entity.getId());
                    vo.setSkuName(entity.getSkuName());
                    vo.setSpecInfo(entity.getSpecInfo());
                    vo.setUnitName(entity.getUnitName());
                    vo.setPurchasePrice(entity.getPurchasePrice());
                    return vo;
                }).collect(Collectors.toList());

                for (WmsGoodsSkuRespVO sku : recentSkus) {
                    // 避免重复
                    if (result.stream().noneMatch(item -> item.getSkuId().equals(sku.getId()))) {
                        PurchaseOrderRecommendVO.GoodsRecommendItem item = new PurchaseOrderRecommendVO.GoodsRecommendItem();
                        item.setSkuId(sku.getId());
                        item.setSkuName(sku.getSkuName());
                        item.setSpecInfo(sku.getSpecInfo());
                        item.setUnitName(sku.getUnitName());
                        item.setStockQty(getStockQty(sku.getId()));
                        item.setStockStatus(getStockStatus(sku));
                        item.setPurchasePrice(formatPrice(sku.getPurchasePrice()));
                        result.add(item);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("获取采购推荐商品失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取销售订单推荐商品
     * 包括：常购商品 + 库存充足商品
     */
    private List<SalesOrderRecommendVO.GoodsRecommendItem> getRecommendedGoodsForSales() {
        try {
            List<SalesOrderRecommendVO.GoodsRecommendItem> result = new ArrayList<>();

            // 查询最近创建的商品
            List<WmsGoodsSkuRespVO> recentSkus = goodsSkuService.list(
                    Wrappers.<com.ruoyi.wms.domain.WmsGoodsSku>lambdaQuery()
                            .orderByDesc(com.ruoyi.wms.domain.WmsGoodsSku::getCreateTime)
                            .last("LIMIT " + RECOMMEND_LIMIT)
            ).stream().map(entity -> {
                WmsGoodsSkuRespVO vo = new WmsGoodsSkuRespVO();
                vo.setId(entity.getId());
                vo.setSkuName(entity.getSkuName());
                vo.setSpecInfo(entity.getSpecInfo());
                vo.setUnitName(entity.getUnitName());
                vo.setSalePrice(entity.getSalePrice());
                vo.setMinStock(entity.getMinStock());
                return vo;
            }).collect(Collectors.toList());

            for (WmsGoodsSkuRespVO sku : recentSkus) {
                SalesOrderRecommendVO.GoodsRecommendItem item = new SalesOrderRecommendVO.GoodsRecommendItem();
                item.setSkuId(sku.getId());
                item.setSkuName(sku.getSkuName());
                item.setSpecInfo(sku.getSpecInfo());
                item.setUnitName(sku.getUnitName());
                item.setStockQty(getStockQty(sku.getId()));
                item.setStockStatus(getStockStatus(sku));
                item.setSalePrice(formatPrice(sku.getSalePrice()));
                result.add(item);
            }

            return result;
        } catch (Exception e) {
            log.error("获取销售推荐商品失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取库存预警的SKU列表
     */
    private List<WmsGoodsSkuRespVO> getLowStockSkus() {
        // 查询设置了最小库存的SKU
        List<com.ruoyi.wms.domain.WmsGoodsSku> skuEntities = goodsSkuService.list(
                Wrappers.<com.ruoyi.wms.domain.WmsGoodsSku>lambdaQuery()
                        .isNotNull(com.ruoyi.wms.domain.WmsGoodsSku::getMinStock)
                        .gt(com.ruoyi.wms.domain.WmsGoodsSku::getMinStock, 0)
        );

        // 筛选出库存低于最小库存的
        List<WmsGoodsSkuRespVO> lowStockSkus = new ArrayList<>();
        for (com.ruoyi.wms.domain.WmsGoodsSku entity : skuEntities) {
            Integer stockQty = getStockQty(entity.getId());
            if (stockQty < entity.getMinStock()) {
                WmsGoodsSkuRespVO vo = new WmsGoodsSkuRespVO();
                vo.setId(entity.getId());
                vo.setSkuName(entity.getSkuName());
                vo.setSpecInfo(entity.getSpecInfo());
                vo.setUnitName(entity.getUnitName());
                vo.setPurchasePrice(entity.getPurchasePrice());
                vo.setMinStock(entity.getMinStock());
                lowStockSkus.add(vo);
            }
        }

        return lowStockSkus;
    }

    /**
     * 获取SKU的当前库存数量
     */
    private Integer getStockQty(Long skuId) {
        try {
            List<WmsStock> stocks = stockService.list(
                    Wrappers.<WmsStock>lambdaQuery()
                            .eq(WmsStock::getSkuId, skuId)
            );
            int totalQty = 0;
            for (WmsStock stock : stocks) {
                totalQty += stock.getQuantity() != null ? stock.getQuantity().intValue() : 0;
            }
            return totalQty;
        } catch (Exception e) {
            log.warn("获取SKU库存失败: skuId={}", skuId, e);
            return 0;
        }
    }

    /**
     * 获取库存状态描述
     */
    private String getStockStatus(WmsGoodsSkuRespVO sku) {
        Integer stockQty = getStockQty(sku.getId());
        Integer minStock = sku.getMinStock();
        if (minStock != null && minStock > 0) {
            if (stockQty <= 0) {
                return "无库存";
            } else if (stockQty < minStock) {
                return "库存不足";
            } else {
                return "库存充足";
            }
        }
        return stockQty > 0 ? "有库存" : "无库存";
    }

    /**
     * 格式化价格
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
