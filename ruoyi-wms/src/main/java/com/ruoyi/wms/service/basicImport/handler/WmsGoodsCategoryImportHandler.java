package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsGoodsCategory;
import com.ruoyi.wms.service.basicImport.ImportFieldMeta;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.mapper.WmsGoodsCategoryMapper;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import com.ruoyi.wms.service.basicImport.AbstractSimpleExcelImportHandler;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WmsGoodsCategoryImportHandler extends AbstractSimpleExcelImportHandler {

    private final WmsGoodsCategoryService categoryService;
    private final WmsGoodsCategoryMapper categoryMapper;

    public WmsGoodsCategoryImportHandler(WmsGoodsCategoryService categoryService,
                                         WmsGoodsCategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public String getType() {
        return "category";
    }

    @Override
    protected String validateAndSave(Row row, int excelRowNum) {
        String categoryName = cellString(row, "categoryName");
        String parentCategoryName = cellString(row, "parentCategoryName");
        String status = normalizeStatus(cellString(row, "status"));
        Integer orderNum = cellInteger(row, "orderNum", 0);
        String remark = cellString(row, "remark");

        if (StringUtils.isEmpty(categoryName)) {
            return "分类名称不能为空";
        }

        Long parentId = 0L;
        if (StringUtils.isNotEmpty(parentCategoryName)) {
            WmsGoodsCategory parent = categoryMapper.selectOne(new LambdaQueryWrapper<WmsGoodsCategory>()
                    .eq(WmsGoodsCategory::getCategoryName, parentCategoryName.trim())
                    .last("LIMIT 1"));
            if (parent == null) {
                return "父分类不存在: " + parentCategoryName;
            }
            parentId = parent.getId();
        }

        Long dupCount = categoryMapper.selectCount(new LambdaQueryWrapper<WmsGoodsCategory>()
                .eq(WmsGoodsCategory::getParentId, parentId)
                .eq(WmsGoodsCategory::getCategoryName, categoryName.trim()));
        if (dupCount != null && dupCount > 0) {
            return "同级分类已存在: " + categoryName;
        }

        WmsGoodsCategorySaveReqVO reqVO = new WmsGoodsCategorySaveReqVO();
        reqVO.setCategoryName(categoryName.trim());
        reqVO.setParentId(parentId);
        reqVO.setStatus(status);
        reqVO.setOrderNum(orderNum == null ? 0 : orderNum);
        reqVO.setRemark(remark);
        categoryService.create(reqVO);
        return null;
    }

    @Override
    protected String[] headers() {
        return new String[] {"分类名称*", "父分类名称", "状态(正常/停用)", "排序", "备注"};
    }

    @Override
    protected String[] sampleRow() {
        return new String[] {"饮料", "", "正常", "1", "顶级分类示例"};
    }

    @Override
    protected List<ImportFieldMeta> fieldMetas() {
        return Arrays.asList(
                ImportFieldMeta.of("categoryName", "分类名称", true, "名称"),
                ImportFieldMeta.of("parentCategoryName", "父分类名称", false, "父分类"),
                ImportFieldMeta.of("status", "状态", false, "状态(正常/停用)"),
                ImportFieldMeta.of("orderNum", "排序", false),
                ImportFieldMeta.of("remark", "备注", false)
        );
    }

    @Override
    public String templateFileName() {
        return "category_import_template.xlsx";
    }
}
