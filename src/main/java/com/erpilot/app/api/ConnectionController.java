package com.erpilot.app.api;

import com.erpilot.app.common.dto.ConnectionConfig;
import com.erpilot.app.core.ConnectionSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionSessionService connectionSessionService;

    public ConnectionController(ConnectionSessionService connectionSessionService) {
        this.connectionSessionService = connectionSessionService;
    }

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody ConnectionConfig config) {
        String dialect = connectionSessionService.connect(config);
        return ResponseEntity.ok(Map.of(
                "connected", true,
                "dialecte", dialect
        ));
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean connected = connectionSessionService.isConnected();
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("connected", connected);
        body.put("dialecte", connected ? connectionSessionService.getActiveDialectName() : null);
        return body;
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        connectionSessionService.disconnect();
        return ResponseEntity.noContent().build();
    }
}