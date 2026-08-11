package com.erpilot.app.api;

import com.erpilot.app.api.dto.QueryApiRequest;
import com.erpilot.app.api.dto.QueryApiResponse;
import com.erpilot.app.core.OrchestrationResult;
import com.erpilot.app.core.QueryOrchestrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QueryController {

    private final QueryOrchestrationService orchestrationService;

    public QueryController(QueryOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/query")
    public QueryApiResponse query(@RequestBody QueryApiRequest request) {
        OrchestrationResult result = orchestrationService.processQuestion(
                request.getQuestion(), request.getRole());
        return QueryApiResponse.from(result);
    }
}