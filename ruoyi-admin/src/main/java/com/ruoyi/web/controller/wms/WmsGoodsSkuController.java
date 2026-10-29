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
import com.ruoyi.common.core.page.TableDataInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuPageReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 商品SKU管理Controller
 *
 * @author ruoyi
 */
@Api("商品SKU管理")
@RestController
@RequestMapping("/wms/goodsSku")
@Validated
public class WmsGoodsSkuController extends BaseController {

    private final WmsGoodsSkuService goodsSkuService;

    public WmsGoodsSkuController(WmsGoodsSkuService goodsSkuService) {
        this.goodsSkuService = goodsSkuService;
    }

    /**
     * 查询商品SKU分页列表
     */
    @ApiOperation("查询商品SKU分页列表")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsGoodsSkuPageReqVO reqVO) {
        IPage<WmsGoodsSkuRespVO> page = goodsSkuService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询商品SKU列表（按商品ID）
     */
    @ApiOperation("查询商品SKU列表")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:query')")
    @GetMapping("/list/{goodsId}")
    public AjaxResult list(@PathVariable Long goodsId) {
        List<WmsGoodsSkuRespVO> list = goodsSkuService.listByGoodsId(goodsId);
        return success(list);
    }

    /**
     * 获取商品SKU详情
     */
    @ApiOperation("获取商品SKU详情")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(goodsSkuService.get(id));
    }

    /**
     * 新增商品SKU
     */
    @ApiOperation("新增商品SKU")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsGoodsSkuSaveReqVO reqVO) {
        Long id = goodsSkuService.create(reqVO);
        return success(id);
    }

    /**
     * 修改商品SKU
     */
    @ApiOperation("修改商品SKU")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsGoodsSkuSaveReqVO reqVO) {
        goodsSkuService.update(reqVO);
        return success();
    }

    /**
     * 删除商品SKU
     */
    @ApiOperation("删除商品SKU")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        goodsSkuService.delete(id);
        return success();
    }

}
