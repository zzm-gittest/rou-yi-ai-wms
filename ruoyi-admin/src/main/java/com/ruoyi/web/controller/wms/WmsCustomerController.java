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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerPageReqVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;
import com.ruoyi.wms.service.WmsCustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 客户管理Controller
 *
 * @author ruoyi
 */
@Api("客户管理")
@RestController
@RequestMapping("/wms/customer")
@Validated
public class WmsCustomerController extends BaseController {

    private final WmsCustomerService customerService;

    public WmsCustomerController(WmsCustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 查询客户分页列表
     */
    @ApiOperation("查询客户分页列表")
    @PreAuthorize("@ss.hasPermi('wms:customer:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsCustomerPageReqVO reqVO) {
        IPage<WmsCustomerRespVO> page = customerService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询客户列表（不分页）
     */
    @ApiOperation("查询客户列表")
    @PreAuthorize("@ss.hasPermi('wms:customer:query')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsCustomerRespVO> list = customerService.selectList();
        return success(list);
    }

    /**
     * 获取客户详情
     */
    @ApiOperation("获取客户详情")
    @PreAuthorize("@ss.hasPermi('wms:customer:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(customerService.get(id));
    }

    /**
     * 新增客户
     */
    @ApiOperation("新增客户")
    @PreAuthorize("@ss.hasPermi('wms:customer:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsCustomerSaveReqVO reqVO) {
        Long id = customerService.create(reqVO);
        return success(id);
    }

    /**
     * 修改客户
     */
    @ApiOperation("修改客户")
    @PreAuthorize("@ss.hasPermi('wms:customer:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsCustomerSaveReqVO reqVO) {
        customerService.update(reqVO);
        return success();
    }

    /**
     * 删除客户
     */
    @ApiOperation("删除客户")
    @PreAuthorize("@ss.hasPermi('wms:customer:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        customerService.delete(id);
        return success();
    }
}
