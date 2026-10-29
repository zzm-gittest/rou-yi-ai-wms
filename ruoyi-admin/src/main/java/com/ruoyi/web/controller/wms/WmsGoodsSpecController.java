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
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecListReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecRespVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecSaveReqVO;
import com.ruoyi.wms.service.WmsGoodsSpecService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 商品规格管理Controller
 *
 * @author ruoyi
 */
@Api("商品规格管理")
@RestController
@RequestMapping("/wms/goodsSpec")
@Validated
public class WmsGoodsSpecController extends BaseController {

    private final WmsGoodsSpecService goodsSpecService;

    public WmsGoodsSpecController(WmsGoodsSpecService goodsSpecService) {
        this.goodsSpecService = goodsSpecService;
    }

    /**
     * 查询商品规格列表
     */
    @ApiOperation("查询商品规格列表")
    @PreAuthorize("@ss.hasPermi('wms:goodsSpec:query')")
    @GetMapping("/list")
    public AjaxResult list(@Valid WmsGoodsSpecListReqVO reqVO) {
        List<WmsGoodsSpecRespVO> list = goodsSpecService.listByGoodsId(reqVO);
        return success(list);
    }

    /**
     * 获取商品规格详情
     */
    @ApiOperation("获取商品规格详情")
    @PreAuthorize("@ss.hasPermi('wms:goodsSpec:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(goodsSpecService.get(id));
    }

    /**
     * 新增商品规格
     */
    @ApiOperation("新增商品规格")
    @PreAuthorize("@ss.hasPermi('wms:goodsSpec:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsGoodsSpecSaveReqVO reqVO) {
        Long id = goodsSpecService.create(reqVO);
        return success(id);
    }

    /**
     * 修改商品规格
     */
    @ApiOperation("修改商品规格")
    @PreAuthorize("@ss.hasPermi('wms:goodsSpec:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsGoodsSpecSaveReqVO reqVO) {
        goodsSpecService.update(reqVO);
        return success();
    }

    /**
     * 删除商品规格
     */
    @ApiOperation("删除商品规格")
    @PreAuthorize("@ss.hasPermi('wms:goodsSpec:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        goodsSpecService.delete(id);
        return success();
    }
}
