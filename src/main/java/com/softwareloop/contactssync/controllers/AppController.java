package com.softwareloop.contactssync.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareloop.contactssync.security.UserSession;
import com.softwareloop.contactssync.util.TextUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class AppController {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    @Getter
    private final ObjectMapper objectMapper;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public AppController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //--------------------------------------------------------------------------
    // Interface implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @GetMapping("/")
    public String index(UserSession userSession) {
        if (userSession != null) {
            return "redirect:/app";
        }
        return "index";
    }

    @GetMapping("/app/**")
    public String app(
            UserSession userSession,
            Model model
    ) throws JsonProcessingException {
        String userSessionJson = String.format(
                "window.userSession = %s",
                objectMapper.writeValueAsString(userSession));

        model.addAttribute("userSessionJson",
                TextUtils.escapeJsForInlineScript(userSessionJson));
        return "app";
    }

    @RequestMapping("/logout")
    public String logout(
            HttpSession httpSession,
            UserSession userSession
    ) {
        httpSession.invalidate();
        return "redirect:/";
    }



}
