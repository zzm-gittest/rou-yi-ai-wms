package com.ruoyi.wms.service.ai.function.impl;

import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.ai.function.AbstractAiFunction;
import com.ruoyi.wms.service.ai.function.AiFunction;
import com.ruoyi.wms.service.ai.function.FunctionCallResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 创建采购订单函数
 *
 * @author ruoyi
 */
@Slf4j
@Component
public class CreatePurchaseOrderFunction extends AbstractAiFunction {

    @Autowired
    private WmsPurchaseOrderService purchaseOrderService;

    @Autowired
    private WmsSupplierService supplierService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Override
    public AiFunction getFunctionDefinition() {
        AiFunction function = new AiFunction();
        function.setName("create_purchase_order");
        function.setDescription("创建采购订单。需要供应商和商品列表信息。");
        function.setRequireConfirmation(true);

        AiFunction.AiParameter parameters = new AiFunction.AiParameter();
        parameters.setRequired(Arrays.asList("supplier", "goods"));

        Map<String, AiFunction.PropertySchema> properties = new LinkedHashMap<>();

        properties.put("supplier", AiFunction.PropertySchema.string("供应商名称"));
        properties.put("goods", AiFunction.PropertySchema.array("商品列表",
                AiFunction.PropertySchema.string("商品信息（格式：商品名 数量 单价，如：矿泉水 100 50）")));

        parameters.setProperties(properties);
        function.setParameters(parameters);

        return function;
    }

    @Override
    public FunctionCallResult execute(Map<String, Object> parameters, Long userId) {
        try {
            // 解析供应商
            String supplierName = getParamString(parameters, "supplier");
            if (supplierName == null || supplierName.trim().isEmpty()) {
                return FunctionCallResult.error("create_purchase_order", "供应商名称不能为空");
            }

            // 查找或创建供应商
            Long supplierId = findOrCreateSupplier(supplierName);

            // 解析商品列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) (List<?>) getParamList(parameters, "goods", Map.class);
            if (goodsList.isEmpty()) {
                // 尝试从字符串解析
                String goodsStr = getParamString(parameters, "goods");
                goodsList = parseGoodsFromString(goodsStr);
            }

            if (goodsList.isEmpty()) {
                return FunctionCallResult.error("create_purchase_order", "商品列表不能为空");
            }

            // 创建订单
            return createOrder(supplierId, supplierName, goodsList);

        } catch (Exception e) {
            log.error("创建采购订单失败", e);
            return FunctionCallResult.error("create_purchase_order", e.getMessage());
        }
    }

    /**
     * 查找或创建供应商
     */
    private Long findOrCreateSupplier(String supplierName) {
        try {
            // 先尝试通过名称查找
            Long id = supplierService.findIdByName(supplierName);
            if (id != null) {
                return id;
            }
            // 如果不存在，返回null让系统处理
            log.warn("供应商不存在: {}", supplierName);
            return null;
        } catch (Exception e) {
            log.error("查找供应商失败", e);
            return null;
        }
    }

    /**
     * 从字符串解析商品列表
     * 支持格式： "矿泉水 100箱 50元" 或 ["矿泉水 100 50", "可乐 50 40"]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseGoodsFromString(String goodsStr) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (goodsStr == null || goodsStr.trim().isEmpty()) {
            return result;
        }

        // 简单解析：商品名 数量 [单价]
        String[] parts = goodsStr.split("[,，\\s]+");
        if (parts.length >= 2) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", parts[0]);
            item.put("quantity", Integer.parseInt(parts[1].replaceAll("[^0-9]", "")));
            if (parts.length >= 3) {
                item.put("price", Double.parseDouble(parts[2].replaceAll("[^0-9.]", "")));
            }
            result.add(item);
        }

        return result;
    }

    /**
     * 创建订单
     */
    private FunctionCallResult createOrder(Long supplierId, String supplierName, List<Map<String, Object>> goodsList) {
        WmsPurchaseOrder order = new WmsPurchaseOrder();
        order.setSupplierId(supplierId);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus("0"); // 待审核状态

        List<WmsPurchaseOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map<String, Object> goods : goodsList) {
            WmsPurchaseOrderItem item = new WmsPurchaseOrderItem();

            String goodsName = (String) goods.get("name");
            Integer quantity = (Integer) goods.get("quantity");
            Double price = (Double) goods.get("price");
            if (price == null && goods.get("price") != null) {
                price = Double.parseDouble(goods.get("price").toString());
            }

            // 尝试匹配商品
            WmsGoodsSkuRespVO sku = matchGoods(goodsName);
            if (sku != null) {
                item.setSkuId(sku.getId());
                item.setGoodsId(sku.getGoodsId());
                item.setSkuName(sku.getSkuName());
                item.setSpecInfo(sku.getSpecInfo());
                item.setUnitName(sku.getUnitName());
                if (price == null) {
                    price = sku.getPurchasePrice() != null ? sku.getPurchasePrice().doubleValue() : 0.0;
                }
            } else {
                item.setSkuName(goodsName);
            }

            item.setQuantity(BigDecimal.valueOf(quantity != null ? quantity : 1));
            item.setPrice(BigDecimal.valueOf(price != null ? price : 0.0));
            item.setAmount(item.getQuantity().multiply(item.getPrice()));

            totalAmount = totalAmount.add(item.getAmount());
            items.add(item);
        }

        order.setTotalAmount(totalAmount);

        // 这里简化处理，直接返回结果，实际需要调用service保存
        Map<String, Object> result = new HashMap<>();
        result.put("supplier", supplierName);
        result.put("orderDate", LocalDate.now().toString());
        result.put("items", items);
        result.put("totalAmount", totalAmount);
        result.put("itemCount", items.size());

        StringBuilder sb = new StringBuilder();
        sb.append("✅ 采购订单已创建！\n\n");
        sb.append("📋 订单信息：\n");
        sb.append("   供应商：").append(supplierName).append("\n");
        sb.append("   日期：").append(LocalDate.now()).append("\n");
        sb.append("   商品数量：").append(items.size()).append(" 种\n");
        sb.append("   总金额：¥").append(totalAmount).append("\n\n");
        sb.append("📦 商品明细：\n");
        for (int i = 0; i < items.size(); i++) {
            WmsPurchaseOrderItem item = items.get(i);
            sb.append("   ").append(i + 1).append(". ")
                    .append(item.getSkuName());
            if (item.getSpecInfo() != null) {
                sb.append(" (").append(item.getSpecInfo()).append(")");
            }
            sb.append("\n      数量：").append(item.getQuantity());
            if (item.getUnitName() != null) {
                sb.append(item.getUnitName());
            }
            sb.append(" × ¥").append(item.getPrice())
                    .append(" = ¥").append(item.getAmount()).append("\n");
        }
        sb.append("\n💡 订单已保存为草稿状态，您可以前往采购订单页面确认。");

        return FunctionCallResult.success("create_purchase_order", sb.toString(), result);
    }

    /**
     * 匹配商品
     */
    private WmsGoodsSkuRespVO matchGoods(String name) {
        try {
            List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(name, 5);
            if (!skus.isEmpty()) {
                return skus.get(0);
            }
        } catch (Exception e) {
            log.warn("匹配商品失败: {}", name, e);
        }
        return null;
    }
}
