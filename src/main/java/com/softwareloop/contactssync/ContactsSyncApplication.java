package com.softwareloop.contactssync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareloop.contactssync.security.UserSessionArgumentResolver;
import com.softwareloop.contactssync.security.UserSessionInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@SpringBootApplication
public class ContactsSyncApplication extends WebMvcConfigurerAdapter {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Dependencies
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // WebMvcConfigurerAdapter overrides
    //--------------------------------------------------------------------------

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("/", "classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(userSessionInterceptor());
    }

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userSessionArgumentResolver());
    }

    //--------------------------------------------------------------------------
    // Beans
    //--------------------------------------------------------------------------

    @Bean
    public UserSessionInterceptor userSessionInterceptor() {
        return new UserSessionInterceptor();
    }

    @Bean
    public UserSessionArgumentResolver userSessionArgumentResolver() {
        return new UserSessionArgumentResolver();
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver =
                new InternalResourceViewResolver();
        viewResolver.setViewClass(InternalResourceView.class);
        viewResolver.setPrefix("/");
        viewResolver.setSuffix(".html");

        return viewResolver;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        SpringApplication.run(ContactsSyncApplication.class, args);
    }
}
