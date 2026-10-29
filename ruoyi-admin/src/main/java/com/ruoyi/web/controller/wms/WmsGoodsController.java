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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsExportVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsPageReqVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsRespVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.service.WmsGoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 商品管理Controller
 *
 * @author ruoyi
 */
@Api("商品管理")
@RestController
@RequestMapping("/wms/goods")
@Validated
public class WmsGoodsController extends BaseController {

    private final WmsGoodsService goodsService;

    public WmsGoodsController(WmsGoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * 查询商品分页列表
     */
    @ApiOperation("查询商品分页列表")
    @PreAuthorize("@ss.hasPermi('wms:goods:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsGoodsPageReqVO reqVO) {
        IPage<WmsGoodsRespVO> page = goodsService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询商品列表（不分页）
     */
    @ApiOperation("查询商品列表")
    @PreAuthorize("@ss.hasPermi('wms:goods:query')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsGoodsRespVO> list = goodsService.selectList();
        return success(list);
    }

    /**
     * 获取商品详情
     */
    @ApiOperation("获取商品详情")
    @PreAuthorize("@ss.hasPermi('wms:goods:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(goodsService.get(id));
    }

    /**
     * 获取商品详情（含SKU列表）
     */
    @ApiOperation("获取商品详情（含SKU列表）")
    @PreAuthorize("@ss.hasPermi('wms:goods:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getDetailWithSkus(@PathVariable Long id) {
        return success(goodsService.getDetailWithSkus(id));
    }

    /**
     * 新增商品
     */
    @ApiOperation("新增商品")
    @PreAuthorize("@ss.hasPermi('wms:goods:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsGoodsSaveReqVO reqVO) {
        Long id = goodsService.create(reqVO);
        return success(id);
    }

    /**
     * 修改商品
     */
    @ApiOperation("修改商品")
    @PreAuthorize("@ss.hasPermi('wms:goods:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsGoodsSaveReqVO reqVO) {
        goodsService.update(reqVO);
        return success();
    }

    /**
     * 删除商品
     */
    @ApiOperation("删除商品")
    @PreAuthorize("@ss.hasPermi('wms:goods:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        goodsService.delete(id);
        return success();
    }

    /**
     * 生成商品编码
     */
    @ApiOperation("生成商品编码")
    @PreAuthorize("@ss.hasPermi('wms:goods:add')")
    @GetMapping("/generateCode")
    public AjaxResult generateGoodsCode(String categoryId) {
        return AjaxResult.success("操作成功", goodsService.generateGoodsCode(categoryId));
    }

    /**
     * 生成SKU编码
     */
    @ApiOperation("生成SKU编码")
    @PreAuthorize("@ss.hasPermi('wms:goodsSku:add')")
    @GetMapping("/generateSkuCode")
    public AjaxResult generateSkuCode(Long goodsId) {
        return AjaxResult.success("操作成功", goodsService.generateSkuCode(goodsId));
    }

    /**
     * 导出商品
     */
    @ApiOperation("导出商品")
    @PreAuthorize("@ss.hasPermi('wms:goods:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsGoodsPageReqVO reqVO) {
        List<WmsGoodsExportVO> list = goodsService.exportList(reqVO);
        ExcelUtil<WmsGoodsExportVO> util = new ExcelUtil<>(WmsGoodsExportVO.class);
        util.exportExcel(response, list, "商品数据");
    }

    /**
     * 导入商品
     */
    @ApiOperation("导入商品")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/import")
    public AjaxResult importGoods(@RequestParam("file") MultipartFile file) throws Exception {
        ImportResult result = goodsService.importGoods(file);
        return success(result);
    }

    /**
     * 下载导入模板
     */
    @ApiOperation("下载导入模板")
    @PreAuthorize("@ss.hasPermi('wms:goods:importTemplate')")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, 
            @RequestParam(value = "type", defaultValue = "simple") String templateType) throws Exception {
        goodsService.generateImportTemplate(response, templateType);
    }
}
