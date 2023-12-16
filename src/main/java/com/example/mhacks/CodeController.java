package com.example.mhacks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class CodeController {
	
	class Pair {String f; String s; Pair(String f, String s) {this.f = f;this.s = s;}@Override public boolean equals(Object o) {if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;Pair pair = (Pair) o;return f.equals(pair.f) && s.equals(pair.s);}@Override public int hashCode() {return Objects.hash(f, s);}}
	
	@GetMapping("")
	public String start() {
		return "codingtest.html";
	}
	
	@PostMapping("/submit-code")
	public ResponseEntity<String> run_code(@RequestParam String code, @RequestParam String[] inputs, @RequestParam String[] outputs, @RequestParam String language){
		try {code = URLDecoder.decode(code, "UTF-8");}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		System.out.println("Language Selected => " + language);
		int n = inputs.length;
		String verdicts[] = new String[n];
		String f_outputs[] = new String[n];
		for(int i=0;i<n;i++) {
			String input = inputs[i];
			String output = outputs[i];
			System.out.println(code + " " + input + " " + output);
			Pair f_output = null;
			if(language.equals("Java")) f_output = run(code, input, output);
			else if (language.equals("Cpp")) f_output = run_cpp(code, input, output);
			else if (language.equals("Python")) f_output = runpy(code, input, output);
			f_outputs[i] = f_output.s;
			verdicts[i] = f_output.f;
			System.out.println(f_outputs[i] + " " + verdicts[i]);
		}
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("foutputs", f_outputs);
	    responseMap.put("verdicts", verdicts);
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        String jsonResponse = objectMapper.writeValueAsString(responseMap);
	        System.out.println(jsonResponse);
	        return ResponseEntity.status(HttpStatus.OK).body(jsonResponse);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the response");
	    }
	}
	
	public Pair run(String code, String input, String output) {
		String expected[] = output.trim().split("\n");
		File sourceFile = new File(extract(code) + ".java");
        FileWriter writer;
		try {
			writer = new FileWriter(sourceFile);
			writer.write(code);
	        writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ExecutorService executor = Executors.newSingleThreadExecutor();
        // Use a Future to track the execution
        Future<Pair> future = executor.submit(() -> {
            // Compile the Java source file
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			int compilationResult = compiler.run(null, null, null, sourceFile.getPath());

			if (compilationResult == 0) {
			    // Compilation succeeded, execute the compiled class
			    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
			    String className = sourceFile.getName().replace(".java", "");
			    ProcessBuilder processBuilder = new ProcessBuilder("java", className);
			    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
			    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
			    long start = System.currentTimeMillis();
			    Process process = processBuilder.start();

                // Write the input to the standard input of the process
                try (OutputStream outputStream = process.getOutputStream()) {
                    byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(inputBytes);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
			    int i = 0;
			    boolean flag = true;
			    String foutput = "";
			    while ((line = reader.readLine()) != null) {
			    	if(i == expected.length) flag = false;
			    	if(!line.trim().equals(expected[i++].trim())) flag = false;
			    	foutput += line.trim() + "\n";
	            }
			    if(!flag) return new Pair("Wrong Answer", foutput);
			    return new Pair("Passed", foutput);
			} else {
			    return new Pair("Compilation Failed ", "-1");
			}
        });
        try {
            Pair result = future.get(5, TimeUnit.SECONDS); // 5 seconds timeout
            return result;
        } catch (Exception e) {
            System.out.println("Time Out occurred ");
            future.cancel(true);
            //System.out.println("TLE ");
            return new Pair("TLE  ", "-1");
        }
	}
	
	public Pair run_cpp(String f_code, String input, String output) {
    	File sourceFile = new File("main.cpp");
        FileWriter writer;
        try {
            writer = new FileWriter(sourceFile);
            writer.write(f_code);
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String expected[] = output.trim().split("\n");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Pair> future = executor.submit(() -> {
        	ProcessBuilder processBuilder = new ProcessBuilder("g++", sourceFile.getPath(), "-o", "output");
            Process compileProcess;
            try {
                compileProcess = processBuilder.start();
                int compileExitCode = compileProcess.waitFor();
                if (compileExitCode == 0) {
                	processBuilder = new ProcessBuilder("./output");
                    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
                    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    long start = System.currentTimeMillis();
                    try {
                        Process process = processBuilder.start();
                        try (OutputStream outputStream = process.getOutputStream()) {
                            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
                            outputStream.write(inputBytes);
                        }
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        int i = 0;
        			    boolean flag = true;
        			    String foutput = "";
        			    while ((line = reader.readLine()) != null) {
        			    	if(i == expected.length) flag = false;
        			    	if(!line.trim().equals(expected[i++].trim())) flag = false;
        			    	foutput += line.trim() + "\n";
        	            }
        			    int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            return new Pair("Execution Failed with exit code " + exitCode, "-1");
                        }
        			    if(!flag) return new Pair("Wrong Answer", foutput);
        			    return new Pair("Passed", foutput);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        return new Pair("Internal Error ", "-1");
                    }
                } else {
                    return new Pair("Compilation Failed ", "-1");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return new Pair("Internal Error", "-1");
            }
        });

        try {
            Pair result = future.get(5, TimeUnit.SECONDS); // 5 seconds timeout
            return result;
        } catch (Exception e) {
            System.out.println("Time Out occurred ");
            future.cancel(true);
            //System.out.println("TLE ");
            return new Pair("TLE  ", "-1");
        }
	}
	
	public Pair runpy(String code, String input, String output) {
        String[] expected = output.trim().split("\n");
        ArrayList<Long> times = new ArrayList<>();

        // Write the Python code to a file
        File sourceFile = new File("main.py");
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(code);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Pair> future = executor.submit(() -> {
            // Run Python code
            ProcessBuilder processBuilder = new ProcessBuilder("python", sourceFile.getPath());
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

            try {
                Process process = processBuilder.start();

                // Write the input to the standard input of the process
                try (OutputStream outputStream = process.getOutputStream()) {
                    byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(inputBytes);
                }

                // Capture the output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                int i = 0;
			    boolean flag = true;
			    String foutput = "";
			    while ((line = reader.readLine()) != null) {
			    	if(i == expected.length) flag = false;
			    	if(!line.trim().equals(expected[i++].trim())) flag = false;
			    	foutput += line.trim() + "\n";
	            }
			    int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return new Pair("Execution Failed with exit code " + exitCode, "-1");
                }
			    if(!flag) return new Pair("Wrong Answer", foutput);
			    return new Pair("Passed", foutput);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return new Pair("Execution Error", "-1");
            }
        });
        try {
            Pair result = future.get(5, TimeUnit.SECONDS); // 5 seconds timeout
            return result;
        } catch (Exception e) {
            System.out.println("Time Out occurred ");
            future.cancel(true);
            //System.out.println("TLE ");
            return new Pair("TLE  ", "-1");
        }
    }
	
	public String extract(String code) {
		   String className = null;
	       Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
	       Matcher matcher = pattern.matcher(code);
	       if (matcher.find()) className = matcher.group(1);
	       else className = "Main";
	       return className;
	}
}
