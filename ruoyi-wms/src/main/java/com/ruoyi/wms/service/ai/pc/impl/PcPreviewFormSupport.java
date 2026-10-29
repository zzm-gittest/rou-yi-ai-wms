package com.ruoyi.wms.service.ai.pc.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PcPreviewFormSupport {

    private final PcMultiBillChatServiceImpl owner;

    PcPreviewFormSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    void applyDirectPurchasePreviewForm(PcState state, Map<String, Object> formData) {
        Object supplierIdRaw = formData.get("supplierId");
        if (supplierIdRaw == null || StringUtils.isBlank(String.valueOf(supplierIdRaw))) {
            state.setSupplierId(null);
            state.setSupplierName(null);
            state.setSupplierInput(null);
        } else {
            Long supplierId = Long.valueOf(String.valueOf(supplierIdRaw));
            WmsSupplierRespVO supplier = owner.findSupplierById(supplierId);
            if (supplier == null) {
                throw new ServiceException("供应商不存在");
            }
            state.setSupplierId(supplier.getId());
            state.setSupplierName(supplier.getSupplierName());
            state.setSupplierInput(supplier.getSupplierName());
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("采购确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("采购订单至少保留一条商品明细");
        }

        List<ItemDraft> drafts = new ArrayList<>();
        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】单价必须大于0");
            }

            ItemDraft draft = new ItemDraft();
            draft.forceSkuId = sku.getId();
            draft.name = StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName());
            draft.unit = StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName());
            draft.quantity = quantity;
            draft.price = price;
            drafts.add(draft);

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), draft.name));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(draft.unit, sku.getUnitName()));
            line.setQuantity(quantity);
            line.setPrice(price);
            lines.add(line);
        }

        state.setItemDrafts(drafts);
        state.setPreparedLines(lines);
        state.setPreparedPoItems(owner.buildPurchaseOrderItemsFromLines(lines));
    }

    void applyPurchaseInPreviewForm(PcState state, Map<String, Object> formData) {
        Object supplierIdRaw = formData.get("supplierId");
        if (supplierIdRaw == null || StringUtils.isBlank(String.valueOf(supplierIdRaw))) {
            state.setSupplierId(null);
            state.setSupplierName(null);
            state.setSupplierInput(null);
        } else {
            Long supplierId = Long.valueOf(String.valueOf(supplierIdRaw));
            WmsSupplierRespVO supplier = owner.findSupplierById(supplierId);
            if (supplier == null) {
                throw new ServiceException("供应商不存在");
            }
            state.setSupplierId(supplier.getId());
            state.setSupplierName(supplier.getSupplierName());
            state.setSupplierInput(supplier.getSupplierName());
        }

        Object warehouseIdRaw = formData.get("warehouseId");
        if (warehouseIdRaw == null || StringUtils.isBlank(String.valueOf(warehouseIdRaw))) {
            state.setWarehouseId(null);
            state.setWarehouseName(null);
            state.setWarehouseInput(null);
        } else {
            Long warehouseId = Long.valueOf(String.valueOf(warehouseIdRaw));
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
            if (warehouse == null) {
                throw new ServiceException("仓库不存在");
            }
            state.setWarehouseId(warehouse.getId());
            state.setWarehouseName(warehouse.getWarehouseName());
            state.setWarehouseInput(warehouse.getWarehouseName());
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("采购入库确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("采购入库至少保留一条商品明细");
        }

        boolean requirePrice = !owner.hasLinkedSource(state);
        List<ItemDraft> drafts = new ArrayList<>();
        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }
            if (requirePrice && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】单价必须大于0");
            }

            ItemDraft draft = new ItemDraft();
            draft.forceSkuId = sku.getId();
            draft.name = StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName());
            draft.unit = StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName());
            draft.quantity = quantity;
            draft.price = price;
            drafts.add(draft);

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), draft.name));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(draft.unit, sku.getUnitName()));
            line.setQuantity(quantity);
            line.setPrice(price);
            lines.add(line);
        }

        state.setItemDrafts(drafts);
        state.setPreparedLines(lines);
        state.setPreparedPiItems(owner.buildPurchaseInItemsFromLines(lines));
    }

    void applyLinkedPurchaseInWarehouseForm(PcState state, Map<String, Object> formData) {
        if (!owner.hasLinkedSource(state)) {
            return;
        }
        if (!formData.containsKey("warehouseId")) {
            return;
        }
        Object warehouseIdRaw = formData.get("warehouseId");
        if (warehouseIdRaw == null || StringUtils.isBlank(String.valueOf(warehouseIdRaw))) {
            state.setWarehouseId(null);
            state.setWarehouseName(null);
            state.setWarehouseInput(null);
            return;
        }
        Long warehouseId = Long.valueOf(String.valueOf(warehouseIdRaw));
        WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
        if (warehouse == null) {
            throw new ServiceException("仓库不存在");
        }
        state.setWarehouseId(warehouse.getId());
        state.setWarehouseName(warehouse.getWarehouseName());
        state.setWarehouseInput(warehouse.getWarehouseName());
    }

    void applyPurchaseReturnPreviewForm(PcState state, Map<String, Object> formData) {
        Object supplierIdRaw = formData.get("supplierId");
        if (supplierIdRaw == null || StringUtils.isBlank(String.valueOf(supplierIdRaw))) {
            state.setSupplierId(null);
            state.setSupplierName(null);
            state.setSupplierInput(null);
        } else {
            Long supplierId = Long.valueOf(String.valueOf(supplierIdRaw));
            WmsSupplierRespVO supplier = owner.findSupplierById(supplierId);
            if (supplier == null) {
                throw new ServiceException("供应商不存在");
            }
            state.setSupplierId(supplier.getId());
            state.setSupplierName(supplier.getSupplierName());
            state.setSupplierInput(supplier.getSupplierName());
        }

        Object warehouseIdRaw = formData.get("warehouseId");
        if (warehouseIdRaw == null || StringUtils.isBlank(String.valueOf(warehouseIdRaw))) {
            state.setWarehouseId(null);
            state.setWarehouseName(null);
            state.setWarehouseInput(null);
        } else {
            Long warehouseId = Long.valueOf(String.valueOf(warehouseIdRaw));
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
            if (warehouse == null) {
                throw new ServiceException("仓库不存在");
            }
            state.setWarehouseId(warehouse.getId());
            state.setWarehouseName(warehouse.getWarehouseName());
            state.setWarehouseInput(warehouse.getWarehouseName());
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("采购退货确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("采购退货至少保留一条商品明细");
        }

        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName()));
            line.setQuantity(quantity);
            line.setPrice(price);
            lines.add(line);
        }

        state.setPreparedLines(lines);
    }

    /**
     * 应用销售订单确认卡片表单数据（客户ID + 商品明细含单价）
     */
    void applySalesOrderPreviewForm(PcState state, Map<String, Object> formData) {
        // 应用客户ID
        Object customerIdRaw = formData.get("customerId");
        if (customerIdRaw != null && StringUtils.isNotBlank(String.valueOf(customerIdRaw))) {
            Long customerId = Long.valueOf(String.valueOf(customerIdRaw));
            com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO customer = owner.findCustomerById(customerId);
            if (customer == null) {
                throw new ServiceException("客户不存在");
            }
            state.customerId = customer.getId();
            state.customerName = customer.getCustomerName();
            state.customerInput = customer.getCustomerName();
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("销售订单确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("销售订单至少保留一条商品明细");
        }

        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】单价必须大于0");
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName()));
            line.setQuantity(quantity);
            line.setPrice(price);
            lines.add(line);
        }

        state.setPreparedLines(lines);
    }

    /**
     * 应用销售出库确认卡片表单数据（客户ID + 仓库ID + 商品明细）
     */
    void applySalesOutPreviewForm(PcState state, Map<String, Object> formData) {
        // 应用客户ID
        Object customerIdRaw = formData.get("customerId");
        if (customerIdRaw != null && StringUtils.isNotBlank(String.valueOf(customerIdRaw))) {
            Long customerId = Long.valueOf(String.valueOf(customerIdRaw));
            com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO customer = owner.findCustomerById(customerId);
            if (customer == null) {
                throw new ServiceException("客户不存在");
            }
            state.customerId = customer.getId();
            state.customerName = customer.getCustomerName();
            state.customerInput = customer.getCustomerName();
        }

        // 应用仓库ID
        Object warehouseIdRaw = formData.get("warehouseId");
        if (warehouseIdRaw != null && StringUtils.isNotBlank(String.valueOf(warehouseIdRaw))) {
            Long warehouseId = Long.valueOf(String.valueOf(warehouseIdRaw));
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
            if (warehouse == null) {
                throw new ServiceException("仓库不存在");
            }
            state.setWarehouseId(warehouse.getId());
            state.setWarehouseName(warehouse.getWarehouseName());
            state.setWarehouseInput(warehouse.getWarehouseName());
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("销售出库确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("销售出库至少保留一条商品明细");
        }

        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName()));
            line.setQuantity(quantity);
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                line.setPrice(price);
            }
            lines.add(line);
        }

        state.setPreparedLines(lines);
    }

    /**
     * 应用销售退货直接模式确认卡片表单数据（客户ID + 仓库ID + 商品明细 + 备注）
     */
    void applySalesReturnPreviewForm(PcState state, Map<String, Object> formData) {
        // 应用客户ID
        Object customerIdRaw = formData.get("customerId");
        if (customerIdRaw != null && StringUtils.isNotBlank(String.valueOf(customerIdRaw))) {
            Long customerId = Long.valueOf(String.valueOf(customerIdRaw));
            com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO customer = owner.findCustomerById(customerId);
            if (customer == null) {
                throw new ServiceException("客户不存在");
            }
            state.customerId = customer.getId();
            state.customerName = customer.getCustomerName();
            state.customerInput = customer.getCustomerName();
        }

        // 应用仓库ID
        Object warehouseIdRaw = formData.get("warehouseId");
        if (warehouseIdRaw != null && StringUtils.isNotBlank(String.valueOf(warehouseIdRaw))) {
            Long warehouseId = Long.valueOf(String.valueOf(warehouseIdRaw));
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
            if (warehouse == null) {
                throw new ServiceException("仓库不存在");
            }
            state.setWarehouseId(warehouse.getId());
            state.setWarehouseName(warehouse.getWarehouseName());
            state.setWarehouseInput(warehouse.getWarehouseName());
        }

        // 应用备注
        Object remarkRaw = formData.get("remark");
        if (remarkRaw != null && StringUtils.isNotBlank(String.valueOf(remarkRaw))) {
            state.reason = String.valueOf(remarkRaw).trim();
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("销售退货确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("销售退货至少保留一条商品明细");
        }

        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal price = toBigDecimal(item.get("price"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品【" + StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()) + "】数量必须大于0");
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName()));
            line.setQuantity(quantity);
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                line.setPrice(price);
            }
            lines.add(line);
        }

        state.setPreparedLines(lines);
    }

    /**
     * 应用库存调拨确认卡片表单数据（调入仓库ID + 商品明细数量）
     */
    void applyStockTransferPreviewForm(PcState state, Map<String, Object> formData) {
        // 应用调入仓库ID
        Object inWarehouseIdRaw = formData.get("inWarehouseId");
        if (inWarehouseIdRaw != null && StringUtils.isNotBlank(String.valueOf(inWarehouseIdRaw))) {
            Long inWarehouseId = Long.valueOf(String.valueOf(inWarehouseIdRaw));
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(inWarehouseId);
            if (warehouse == null) {
                throw new ServiceException("调入仓库不存在");
            }
            if (state.outWarehouseId != null && state.outWarehouseId.equals(inWarehouseId)) {
                throw new ServiceException("调入仓库不能与调出仓库相同");
            }
            state.inWarehouseId = warehouse.getId();
            state.inWarehouseName = warehouse.getWarehouseName();
            state.inWarehouseInput = warehouse.getWarehouseName();
        }
        if (state.inWarehouseId == null) {
            throw new ServiceException("请选择调入仓库");
        }

        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(formData.get("items"),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("库存调拨确认信息格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("库存调拨至少保留一条商品明细");
        }

        List<LineDraft> lines = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null) {
                throw new ServiceException("商品信息不完整，请重新选择商品");
            }
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在");
            }
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            BigDecimal maxQty = toBigDecimal(item.get("maxQty"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("商品" + quoteCn(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName())) + "数量必须大于0");
            }
            if (maxQty != null && quantity.compareTo(maxQty) > 0) {
                throw new ServiceException("商品" + quoteCn(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName())) + "调拨数量不能超过库存数量(" + maxQty + ")");
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(StringUtils.defaultIfBlank(PcMultiBillChatServiceImpl.str(item.get("unit")), sku.getUnitName()));
            line.setQuantity(quantity);
            line.setMaxQty(maxQty);
            line.setPrice(toBigDecimal(item.get("price")));
            lines.add(line);
        }

        state.setPreparedLines(lines);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String quoteCn(String text) {
        return "\u201c" + text + "\u201d";
    }
}
