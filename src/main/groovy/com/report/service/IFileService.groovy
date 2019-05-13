package com.report.service

import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine
import org.grails.web.json.JSONObject
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name="FILE-SERVICE")
interface IFileService {
    /**
     * 获取文件访问路径
     * @param fileUrl
     * @param accountId
     * @return
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")//work for testing
    @RequestLine("POST /fileBizService/getReportFileVisitUrl")//work for testing
    @Body("fileUrl={fileUrl}&accountId={accountId}")
    @RequestMapping(method = RequestMethod.POST, value = "/fileBizService/getReportFileVisitUrl")//work for service discovery
    JSONObject getReportFileVisitUrl(@RequestParam("fileUrl")@Param("fileUrl")String fileUrl, @RequestParam("accountId")@Param("accountId")String accountId)

    /**
     * 上传文件
     * @param cloudId
     * @param reportId
     * @return
     */
    @RequestLine("POST /fileBizService/uploadReportFile")//work for testing
    @Headers("Content-Type: application/x-www-form-urlencoded")//work for testing
    @Body("cloudId={cloudId}&reportId={reportId}")
    @RequestMapping(method = RequestMethod.POST, value = "/fileBizService/uploadReportFile")//work for service discovery
    JSONObject uploadReportFile(
            @RequestParam("cloudId")
            @Param("cloudId")
            String cloudId,  //不加@Param默认为body
            @RequestParam("reportId")
            @Param("reportId")
            String reportId)

    /**
     * 删除文件
     * @param cloudId
     * @param reportId
     * @return
     */
    @RequestLine("POST /fileBizService/deleteReportFile")//work for testing
    @Headers("Content-Type: application/x-www-form-urlencoded")//work for testing
    @Body("fileUrl={fileUrl}")
    @RequestMapping(method = RequestMethod.POST, value = "/fileBizService/deleteReportFile")//work for service discovery
    JSONObject deleteReportFile(@Param("fileUrl")@RequestParam("fileUrl")String fileUrl)
}