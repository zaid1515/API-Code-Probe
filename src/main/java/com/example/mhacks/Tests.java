package com.example.mhacks;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Controller
public class Tests {

    @PostMapping("/test1")
    public ResponseEntity<String> test1(@RequestBody Map<String, Object> requestMap) {
        try {
            String key1 = (String) requestMap.get("key1");
            String key2 = (String) requestMap.get("key2");

            String result = key1 + key2;

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("ans", result);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(responseMap);

            return ResponseEntity.status(HttpStatus.OK).body(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the response");
        }
    }
}
