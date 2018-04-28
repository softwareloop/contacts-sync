package com.softwareloop.contactssync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.softwareloop.contactssync.security.UserSessionArgumentResolver;
import com.softwareloop.contactssync.security.SecurityInterceptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ContactsSyncApplication extends WebMvcConfigurerAdapter {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    private static final String GOOGLE_TOKEN_URL =
            "https://www.googleapis.com/oauth2/v4/token";

    private static final List<String> SCOPES = Arrays.asList(
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/contacts");

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Dependencies
    //--------------------------------------------------------------------------

    @Getter
    @Setter
    @Value("${google.clientId}")
    private String googleClientId;

    @Getter
    @Setter
    @Value("${google.clientSecret}")
    private String googleClientSecret;

    @Autowired
    public SecurityInterceptor securityInterceptor;

    @Autowired
    public UserSessionArgumentResolver userSessionArgumentResolver;

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
        interceptorRegistry.addInterceptor(securityInterceptor);
    }

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userSessionArgumentResolver);
    }

    //--------------------------------------------------------------------------
    // Beans
    //--------------------------------------------------------------------------

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

    @Bean
    public NetHttpTransport netHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JacksonFactory jacksonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    public AuthorizationCodeFlow authorizationCodeFlow() throws IOException {
        GoogleAuthorizationCodeFlow.Builder flowBuilder =
                new GoogleAuthorizationCodeFlow.Builder(
                        netHttpTransport(),
                        jacksonFactory(),
                        googleClientId,
                        googleClientSecret,
                        SCOPES);

        // Ugrade the url to the latest version to get more OpenID fields
        flowBuilder.setTokenServerUrl(new GenericUrl(GOOGLE_TOKEN_URL));

        return flowBuilder.setDataStoreFactory(dataStoreFactory())
                .setAccessType("offline")
                .build();
    }

    @Bean
    public DataStoreFactory dataStoreFactory() {
        return new MemoryDataStoreFactory();
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        SpringApplication.run(ContactsSyncApplication.class, args);
    }
}
