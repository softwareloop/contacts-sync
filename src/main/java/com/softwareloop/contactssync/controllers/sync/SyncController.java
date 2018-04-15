package com.softwareloop.contactssync.controllers.sync;

import com.softwareloop.contactssync.controllers.SimpleResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

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

    @PostMapping("/sync")
    public SimpleResponse sync() {
        return new SimpleResponse("Ok");
    }
}
