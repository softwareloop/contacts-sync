package com.softwareloop.contactssync.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.softwareloop.contactssync.security.SecurityConstants.CSRF_TOKEN_HEADER;
import static com.softwareloop.contactssync.security.SecurityConstants.POST_AUTH_REDIRECT_ATTRIBUTE;
import static com.softwareloop.contactssync.security.SecurityConstants.USER_SESSION_ATTRIBUTE;

@Slf4j
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final UrlPathHelper urlPathHelper;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public SecurityInterceptor(UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    //--------------------------------------------------------------------------
    // HandlerInterceptorAdapter overrides
    //--------------------------------------------------------------------------

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestURI = urlPathHelper.getLookupPathForRequest(request);
        log.debug("preHandle: {}", requestURI);

        addResponseHeaders(response);

        HttpSession httpSession = request.getSession();
        UserSession userSession =
                (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);

        if (userSession == null && requiresAuthentication(requestURI)) {
            if ("GET".equals(request.getMethod())) {
                redirectToLogin(request, response, requestURI);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return false;
        }

        if (requiresCsrfProtection(requestURI)) {
            String csrfToken = request.getHeader(CSRF_TOKEN_HEADER);
            if (userSession == null ||
                    !userSession.getCsrfToken().equals(csrfToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }

        return true;
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public void addResponseHeaders(
            HttpServletResponse response
    ) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("X-Frame-Options", "DENY");

        response.setHeader("Cache-Control",
                "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    public boolean requiresAuthentication(String requestURI) {
        return requestURI.startsWith("/app") || requestURI.startsWith("/api");
    }

    public boolean requiresCsrfProtection(String requestURI) {
        return requestURI.startsWith("/api");
    }

    public void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestURI
    ) throws IOException {
        String fullPath = requestURI + "?" + request.getQueryString();
        response.sendRedirect(String.format(
                "/login?%s=%s",
                POST_AUTH_REDIRECT_ATTRIBUTE,
                URLEncoder.encode(fullPath, "UTF-8")));
    }


}
