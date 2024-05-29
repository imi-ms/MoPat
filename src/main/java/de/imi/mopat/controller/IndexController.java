package de.imi.mopat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Simple Controller that handles requests to domain without any logic.
 */
@Controller
public class IndexController {

    /**
     * Redirects root URI to /mobile/user/login.
     *
     * @return login view
     */
    @GetMapping
    public String startPage() {
        return "/mobile/user/login";
    }

    /**
     * Redirects standard index access to start page.
     *
     * @return redirect to start page
     */
    @GetMapping("index")
    public String startPageRedirect() {
        return "redirect:/";
    }

    /**
     * Just a dummy endpoint to ensure that the favicon.ico is returning a 200 response
     */
    @GetMapping("/favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
    }
}
