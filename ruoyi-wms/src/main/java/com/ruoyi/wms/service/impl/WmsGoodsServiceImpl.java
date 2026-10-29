package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsPurchaseInItem;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.WmsPurchaseReturnItem;
import com.ruoyi.wms.domain.WmsSalesOrderItem;
import com.ruoyi.wms.domain.WmsSalesOutItem;
import com.ruoyi.wms.domain.WmsSalesReturnItem;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockCheckItem;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsGoodsCategory;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsStockTransferItem;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import com.ruoyi.wms.domain.vo.goods.*;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsPageReqVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsRespVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.mapper.WmsGoodsMapper;
import com.ruoyi.wms.mapper.WmsGoodsSkuMapper;
import com.ruoyi.wms.mapper.WmsPurchaseInItemMapper;
import com.ruoyi.wms.mapper.WmsPurchaseOrderItemMapper;
import com.ruoyi.wms.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderItemMapper;
import com.ruoyi.wms.mapper.WmsSalesOutItemMapper;
import com.ruoyi.wms.mapper.WmsSalesReturnItemMapper;
import com.ruoyi.wms.mapper.WmsStockCheckItemMapper;
import com.ruoyi.wms.mapper.WmsStockLogMapper;
import com.ruoyi.wms.mapper.WmsStockMapper;
import com.ruoyi.wms.mapper.WmsStockTransferItemMapper;
import com.ruoyi.wms.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 商品表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsGoodsServiceImpl extends ServiceImpl<WmsGoodsMapper, WmsGoods> implements WmsGoodsService {

    private static final Logger log = LoggerFactory.getLogger(WmsGoodsServiceImpl.class);

    private final WmsGoodsCategoryService categoryService;
    private final WmsGoodsSkuMapper skuMapper;
    private final WmsGoodsSkuService goodsSkuService;
    private final WmsUnitService unitService;
    private final WmsUserOnboardingService onboardingService;

    @Autowired
    private WmsPurchaseInItemMapper purchaseInItemMapper;
    @Autowired
    private WmsPurchaseOrderItemMapper purchaseOrderItemMapper;
    @Autowired
    private WmsPurchaseReturnItemMapper purchaseReturnItemMapper;
    @Autowired
    private WmsSalesOrderItemMapper salesOrderItemMapper;
    @Autowired
    private WmsSalesOutItemMapper salesOutItemMapper;
    @Autowired
    private WmsSalesReturnItemMapper salesReturnItemMapper;
    @Autowired
    private WmsStockCheckItemMapper stockCheckItemMapper;
    @Autowired
    private WmsStockTransferItemMapper stockTransferItemMapper;
    @Autowired
    private WmsStockMapper stockMapper;
    @Autowired
    private WmsStockLogMapper stockLogMapper;

    public WmsGoodsServiceImpl(WmsGoodsCategoryService categoryService,
                               WmsGoodsSkuMapper skuMapper,
                               WmsGoodsSkuService goodsSkuService,
                               WmsUnitService unitService,
                               WmsUserOnboardingService onboardingService) {
        this.categoryService = categoryService;
        this.skuMapper = skuMapper;
        this.goodsSkuService = goodsSkuService;
        this.unitService = unitService;
        this.onboardingService = onboardingService;
    }

    @Override
    public IPage<WmsGoodsRespVO> page(WmsGoodsPageReqVO reqVO) {
        LambdaQueryWrapper<WmsGoods> wrapper = new LambdaQueryWrapper<WmsGoods>()
                .eq(WmsGoods::getDelFlag, false)
                .like(StringUtils.isNotEmpty(reqVO.getGoodsCode()), WmsGoods::getGoodsCode, reqVO.getGoodsCode())
                .like(StringUtils.isNotEmpty(reqVO.getGoodsName()), WmsGoods::getGoodsName, reqVO.getGoodsName())
                .eq(reqVO.getCategoryId() != null, WmsGoods::getCategoryId, reqVO.getCategoryId())
                .eq(StringUtils.isNotEmpty(reqVO.getBrand()), WmsGoods::getBrand, reqVO.getBrand())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsGoods::getStatus, reqVO.getStatus())
                .orderByDesc(WmsGoods::getCreateTime);

        Page<WmsGoods> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsGoodsRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<WmsGoodsRespVO> respVOList = VoUtils.convertList(page.getRecords(), WmsGoodsRespVO.class);

        // 填充分类和单位名称
        fillBaseInfo(respVOList);

        resultPage.setRecords(respVOList);
        return resultPage;
    }

    @Override
    public List<WmsGoodsRespVO> selectList() {
        LambdaQueryWrapper<WmsGoods> wrapper = new LambdaQueryWrapper<WmsGoods>()
                .eq(WmsGoods::getDelFlag, false)
                .eq(WmsGoods::getStatus, "0")
                .orderByDesc(WmsGoods::getCreateTime);
        List<WmsGoodsRespVO> respVOList = VoUtils.convertList(list(wrapper), WmsGoodsRespVO.class);

        // 填充分类和单位名称
        fillBaseInfo(respVOList);

        return respVOList;
    }

    @Override
    public WmsGoodsRespVO get(Long id) {
        WmsGoods goods = getById(id);
        WmsGoodsRespVO respVO = VoUtils.convert(goods, WmsGoodsRespVO.class);

        // 填充分类和单位名称
        if (respVO != null) {
            if (respVO.getCategoryId() != null) {
                WmsGoodsCategory category = categoryService.getById(respVO.getCategoryId());
                if (category != null) {
                    respVO.setCategoryName(category.getCategoryName());
                }
            }
            if (respVO.getUnitId() != null) {
                WmsUnit unit = unitService.getById(respVO.getUnitId());
                if (unit != null) {
                    respVO.setUnitName(unit.getUnitName());
                }
            }
        }

        return respVO;
    }

    @Override
    public WmsGoodsRespVO getDetailWithSkus(Long id) {
        WmsGoods goods = getById(id);
        WmsGoodsRespVO respVO = VoUtils.convert(goods, WmsGoodsRespVO.class);

        // 填充分类和单位名称
        if (respVO != null) {
            if (respVO.getCategoryId() != null) {
                WmsGoodsCategory category = categoryService.getById(respVO.getCategoryId());
                if (category != null) {
                    respVO.setCategoryName(category.getCategoryName());
                }
            }
            if (respVO.getUnitId() != null) {
                WmsUnit unit = unitService.getById(respVO.getUnitId());
                if (unit != null) {
                    respVO.setUnitName(unit.getUnitName());
                }
            }
        }

        // 填充SKU列表
        if (respVO != null) {
            List<com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO> skuList =
                goodsSkuService.listByGoodsId(id);
            respVO.setSkuList(skuList);
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsGoodsSaveReqVO reqVO) {
        WmsGoods goods = VoUtils.toEntity(reqVO, WmsGoods.class);

        // 手动转换categoryId（String转Long，BeanUtils.copyProperties可能转换失败）
        if (StringUtils.isNotEmpty(reqVO.getCategoryId())) {
            try {
                goods.setCategoryId(Long.parseLong(reqVO.getCategoryId()));
            } catch (NumberFormatException e) {
                // 转换失败时使用默认值
                goods.setCategoryId(null);
            }
        }

        // 如果商品编码为空，自动生成
        if (StringUtils.isEmpty(goods.getGoodsCode())) {
            goods.setGoodsCode(generateGoodsCodeInternal(reqVO.getCategoryId()));
        }

        save(goods);
        saveOrReplaceSkus(goods.getId(), reqVO.getSkuList(), Boolean.TRUE.equals(reqVO.getSyncSkuList()));

        tryCompleteAddGoodsOnboarding(goods.getId());
        return goods.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsGoodsSaveReqVO reqVO) {
        WmsGoods goods = VoUtils.toEntity(reqVO, WmsGoods.class);

        // 手动转换categoryId（String转Long，BeanUtils.copyProperties可能转换失败）
        if (StringUtils.isNotEmpty(reqVO.getCategoryId())) {
            try {
                goods.setCategoryId(Long.parseLong(reqVO.getCategoryId()));
            } catch (NumberFormatException e) {
                // 转换失败时使用默认值
                goods.setCategoryId(null);
            }
        }

        updateById(goods);
        saveOrReplaceSkus(goods.getId(), reqVO.getSkuList(), Boolean.TRUE.equals(reqVO.getSyncSkuList()));
        tryCompleteAddGoodsOnboarding(goods.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsGoods goods = getById(id);
        if (goods == null) {
            throw new ServiceException("商品不存在");
        }

        if (hasHistoricalRelation(id)) {
            throw new ServiceException("商品已关联历史单据或库存记录，无法删除。请改为停用商品。");
        }

        // 先删除该商品下的SKU，再删除商品本身。
        LambdaQueryWrapper<WmsGoodsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmsGoodsSku::getGoodsId, id);
        skuMapper.delete(wrapper);

        removeById(id);
    }

    @Override
    public String generateGoodsCode(String categoryId) {
        return generateGoodsCodeInternal(categoryId);
    }

    @Override
    public String generateSkuCode(Long goodsId) {
        // SKU编码规则：SK + 商品ID(8位) + 2位序号
        String prefix = "SK" + String.format("%08d", goodsId);

        String lastSkuCode = skuMapper.selectMaxSkuCodeIncludingDeleted(goodsId, prefix);
        int nextSeq = 1;
        if (StringUtils.isNotBlank(lastSkuCode) && lastSkuCode.length() > prefix.length()) {
            String suffix = lastSkuCode.substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignore) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%02d", nextSeq);
    }

    /**
     * 自动生成商品编码（内部方法）
     * 规则：SP + 分类ID(4位) + 4位序号
     * 例如：分类1 -> SP00010001，分类106 -> SP00010601
     */
    private String generateGoodsCodeInternal(String categoryId) {
        // 获取分类编码前缀
        String prefix = "SP";
        if (StringUtils.isNotEmpty(categoryId)) {
            try {
                // 使用分类ID作为前缀，补齐4位
                prefix = "SP" + String.format("%04d", Long.parseLong(categoryId));
            } catch (NumberFormatException e) {
                prefix = "SP0001";
            }
        }

        // 查询该分类下最后一个商品的序号
        LambdaQueryWrapper<WmsGoods> wrapper = new LambdaQueryWrapper<WmsGoods>()
                .likeRight(WmsGoods::getGoodsCode, prefix)
                .orderByDesc(WmsGoods::getGoodsCode)
                .last("LIMIT 1");

        WmsGoods lastGoods = getOne(wrapper);
        int nextSeq = 1;
        if (lastGoods != null && lastGoods.getGoodsCode() != null) {
            String codeStr = lastGoods.getGoodsCode().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(codeStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }

        // 生成新编码：前缀 + 4位序号
        return prefix + String.format("%04d", nextSeq);
    }

    /**
     * 填充分类名称
     */
    private void fillBaseInfo(List<WmsGoodsRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取所有分类ID
        List<Long> categoryIds = list.stream()
                .map(WmsGoodsRespVO::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return;
        }

        // 批量查询分类
        List<WmsGoodsCategory> categories = categoryService.listByIds(categoryIds);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(WmsGoodsCategory::getId, WmsGoodsCategory::getCategoryName));

        // 填充分类名称
        list.forEach(item -> {
            if (item.getCategoryId() != null) {
                item.setCategoryName(categoryMap.get(item.getCategoryId()));
            }
        });

        // 获取所有单位ID
        List<Long> unitIds = list.stream()
                .map(WmsGoodsRespVO::getUnitId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (!unitIds.isEmpty()) {
            List<WmsUnit> units = unitService.listByIds(unitIds);
            Map<Long, String> unitMap = units.stream()
                    .collect(Collectors.toMap(WmsUnit::getId, WmsUnit::getUnitName));
            list.forEach(item -> {
                if (item.getUnitId() != null) {
                    item.setUnitName(unitMap.get(item.getUnitId()));
                }
            });
        }
    }

    private void saveOrReplaceSkus(Long goodsId, List<WmsGoodsSkuSaveReqVO> skuList, boolean syncSkuList) {
        if (goodsId == null || !syncSkuList || skuList == null) {
            return;
        }

        // 采用增量同步，避免"先删后建"触发逻辑删除记录的唯一键冲突。
        List<WmsGoodsSku> existingList = skuMapper.selectList(new LambdaQueryWrapper<WmsGoodsSku>()
                .eq(WmsGoodsSku::getGoodsId, goodsId));
        Map<Long, WmsGoodsSku> existingMap = existingList.stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, item -> item));
        Set<Long> retainedIds = new HashSet<>();

        for (WmsGoodsSkuSaveReqVO skuReq : skuList) {
            if (skuReq == null || StringUtils.isBlank(skuReq.getSkuName())) {
                continue;
            }

            skuReq.setGoodsId(goodsId);
            if (skuReq.getStatus() == null) {
                skuReq.setStatus("0");
            }
            if (skuReq.getMinStock() == null) {
                skuReq.setMinStock(0);
            }
            if (skuReq.getMaxStock() == null) {
                skuReq.setMaxStock(999999);
            }

            if (skuReq.getId() != null) {
                WmsGoodsSku existing = existingMap.get(skuReq.getId());
                if (existing == null) {
                    throw new ServiceException("SKU不存在或不属于当前商品: " + skuReq.getId());
                }
                if (StringUtils.isBlank(skuReq.getSkuCode())) {
                    skuReq.setSkuCode(existing.getSkuCode());
                }
                goodsSkuService.update(skuReq);
                retainedIds.add(skuReq.getId());
            } else {
                Long newId = goodsSkuService.create(skuReq);
                retainedIds.add(newId);
            }
        }

        // 将未在本次请求中保留的SKU标记删除。
        for (WmsGoodsSku existing : existingList) {
            if (!retainedIds.contains(existing.getId())) {
                skuMapper.deleteById(existing.getId());
            }
        }

        // 更新商品主表的最小库存和最大库存（取所有SKU的最小值和最大值）
        updateGoodsStockRange(goodsId);
    }

    /**
     * 更新商品主表的最小库存和最大库存（累加所有SKU的值）
     */
    private void updateGoodsStockRange(Long goodsId) {
        List<WmsGoodsSku> skuList = skuMapper.selectList(new LambdaQueryWrapper<WmsGoodsSku>()
                .eq(WmsGoodsSku::getGoodsId, goodsId)
                .eq(WmsGoodsSku::getDelFlag, false));

        if (skuList == null || skuList.isEmpty()) {
            return;
        }

        // 累加所有SKU的最小库存和最大库存
        int totalMinStock = 0;
        int totalMaxStock = 0;
        for (WmsGoodsSku sku : skuList) {
            if (sku.getMinStock() != null) {
                totalMinStock += sku.getMinStock();
            }
            if (sku.getMaxStock() != null) {
                totalMaxStock += sku.getMaxStock();
            }
        }

        WmsGoods goods = new WmsGoods();
        goods.setId(goodsId);
        goods.setMinStock(totalMinStock);
        goods.setMaxStock(totalMaxStock);
        updateById(goods);
    }

    private void tryCompleteAddGoodsOnboarding(Long goodsId) {
        try {
            if (goodsId == null) {
                return;
            }

            Long skuCount = skuMapper.selectCount(new LambdaQueryWrapper<WmsGoodsSku>()
                    .eq(WmsGoodsSku::getGoodsId, goodsId));
            if (skuCount == null || skuCount <= 0) {
                return;
            }

            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return;
            }

            // 只有商品存在至少一个SKU时，才判定"添加第一个商品"完成。
            onboardingService.completeTask(userId, "add_goods");
        } catch (Exception ex) {
            // 新手引导失败不应阻塞商品主流程
            log.warn("complete onboarding add_goods failed", ex);
        }
    }

    private boolean hasHistoricalRelation(Long goodsId) {
        return hasRows(purchaseInItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseInItem>()
                .eq(WmsPurchaseInItem::getGoodsId, goodsId)))
                || hasRows(purchaseOrderItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseOrderItem>()
                .eq(WmsPurchaseOrderItem::getGoodsId, goodsId)))
                || hasRows(purchaseReturnItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                .eq(WmsPurchaseReturnItem::getGoodsId, goodsId)))
                || hasRows(salesOrderItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getGoodsId, goodsId)))
                || hasRows(salesOutItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesOutItem>()
                .eq(WmsSalesOutItem::getGoodsId, goodsId)))
                || hasRows(salesReturnItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesReturnItem>()
                .eq(WmsSalesReturnItem::getGoodsId, goodsId)))
                || hasRows(stockCheckItemMapper.selectCount(new LambdaQueryWrapper<WmsStockCheckItem>()
                .eq(WmsStockCheckItem::getGoodsId, goodsId)))
                || hasRows(stockTransferItemMapper.selectCount(new LambdaQueryWrapper<WmsStockTransferItem>()
                .eq(WmsStockTransferItem::getGoodsId, goodsId)))
                || hasRows(stockMapper.selectCount(new LambdaQueryWrapper<WmsStock>()
                .eq(WmsStock::getGoodsId, goodsId)))
                || hasRows(stockLogMapper.selectCount(new LambdaQueryWrapper<WmsStockLog>()
                .eq(WmsStockLog::getGoodsId, goodsId)));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    @Autowired
    private WmsGoodsImportService goodsImportService;

    @Override
    public ImportResult importGoods(MultipartFile file) throws Exception {
        return goodsImportService.importGoods(file);
    }

    @Override
    public void generateImportTemplate(HttpServletResponse response, String templateType) throws Exception {
        goodsImportService.generateImportTemplate(response, templateType);
    }

    @Override
    public List<WmsGoodsExportVO> exportList(WmsGoodsPageReqVO reqVO) {
        // 1. 按条件查询所有商品（不分页）
        LambdaQueryWrapper<WmsGoods> wrapper = new LambdaQueryWrapper<WmsGoods>()
                .eq(WmsGoods::getDelFlag, false)
                .like(StringUtils.isNotEmpty(reqVO.getGoodsCode()), WmsGoods::getGoodsCode, reqVO.getGoodsCode())
                .like(StringUtils.isNotEmpty(reqVO.getGoodsName()), WmsGoods::getGoodsName, reqVO.getGoodsName())
                .eq(reqVO.getCategoryId() != null, WmsGoods::getCategoryId, reqVO.getCategoryId())
                .eq(StringUtils.isNotEmpty(reqVO.getBrand()), WmsGoods::getBrand, reqVO.getBrand())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsGoods::getStatus, reqVO.getStatus())
                .orderByDesc(WmsGoods::getCreateTime);

        List<WmsGoods> goodsList = list(wrapper);
        List<WmsGoodsRespVO> respVOList = VoUtils.convertList(goodsList, WmsGoodsRespVO.class);
        fillBaseInfo(respVOList);

        if (respVOList.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. 批量查询所有SKU（一次性查出所有商品的SKU）
        List<Long> goodsIds = respVOList.stream().map(WmsGoodsRespVO::getId).collect(Collectors.toList());
        List<WmsGoodsSku> allSkus = skuMapper.selectList(
                new LambdaQueryWrapper<WmsGoodsSku>().in(WmsGoodsSku::getGoodsId, goodsIds));
        Map<Long, List<WmsGoodsSku>> skuMap = allSkus.stream()
                .collect(Collectors.groupingBy(WmsGoodsSku::getGoodsId));

        // 3. 批量查询单位信息（如果fillBaseInfo未填充单位名称，补上）
        // fillBaseInfo 已经填好了 categoryName 和 unitName

        // 4. 组装导出数据
        List<WmsGoodsExportVO> exportList = new java.util.ArrayList<>();
        for (WmsGoodsRespVO goods : respVOList) {
            List<WmsGoodsSku> skus = skuMap.getOrDefault(goods.getId(), java.util.Collections.emptyList());
            if (skus.isEmpty()) {
                exportList.add(buildGoodsExportRow(goods, null));
            } else {
                for (WmsGoodsSku sku : skus) {
                    exportList.add(buildGoodsExportRow(goods, sku));
                }
            }
        }
        return exportList;
    }

    private WmsGoodsExportVO buildGoodsExportRow(WmsGoodsRespVO goods, WmsGoodsSku sku) {
        WmsGoodsExportVO vo = new WmsGoodsExportVO();
        vo.setGoodsCode(goods.getGoodsCode());
        vo.setGoodsName(goods.getGoodsName());
        vo.setCategoryName(goods.getCategoryName());
        vo.setUnitName(goods.getUnitName());
        vo.setBrand(goods.getBrand());
        vo.setPlace(goods.getPlace());
        vo.setStatus(goods.getStatus());
        vo.setRemark(goods.getRemark());
        vo.setCreateTime(goods.getCreateTime());

        if (sku != null) {
            vo.setBarcode(sku.getBarcode());
            vo.setSkuCode(sku.getSkuCode());
            vo.setSkuName(sku.getSkuName());
            vo.setSpecInfo(sku.getSpecInfo());
            vo.setCostPrice(sku.getCostPrice());
            vo.setSalePrice(sku.getSalePrice());
            vo.setMinStock(sku.getMinStock());
            vo.setMaxStock(sku.getMaxStock());
        }
        return vo;
    }
}
