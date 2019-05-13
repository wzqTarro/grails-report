package com.report.service

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
    @RequestLine("GET /metrics?startWith={startWith}&endWith={endWith}&dataType={dataType}&owners={owners}")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @RequestMapping(method = RequestMethod.GET, value = "/metrics")
    List getData(@Param("startWith")@RequestParam("startWith")String leftTime, @Param("endWith")@RequestParam("endWith")String rightTime,
                                      @Param("dataType")@RequestParam("dataType")String dataType,
                 @Param("owners")@RequestParam(value = "owners")Map<String, Object> owners)
}