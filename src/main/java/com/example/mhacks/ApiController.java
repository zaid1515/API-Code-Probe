package com.example.mhacks;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class ApiController {
	
	@PostMapping("/submit-api")
	public ResponseEntity<Map<String, Object>> run_api(@RequestParam("api-url") String api_url, @RequestParam("keys") String[] key, @RequestParam("values") String[] value, @RequestParam("api-method") String method, @RequestParam("request-body") String[] body) {
		System.out.println("api-url" + " : " + api_url);
		for(int i=0;i<key.length;i++) System.out.println("(key, value) : " + "(" + key[i] + "," + value[i] + ")");
		System.out.println("method : " + method);
		for(int i=0;i<body.length;i++) System.out.println("Body " + (i+1) + " : " + body[i]);
		if(body.length == 0) {
			body = new String[1];
		}
		String codes[] = new String[body.length];
		String output[] = new String[body.length];
		for(int index = 0;index < body.length; index++) {
			try {
	            URL url = new URL(api_url);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod(method);
	            connection.setDoOutput(true);
	            connection.setDoInput(true);
	            connection.setRequestProperty("Content-Type", "application/json");
	            for(int i=0;i<key.length;i++) connection.setRequestProperty(key[i], value[i]);
	            if(method.equals("POST")) {
		            byte[] postData = body[index].getBytes(StandardCharsets.UTF_8);
		            connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
		            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {wr.write(postData);}
	            }
	            int responseCode = connection.getResponseCode();
	            System.out.println("Response Code: " + responseCode);
	            codes[index] = responseCode + "";
	            String final_output = "";
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
	                String line;
	                StringBuilder response = new StringBuilder();
	                while ((line = reader.readLine()) != null) {
	                    response.append(line);
	                }
	                String res = response.toString();
	                int start = 0;
	                while(true) {
	                	int end = start + 100;
	                	if(end > res.length()) {
	                		end = res.length();
	                		final_output += res.substring(start, end);
	                		break;
	                	}
	                	final_output += res.substring(start, end);
	                	start = end;
	                }
	            }
	            output[index] = final_output;
	            System.out.println(final_output);
	            connection.disconnect();
			}
			catch(Exception e) {}
			if(body.length == 0) break;
		}
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("codes", codes);
	    responseMap.put("outputs", output);
	    System.out.println(responseMap);
	    return ResponseEntity.status(HttpStatus.OK).body(responseMap);
	}
}
