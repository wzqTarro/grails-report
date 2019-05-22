package com.report.service

import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine
import org.grails.web.json.JSONObject
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@FeignClient("DataSrv")
interface IDataCenterService {
    /**
     * 分时指标结果查询（实表接口）
     * @param startTime
     * @param endTime
     * @param dataType
     * @param owners
     * @return
     */
    @RequestLine("GET /metrics?startWith={startWith}&endWith={endWith}&dataType={dataType}&owners={owners}")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @RequestMapping(method = RequestMethod.GET, value = "/metrics")
    List getData(@Param("startWith")@RequestParam("startWith")String startTime, @Param("endWith")@RequestParam("endWith")String endTime,
                                      @Param("dataType")@RequestParam("dataType")String dataType,
                 @Param("owners")@RequestParam(value = "owners")Map<String, Object> owners)

    @RequestLine("POST /sql")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("script={script}&params={params}")
    @RequestMapping(method = RequestMethod.POST, value = "/sql")
    List getVirtualData(@Param("script")@RequestParam("script")String sql, @Param("params")@RequestParam("params")List params)
}