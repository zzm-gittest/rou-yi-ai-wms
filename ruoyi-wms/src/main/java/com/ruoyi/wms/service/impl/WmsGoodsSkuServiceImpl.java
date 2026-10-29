package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsPurchaseInItem;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.WmsPurchaseReturnItem;
import com.ruoyi.wms.domain.WmsSalesOrderItem;
import com.ruoyi.wms.domain.WmsSalesOutItem;
import com.ruoyi.wms.domain.WmsSalesReturnItem;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockCheckItem;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsStockTransferItem;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuPageReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
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
import com.ruoyi.wms.mapper.WmsGoodsMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsUnitService;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import com.ruoyi.wms.utils.GoodsNameMatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品SKU表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsGoodsSkuServiceImpl extends ServiceImpl<WmsGoodsSkuMapper, WmsGoodsSku> implements WmsGoodsSkuService {

    private static final Logger log = LoggerFactory.getLogger(WmsGoodsSkuServiceImpl.class);

    /** 模糊匹配相似度阈值：低于该分值的候选不进入结果，避免误召回。 */
    private static final double FUZZY_SCORE_THRESHOLD = 0.34d;

    @Autowired
    private WmsUnitService unitService;

    @Autowired
    private WmsUserOnboardingService onboardingService;

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
    @Autowired
    private WmsGoodsMapper goodsMapper;

    @Override
    public IPage<WmsGoodsSkuRespVO> page(WmsGoodsSkuPageReqVO reqVO) {
        Page<WmsGoodsSkuPageReqVO> page = PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize());
        return baseMapper.selectSkuPage(page, reqVO);
    }

    @Override
    public List<WmsGoodsSkuRespVO> listByGoodsId(Long goodsId) {
        LambdaQueryWrapper<WmsGoodsSku> wrapper = new LambdaQueryWrapper<WmsGoodsSku>()
                .eq(WmsGoodsSku::getGoodsId, goodsId)
                .eq(WmsGoodsSku::getDelFlag, false)
                .orderByDesc(WmsGoodsSku::getCreateTime);

        List<WmsGoodsSku> list = baseMapper.selectList(wrapper);
        return list.stream()
                .map(item -> {
                    WmsGoodsSkuRespVO vo = new WmsGoodsSkuRespVO();
                    BeanUtils.copyProperties(item, vo);

                    // 填充单位名称
                    if (item.getUnitId() != null) {
                        var unit = unitService.getById(item.getUnitId());
                        if (unit != null) {
                            vo.setUnitName(unit.getUnitName());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public WmsGoodsSkuRespVO get(Long id) {
        WmsGoodsSkuRespVO vo = baseMapper.selectRespById(id);
        if (vo == null) {
            throw new ServiceException("SKU不存在");
        }

        if (StringUtils.isBlank(vo.getUnitName()) && vo.getUnitId() != null) {
            var unit = unitService.getById(vo.getUnitId());
            if (unit != null) {
                vo.setUnitName(unit.getUnitName());
            }
        }
        return vo;
    }

    @Override
    public Long create(WmsGoodsSkuSaveReqVO reqVO) {
        // 检查同一商品下是否存在相同规格组合的SKU
        if (StringUtils.isNotBlank(reqVO.getSpecInfo())) {
            String normalizedSpecInfo = normalizeSpecInfo(reqVO.getSpecInfo());
            List<WmsGoodsSku> existingSkus = lambdaQuery()
                    .eq(WmsGoodsSku::getGoodsId, reqVO.getGoodsId())
                    .list();
            for (WmsGoodsSku existingSku : existingSkus) {
                if (StringUtils.isNotBlank(existingSku.getSpecInfo())) {
                    String existingNormalized = normalizeSpecInfo(existingSku.getSpecInfo());
                    if (normalizedSpecInfo.equals(existingNormalized)) {
                        throw new ServiceException("该规格组合已存在：" + reqVO.getSpecInfo());
                    }
                }
            }
        }

        // 检查条码是否已存在（全局唯一）
        if (StringUtils.isNotBlank(reqVO.getBarcode())) {
            WmsGoodsSku existingSku = lambdaQuery()
                    .eq(WmsGoodsSku::getBarcode, reqVO.getBarcode())
                    .one();
            if (existingSku != null) {
                throw new ServiceException("该条码已存在：" + reqVO.getBarcode());
            }
        }

        WmsGoodsSku sku = new WmsGoodsSku();
        BeanUtils.copyProperties(reqVO, sku);
        fillUnitNameFromUnitId(sku);
        // skuName 兜底：为空时使用商品名称
        if (StringUtils.isBlank(sku.getSkuName())) {
            WmsGoods goods = goodsMapper.selectById(reqVO.getGoodsId());
            if (goods != null && StringUtils.isNotBlank(goods.getGoodsName())) {
                sku.setSkuName(goods.getGoodsName());
            }
        }
        if (StringUtils.isBlank(sku.getSkuCode())) {
            sku.setSkuCode(generateSkuCode(reqVO.getGoodsId()));
        }
        checkSkuCodeUnique(null, sku.getSkuCode());

        sku.setStatus(reqVO.getStatus() != null ? reqVO.getStatus() : "0");
        sku.setCreateTime(LocalDateTime.now());
        sku.setUpdateTime(LocalDateTime.now());
        sku.setCreateBy("system");
        sku.setUpdateBy("system");
        baseMapper.insert(sku);

        tryCompleteAddGoodsOnboarding();
        return sku.getId();
    }

    private void tryCompleteAddGoodsOnboarding() {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return;
            }
            // 兼容移动端分步创建商品：保存SKU后补记“添加第一个商品”任务。
            onboardingService.completeTask(userId, "add_goods");
        } catch (Exception ex) {
            // 新手引导失败不应阻塞SKU主流程。
            log.warn("complete onboarding add_goods on sku create failed", ex);
        }
    }

    @Override
    public void update(WmsGoodsSkuSaveReqVO reqVO) {
        WmsGoodsSku existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("SKU不存在");
        }

        // 检查同一商品下是否存在相同规格组合的SKU（排除自己）
        if (StringUtils.isNotBlank(reqVO.getSpecInfo())) {
            String normalizedSpecInfo = normalizeSpecInfo(reqVO.getSpecInfo());
            List<WmsGoodsSku> existingSkus = lambdaQuery()
                    .eq(WmsGoodsSku::getGoodsId, reqVO.getGoodsId())
                    .ne(WmsGoodsSku::getId, reqVO.getId())
                    .list();
            for (WmsGoodsSku existingSku : existingSkus) {
                if (StringUtils.isNotBlank(existingSku.getSpecInfo())) {
                    String existingNormalized = normalizeSpecInfo(existingSku.getSpecInfo());
                    if (normalizedSpecInfo.equals(existingNormalized)) {
                        throw new ServiceException("该规格组合已存在：" + reqVO.getSpecInfo());
                    }
                }
            }
        }

        // 检查条码是否已被其他SKU使用
        if (StringUtils.isNotBlank(reqVO.getBarcode())) {
            WmsGoodsSku barcodeSku = lambdaQuery()
                    .eq(WmsGoodsSku::getBarcode, reqVO.getBarcode())
                    .ne(WmsGoodsSku::getId, reqVO.getId())
                    .one();
            if (barcodeSku != null) {
                throw new ServiceException("该条码已被其他商品使用：" + reqVO.getBarcode());
            }
        }

        WmsGoodsSku sku = new WmsGoodsSku();
        BeanUtils.copyProperties(reqVO, sku);
        fillUnitNameFromUnitId(sku);
        // skuName 兜底：为空时使用商品名称
        if (StringUtils.isBlank(sku.getSkuName())) {
            WmsGoods goods = goodsMapper.selectById(reqVO.getGoodsId());
            if (goods != null && StringUtils.isNotBlank(goods.getGoodsName())) {
                sku.setSkuName(goods.getGoodsName());
            }
        }
        if (StringUtils.isBlank(sku.getSkuCode())) {
            sku.setSkuCode(existing.getSkuCode());
        }
        checkSkuCodeUnique(reqVO.getId(), sku.getSkuCode());

        // 保留原有的规格信息（如果前端未传入）
        if (StringUtils.isBlank(reqVO.getSpecInfo())) {
            sku.setSpecInfo(existing.getSpecInfo());
        }
        // 保留原有的规格值ID组合（如果前端未传入）
        if (StringUtils.isBlank(reqVO.getSpecIds())) {
            sku.setSpecIds(existing.getSpecIds());
        }

        baseMapper.updateById(sku);
    }

    /**
     * 标准化规格信息用于比较（兼容JSON和空格分隔格式）
     * JSON格式: {"颜色":"红色","尺寸":"大"} -> "红色大"
     * 空格格式: "红色 大" -> "红色大"
     */
    private String normalizeSpecInfo(String specInfo) {
        if (StringUtils.isBlank(specInfo)) {
            return "";
        }
        String trimmed = specInfo.trim();
        // 尝试解析为JSON
        if (trimmed.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> map = mapper.readValue(trimmed, java.util.Map.class);
                // 按值排序后拼接，确保顺序一致
                return map.values().stream()
                        .sorted()
                        .collect(java.util.stream.Collectors.joining(""));
            } catch (Exception e) {
                // JSON解析失败，使用空格分隔处理
            }
        }
        // 空格分隔格式：去除所有空格
        return trimmed.replaceAll("\\s+", "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsGoodsSku existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("SKU不存在");
        }
        if (hasHistoricalRelation(id)) {
            throw new ServiceException("商品已关联历史单据或库存记录，无法删除。请改为停用商品。");
        }
        // 改为逻辑删除
        baseMapper.deleteById(id);
        cleanupGoodsIfNoSku(existing.getGoodsId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 改为逻辑删除（批量更新需要循环）
            Set<Long> affectedGoodsIds = new LinkedHashSet<>();
            for (Long id : ids) {
                WmsGoodsSku sku = baseMapper.selectById(id);
                if (sku != null) {
                    if (hasHistoricalRelation(id)) {
                        String skuName = StringUtils.isNotBlank(sku.getSkuName()) ? sku.getSkuName() : String.valueOf(id);
                        throw new ServiceException("SKU【" + skuName + "】已关联历史单据或库存记录，无法删除。请改为停用商品。");
                    }
                    baseMapper.deleteById(id);
                    affectedGoodsIds.add(sku.getGoodsId());
                }
            }
            for (Long goodsId : affectedGoodsIds) {
                cleanupGoodsIfNoSku(goodsId);
            }
        }
    }

    /**
     * 当商品下所有有效 SKU 均被删除时，同步逻辑删除商品头表，避免移动端与PC端展示不一致。
     */
    private void cleanupGoodsIfNoSku(Long goodsId) {
        if (goodsId == null) {
            return;
        }
        Long activeSkuCount = baseMapper.selectCount(new LambdaQueryWrapper<WmsGoodsSku>()
                .eq(WmsGoodsSku::getGoodsId, goodsId)
                .eq(WmsGoodsSku::getDelFlag, false));
        if (activeSkuCount != null && activeSkuCount > 0) {
            return;
        }
        WmsGoods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            goodsMapper.deleteById(goodsId);
        }
    }

    private boolean hasHistoricalRelation(Long skuId) {
        return hasRows(purchaseInItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseInItem>()
                .eq(WmsPurchaseInItem::getSkuId, skuId)))
                || hasRows(purchaseOrderItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseOrderItem>()
                .eq(WmsPurchaseOrderItem::getSkuId, skuId)))
                || hasRows(purchaseReturnItemMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                .eq(WmsPurchaseReturnItem::getSkuId, skuId)))
                || hasRows(salesOrderItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getSkuId, skuId)))
                || hasRows(salesOutItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesOutItem>()
                .eq(WmsSalesOutItem::getSkuId, skuId)))
                || hasRows(salesReturnItemMapper.selectCount(new LambdaQueryWrapper<WmsSalesReturnItem>()
                .eq(WmsSalesReturnItem::getSkuId, skuId)))
                || hasRows(stockCheckItemMapper.selectCount(new LambdaQueryWrapper<WmsStockCheckItem>()
                .eq(WmsStockCheckItem::getSkuId, skuId)))
                || hasRows(stockTransferItemMapper.selectCount(new LambdaQueryWrapper<WmsStockTransferItem>()
                .eq(WmsStockTransferItem::getSkuId, skuId)))
                || hasRows(stockMapper.selectCount(new LambdaQueryWrapper<WmsStock>()
                .eq(WmsStock::getSkuId, skuId)))
                || hasRows(stockLogMapper.selectCount(new LambdaQueryWrapper<WmsStockLog>()
                .eq(WmsStockLog::getSkuId, skuId)));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    @Override
    public WmsGoodsSkuRespVO getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }

        log.info("根据条码查询本地数据库: {}", barcode);
        WmsGoodsSku sku = lambdaQuery()
                .eq(WmsGoodsSku::getBarcode, barcode.trim())
                .one();

        if (sku == null) {
            log.info("条码未录入: {}", barcode);
            return null;
        }

        log.info("条码已存在: {}, 商品: {}", barcode, sku.getSkuName());
        WmsGoodsSkuRespVO vo = new WmsGoodsSkuRespVO();
        BeanUtils.copyProperties(sku, vo);

        // 填充单位名称
        if (sku.getUnitId() != null) {
            var unit = unitService.getById(sku.getUnitId());
            if (unit != null) {
                vo.setUnitName(unit.getUnitName());
            }
        }

        return vo;
    }

    @Override
    public List<WmsGoodsSkuRespVO> searchByName(String name, int limit) {
        if (name == null || name.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String raw = name.trim();
        int safeLimit = limit <= 0 ? 10 : limit;

        // 1) 先走原有精确 LIKE 匹配（连续子串），命中即用，保持既有行为与顺序稳定
        List<WmsGoodsSkuRespVO> exact = baseMapper.searchByNameWithGoods(raw, safeLimit);

        // 2) 归一化后长度不足，或输入本就无有效字符，直接返回精确结果
        String normalized = GoodsNameMatchUtils.normalize(raw);
        if (normalized.length() < GoodsNameMatchUtils.MIN_FUZZY_LENGTH) {
            return exact;
        }

        // 3) bigram 粗召回 + 相似度精排，覆盖空格/缺字/乱序等 LIKE 无法命中的场景
        List<String> tokens = new java.util.ArrayList<>(GoodsNameMatchUtils.bigrams(normalized));
        if (tokens.isEmpty()) {
            return exact;
        }
        // 召回量适当放大，给精排留足候选空间
        int recallLimit = Math.max(safeLimit * 5, 50);
        List<WmsGoodsSkuRespVO> candidates;
        try {
            candidates = baseMapper.recallByTokens(tokens, recallLimit);
        } catch (Exception e) {
            log.warn("商品名称模糊召回失败，回退精确匹配结果. name={}", raw, e);
            return exact;
        }
        if (candidates == null || candidates.isEmpty()) {
            return exact;
        }

        // 合并精确结果（精确结果排在前面且不重复），再对模糊候选按相似度过滤+排序
        java.util.LinkedHashMap<Long, WmsGoodsSkuRespVO> merged = new java.util.LinkedHashMap<>();
        for (WmsGoodsSkuRespVO vo : exact) {
            if (vo != null && vo.getId() != null) {
                merged.put(vo.getId(), vo);
            }
        }

        // 计算每个候选的相似度（取 SKU名称/商品名称/规格 三者最高分）
        List<ScoredSku> scored = new java.util.ArrayList<>();
        for (WmsGoodsSkuRespVO vo : candidates) {
            if (vo == null || vo.getId() == null || merged.containsKey(vo.getId())) {
                continue;
            }
            double score = maxSimilarity(normalized, vo);
            if (score >= FUZZY_SCORE_THRESHOLD) {
                scored.add(new ScoredSku(vo, score));
            }
        }
        scored.sort((x, y) -> Double.compare(y.score, x.score));

        List<WmsGoodsSkuRespVO> result = new java.util.ArrayList<>(merged.values());
        for (ScoredSku s : scored) {
            if (result.size() >= safeLimit) {
                break;
            }
            result.add(s.sku);
        }
        return result.size() > safeLimit ? result.subList(0, safeLimit) : result;
    }

    /**
     * 取候选 SKU 的 SKU名称/商品名称/规格 与归一化查询的最高相似度。
     */
    private double maxSimilarity(String normalizedQuery, WmsGoodsSkuRespVO vo) {
        double best = 0d;
        best = Math.max(best, GoodsNameMatchUtils.similarity(normalizedQuery, vo.getSkuName()));
        best = Math.max(best, GoodsNameMatchUtils.similarity(normalizedQuery, vo.getGoodsName()));
        best = Math.max(best, GoodsNameMatchUtils.similarity(normalizedQuery, vo.getSpecInfo()));
        return best;
    }

    /**
     * 相似度打分的 SKU 内部包装。
     */
    private static final class ScoredSku {
        private final WmsGoodsSkuRespVO sku;
        private final double score;

        private ScoredSku(WmsGoodsSkuRespVO sku, double score) {
            this.sku = sku;
            this.score = score;
        }
    }

    @Override
    public Map<Long, WmsGoodsSku> getEnabledSkuMap(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        LinkedHashSet<Long> uniqueIds = skuIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        List<WmsGoodsSku> skuList = lambdaQuery()
                .in(WmsGoodsSku::getId, uniqueIds)
                .list();
        Map<Long, WmsGoodsSku> skuMap = skuList.stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> sku));

        List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !skuMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new ServiceException("SKU不存在: " + missingIds);
        }

        List<String> disabledSkuNames = skuList.stream()
                .filter(sku -> !"0".equals(sku.getStatus()))
                .map(sku -> StringUtils.isNotBlank(sku.getSkuName()) ? sku.getSkuName() : String.valueOf(sku.getId()))
                .collect(Collectors.toList());
        if (!disabledSkuNames.isEmpty()) {
            throw new ServiceException("以下SKU已停用，不能用于新单据: " + String.join(", ", disabledSkuNames));
        }

        // 兜底补齐单位名称，避免历史SKU仅保存unit_id导致单据快照缺失单位文本。
        for (WmsGoodsSku sku : skuList) {
            if (sku == null || StringUtils.isNotBlank(sku.getUnitName()) || sku.getUnitId() == null) {
                continue;
            }
            var unit = unitService.getById(sku.getUnitId());
            if (unit != null) {
                sku.setUnitName(unit.getUnitName());
            }
        }

        return skuMap;
    }

    private void checkSkuCodeUnique(Long id, String skuCode) {
        if (StringUtils.isBlank(skuCode)) {
            return;
        }
        Long count = baseMapper.countBySkuCodeIncludingDeleted(skuCode, id);
        if (count != null && count > 0) {
            throw new ServiceException("SKU编码已存在：" + skuCode);
        }
    }

    /**
     * 统一回填单位名称，避免仅保存 unit_id 导致 sku.unit_name 为空。
     */
    private void fillUnitNameFromUnitId(WmsGoodsSku sku) {
        if (sku == null || sku.getUnitId() == null) {
            return;
        }
        var unit = unitService.getById(sku.getUnitId());
        if (unit != null) {
            sku.setUnitName(unit.getUnitName());
        }
    }

    private String generateSkuCode(Long goodsId) {
        String prefix = "SK" + String.format("%08d", goodsId);
        String lastSkuCode = baseMapper.selectMaxSkuCodeIncludingDeleted(goodsId, prefix);

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

}
