package com.ruoyi.wms.service.ai.pc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdNameOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
}
