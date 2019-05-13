package statreports

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.h2.server.web.WebServlet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.context.annotation.Bean

@EnableFeignClients(value = ["com.report.service"])
@EnableEurekaClient
@SpringBootApplication
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    //embedded h2 console
    @Bean
    ServletRegistrationBean h2servletRegistration(){
        ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet())
        registrationBean.addUrlMappings("/console/*")
        return registrationBean
    }
}