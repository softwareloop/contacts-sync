package com.softwareloop.contactssync.security;

import com.softwareloop.contactssync.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.softwareloop.contactssync.security.SecurityConstants.CSRF_TOKEN_HEADER;
import static com.softwareloop.contactssync.security.SecurityConstants.POST_AUTH_REDIRECT_ATTRIBUTE;
import static com.softwareloop.contactssync.security.SecurityConstants.USER_SESSION_ATTRIBUTE;

@Slf4j
@Component
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

    @Autowired
    public SecurityInterceptor(UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    //--------------------------------------------------------------------------
    // HandlerInterceptorAdapter overrides
    //--------------------------------------------------------------------------

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        String requestUri = urlPathHelper.getLookupPathForRequest(request);
        log.debug("requestUri: {}", requestUri);

        addResponseHeaders(response);

        HttpSession httpSession = request.getSession();
        UserSession userSession =
                (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);

        return isAuthenticationOk(request, response, userSession, requestUri) &&
                isCsrfOk(request, response, userSession, requestUri);
    }

    //--------------------------------------------------------------------------
    // Authentication-related methods
    //--------------------------------------------------------------------------

    private boolean isAuthenticationOk(
            HttpServletRequest request,
            HttpServletResponse response,
            UserSession userSession,
            String requestUri
    ) throws IOException {
        if (userSession != null) {
            return true;
        }

        if (!isAuthenticationRequired(requestUri)) {
            return true;
        }

        if ("GET".equals(request.getMethod())) {
            redirectToLogin(request, response, requestUri);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return false;
    }

    private boolean isAuthenticationRequired(String requestURI) {
        return requestURI.startsWith("/app") || requestURI.startsWith("/api");
    }

    //--------------------------------------------------------------------------
    // CSRF-related methods
    //--------------------------------------------------------------------------

    private boolean isCsrfOk(
            HttpServletRequest request,
            HttpServletResponse response,
            UserSession userSession,
            String requestUri
    ) {
        if (!isCsrfProtectionRequired(requestUri)) {
            return true;
        }

        String csrfToken = request.getHeader(CSRF_TOKEN_HEADER);
        if (userSession != null && userSession.getCsrfToken().equals(csrfToken)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private boolean isCsrfProtectionRequired(String requestURI) {
        return requestURI.startsWith("/api");
    }

    //--------------------------------------------------------------------------
    // Utility
    //--------------------------------------------------------------------------

    private void addResponseHeaders(
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

    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestUri
    ) throws IOException {
        String fullPath = requestUri;
        String queryString = request.getQueryString();
        if (queryString != null) {
            fullPath = fullPath + "?" + queryString;
        }
        response.sendRedirect(String.format(
                "/login?%s=%s",
                POST_AUTH_REDIRECT_ATTRIBUTE,
                TextUtils.urlEncode(fullPath)));
    }


}
