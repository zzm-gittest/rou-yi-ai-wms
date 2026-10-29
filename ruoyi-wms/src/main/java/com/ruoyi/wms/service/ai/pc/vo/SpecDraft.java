package com.ruoyi.wms.service.ai.pc.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SpecDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    private String specName;
    private List<String> values = new ArrayList<>();
}
