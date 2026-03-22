package com.suhas.auth.controller;

import com.suhas.common.controller.BaseClientLogController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // This maps to /auth/logs
public class ClientLogController extends BaseClientLogController {
    // No code needed here! It inherits the @PostMapping from the base class.
}