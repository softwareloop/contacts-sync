package com.softwareloop.contactssync.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;

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

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // HandlerInterceptorAdapter overrides
    //--------------------------------------------------------------------------

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.debug("preHandle: {}", requestURI);
        HttpSession httpSession = request.getSession();
        UserSession userSession =
                (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);

        if (userSession != null) {
            // the user is authenticated
            return true;
        }

        if (requestURI.startsWith("/app") || requestURI.startsWith("/api")) {
            if ("GET".equals(request.getMethod())) {
                // redirect to login
                String fullPath = requestURI + "?" + request.getQueryString();
                response.sendRedirect(String.format(
                        "/login?%s=%s",
                        POST_AUTH_REDIRECT_ATTRIBUTE,
                        URLEncoder.encode(fullPath, "UTF-8")));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return false;
        }
        if (requestURI.startsWith("/api")) {
            String csrfToken = request.getHeader(CSRF_TOKEN_HEADER);
            if (!userSession.getCsrfToken().equals(csrfToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }

        return true;
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
