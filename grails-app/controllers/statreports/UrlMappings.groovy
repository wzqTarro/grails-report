package statreports

@SuppressWarnings(['UnnecessaryGString'])
class UrlMappings {

    static mappings = {
        "/reportInputs/editReportInput"(controller: "reportInputs", action: 'edit')

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
