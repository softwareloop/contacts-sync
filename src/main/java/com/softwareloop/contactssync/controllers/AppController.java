package com.softwareloop.contactssync.controllers;

import com.softwareloop.contactssync.security.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class AppController {

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
    // Interface implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @GetMapping("/")
    public String index(UserSession userSession, Model model) {
        if (userSession != null) {
            model.addAttribute(userSession);
        }
        return "index";
    }

    @GetMapping("/app/**")
    public String app(HttpServletRequest request) {
        log.debug("Path: {}", request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));

        return "app";
    }

}
