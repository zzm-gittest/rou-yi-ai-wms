package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehousePageReqVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseSaveReqVO;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.WmsWarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 仓库表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsWarehouseServiceImpl extends ServiceImpl<WmsWarehouseMapper, WmsWarehouse> implements WmsWarehouseService {

    private static final String WAREHOUSE_CODE_UNIQUE = "仓库编码已存在";
    private static final Pattern WAREHOUSE_CODE_PATTERN = Pattern.compile("^WH(\\d+)$");

    @Override
    public IPage<WmsWarehouseRespVO> page(WmsWarehousePageReqVO reqVO) {
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<WmsWarehouse>()
                .like(StringUtils.isNotEmpty(reqVO.getWarehouseCode()), WmsWarehouse::getWarehouseCode, reqVO.getWarehouseCode())
                .like(StringUtils.isNotEmpty(reqVO.getWarehouseName()), WmsWarehouse::getWarehouseName, reqVO.getWarehouseName())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsWarehouse::getStatus, reqVO.getStatus())
                .orderByAsc(WmsWarehouse::getSort)
                .orderByDesc(WmsWarehouse::getCreateTime);

        Page<WmsWarehouse> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsWarehouseRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(VoUtils.convertList(page.getRecords(), WmsWarehouseRespVO.class));
        return resultPage;
    }

    @Override
    public List<WmsWarehouseRespVO> selectList() {
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<WmsWarehouse>()
                .eq(WmsWarehouse::getStatus, "0")
                .orderByAsc(WmsWarehouse::getSort);
        return VoUtils.convertList(list(wrapper), WmsWarehouseRespVO.class);
    }

    @Override
    public WmsWarehouseRespVO get(Long id) {
        WmsWarehouse warehouse = getById(id);
        return VoUtils.convert(warehouse, WmsWarehouseRespVO.class);
    }

    @Override
    public Long create(WmsWarehouseSaveReqVO reqVO) {
        if (StringUtils.isEmpty(reqVO.getWarehouseCode())) {
            reqVO.setWarehouseCode(generateWarehouseCode());
        }

        // 校验编码唯一性
        checkWarehouseCodeUnique(null, reqVO.getWarehouseCode());

        // 转换为实体并保存
        WmsWarehouse warehouse = VoUtils.toEntity(reqVO, WmsWarehouse.class);
        save(warehouse);
        return warehouse.getId();
    }

    @Override
    public void update(WmsWarehouseSaveReqVO reqVO) {
        WmsWarehouse existing = getById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("仓库不存在");
        }
        if (StringUtils.isEmpty(reqVO.getWarehouseCode())) {
            reqVO.setWarehouseCode(existing.getWarehouseCode());
        }

        // 校验编码唯一性
        checkWarehouseCodeUnique(reqVO.getId(), reqVO.getWarehouseCode());

        // 转换为实体并更新
        WmsWarehouse warehouse = VoUtils.toEntity(reqVO, WmsWarehouse.class);
        updateById(warehouse);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    /**
     * 校验仓库编码唯一性
     */
    private void checkWarehouseCodeUnique(Long id, String code) {
        if (StringUtils.isEmpty(code)) {
            return;
        }
        LambdaQueryWrapper<WmsWarehouse> wrapper = new LambdaQueryWrapper<WmsWarehouse>()
                .eq(WmsWarehouse::getWarehouseCode, code);
        // 编辑时排除当前记录
        if (id != null) {
            wrapper.ne(WmsWarehouse::getId, id);
        }
        if (this.exists(wrapper)) {
            throw new ServiceException(WAREHOUSE_CODE_UNIQUE);
        }
    }

    private String generateWarehouseCode() {
        int maxSeq = 0;
        List<WmsWarehouse> warehouses = lambdaQuery()
                .select(WmsWarehouse::getWarehouseCode)
                .likeRight(WmsWarehouse::getWarehouseCode, "WH")
                .list();

        for (WmsWarehouse warehouse : warehouses) {
            if (warehouse == null || StringUtils.isEmpty(warehouse.getWarehouseCode())) {
                continue;
            }
            Matcher matcher = WAREHOUSE_CODE_PATTERN.matcher(warehouse.getWarehouseCode().trim());
            if (!matcher.matches()) {
                continue;
            }
            try {
                int seq = Integer.parseInt(matcher.group(1));
                if (seq > maxSeq) {
                    maxSeq = seq;
                }
            } catch (NumberFormatException ignore) {
                // Ignore malformed numeric suffix and continue scanning other codes.
            }
        }

        return "WH" + String.format("%04d", maxSeq + 1);
    }
}
