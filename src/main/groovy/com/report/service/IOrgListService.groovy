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

import javax.ws.rs.FormParam

@FeignClient(name = "ORG-SERVICE")
interface IOrgListService {
    @RequestLine("POST /orgService/getOrgListByStaffId")//work for testing
    @RequestMapping(method = RequestMethod.POST, value = "/orgService/getOrgListByStaffId")//work for service discovery
    JSONObject getOrgListByStaffId(@RequestParam(value = "staffId")String staffId)

    /**
     *
     * @param jsonData
     * @return
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")//work for testing
    @RequestLine("POST /orgService/getOrgTreeList")//work for testing
    @Body("jsonData={jsonData}")//work for testing
    @RequestMapping(method = RequestMethod.POST, value = "/orgService/getOrgTreeList")//work for service discovery 实际测试时 检测RequestMapping是否可以被上面三个测试注解替代
    JSONObject getOrgTreeList(
            @Param("jsonData")//work for testing
            @RequestParam(value = "jsonData")
            String jsonData
    )

    @RequestLine("POST /orgService/getOrgListByOrgTreeAndLayer")//work for testing
    @RequestMapping(method = RequestMethod.POST, value = "/orgService/getOrgListByOrgTreeAndLayer")//work for service discovery
    JSONObject getOrgListByOrgTreeAndLayer(@RequestParam(value = "json")String json)
}
