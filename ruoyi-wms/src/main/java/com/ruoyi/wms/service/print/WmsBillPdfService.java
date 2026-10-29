package com.ruoyi.wms.service.print;

import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;

public interface WmsBillPdfService {

    WmsBillPrintRespVO buildPrintData(String title, Object sourceData);

    byte[] generatePdf(WmsBillPrintRespVO printData);
}
