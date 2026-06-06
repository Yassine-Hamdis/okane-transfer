package com.okanetransfer;

import com.okanetransfer.config.AppConfig;
import com.okanetransfer.config.OpenApiServlet;
import com.okanetransfer.config.SecurityConfig;
import com.okanetransfer.config.WebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppConfig.class, SecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    public void onStartup(jakarta.servlet.ServletContext servletContext)
            throws jakarta.servlet.ServletException {
        super.onStartup(servletContext);
        servletContext.addServlet("openApiServlet", new OpenApiServlet())
                .addMapping("/v3/api-docs");
    }
}