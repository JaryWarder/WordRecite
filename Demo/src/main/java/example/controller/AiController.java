package example.controller;

import example.common.Result;
import example.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @GetMapping("/analyze")
    public Result<String> analyze(HttpServletRequest request,
                                  @RequestParam(required = false) String origin) {
        Object authedUsername = request.getAttribute("username");
        if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
            return Result.error(401, "Unauthorized");
        }
        return aiService.analyze((String) authedUsername, origin);
    }
}
