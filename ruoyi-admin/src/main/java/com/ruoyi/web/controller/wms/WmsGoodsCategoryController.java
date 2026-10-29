package com.ruoyi.web.controller.wms;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryListReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryRespVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 商品分类管理Controller
 *
 * @author ruoyi
 */
@Api("商品分类管理")
@RestController
@RequestMapping("/wms/goodsCategory")
@Validated
public class WmsGoodsCategoryController extends BaseController {

    private final WmsGoodsCategoryService categoryService;

    public WmsGoodsCategoryController(WmsGoodsCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 查询商品分类树列表
     */
    @ApiOperation("查询商品分类树列表")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:query')")
    @GetMapping("/tree")
    public AjaxResult tree(WmsGoodsCategoryListReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new WmsGoodsCategoryListReqVO();
        }
        List<WmsGoodsCategoryRespVO> list = categoryService.listTree(reqVO);
        return success(list);
    }

    /**
     * 查询商品分类列表（不分页）
     */
    @ApiOperation("查询商品分类列表")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:query')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsGoodsCategoryRespVO> list = categoryService.selectList();
        return success(list);
    }

    /**
     * 获取商品分类详情
     */
    @ApiOperation("获取商品分类详情")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(categoryService.get(id));
    }

    /**
     * 新增商品分类
     */
    @ApiOperation("新增商品分类")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsGoodsCategorySaveReqVO reqVO) {
        Long id = categoryService.create(reqVO);
        return success(id);
    }

    /**
     * 修改商品分类
     */
    @ApiOperation("修改商品分类")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsGoodsCategorySaveReqVO reqVO) {
        categoryService.update(reqVO);
        return success();
    }

    /**
     * 删除商品分类
     */
    @ApiOperation("删除商品分类")
    @PreAuthorize("@ss.hasPermi('wms:goodsCategory:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        categoryService.delete(id);
        return success();
    }
}
