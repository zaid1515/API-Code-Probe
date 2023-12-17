package com.example.mhacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FileController {
	
	ArrayList<String> list;
	ArrayList<String> methods;
	
	@GetMapping("/go")
	public String start(Model model) {
		return "filefetch.html";
	}

	@PostMapping("/path-fetch")
	public String RetrieveFiles(@RequestParam("path") String path, @RequestParam("method") String method, Model model) {
		list = new ArrayList<String>();
		methods = new ArrayList<String>();
		System.out.println(path + " : " + method);
		File maindir = new File(path);
		if (maindir.exists() && maindir.isDirectory()) {
          File arr[] = maindir.listFiles();
          RecursivePrint(arr, 0, 0);
		}
		System.out.println(list);
		for(String file : list) {
			try {
			      File myObj = new File(path + "\\" + file);
			      Scanner myReader = new Scanner(myObj);
			      String code = "";
			      while (myReader.hasNextLine()) {
			        String data = myReader.nextLine();
			        code += data + "\n";
			        //System.out.println(data);
			      }
			      extract(code, method);
			      //System.out.println(code);
			      myReader.close();
			    } catch (FileNotFoundException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
		}
		System.out.println(methods);
		model.addAttribute("wordList", methods);
		model.addAttribute("method", method);
		model.addAttribute("path", path);
		return "filefetch.html";
	}
	
	
	
	void extract(String code, String method) {
		int find = code.indexOf("class");
		while(true) {
			try {
				int index1 = code.indexOf("@PostMapping", find);
				int index2 = code.indexOf("@GetMapping", find);
				int index3 = code.indexOf("@PutMapping", find);
				int index4 = code.indexOf("@DeleteMapping", find);
				if(index1 == -1 && index2 == -1 && index3 == -1 && index4 == -1) break;
				if(index1 == -1) index1 = (int)1e9;
				if(index2 == -1) index2 = (int)1e9;
				if(index3 == -1) index3 = (int)1e9;
				if(index4 == -1) index4 = (int)1e9;
				if(index1 < index2 && index1 < index3 && index1 < index4) {
					if(method.equals("POST")) methods.add(code.substring(code.indexOf("(", index1) + 2, code.indexOf(")", index1 + 1) - 1));
					//System.out.println(code.substring(index1, code.indexOf(")", index1) + 1));
					find = index1 + 1;
					continue;
				}
				if(index2 < index1 && index2 < index3 && index2 < index4) {
					if(method.equals("GET")) methods.add(code.substring(code.indexOf("(", index2) + 2, code.indexOf(")", index2 + 1) - 1));
					//System.out.println(code.substring(index2, code.indexOf(")", index2) + 1));
					find = index2 + 1;
					continue;
				}
				if(index3 < index1 && index3 < index2 && index3 < index4) {
					if(method.equals("PUT")) methods.add(code.substring(code.indexOf("(", index3) + 2, code.indexOf(")", index3 + 1) - 1));
					//System.out.println(code.substring(index3, code.indexOf(")", index3) + 1));
					find = index3 + 1;
					continue;
				}
				if(index4 < index1 && index4 < index2 && index4 < index3) {
					if(method.equals("DELETE")) methods.add(code.substring(code.indexOf("(", index4) + 2, code.indexOf(")", index4 + 1) - 1));
					//System.out.println(code.substring(index3, code.indexOf(")", index3) + 1));
					find = index4 + 1;
					continue;
				}
			}
			catch(Exception e) {
				break;
			}
		}
	}
	
	void RecursivePrint(File[] arr, int index, int level) {
        if (index == arr.length) return;
        if (arr[index].isFile()) {
        	if(arr[index].getName().endsWith(".java") && !arr[index].getName().equals("FilesRead.java")) list.add(arr[index].getName());
        	System.out.println(arr[index].getName());
        }	
        else if (arr[index].isDirectory()) RecursivePrint(arr[index].listFiles(), 0, level + 1);
        RecursivePrint(arr, ++index, level);
    }
	
}
