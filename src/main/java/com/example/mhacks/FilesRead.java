package com.example.mhacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FilesRead {
	static ArrayList<String> list = new ArrayList<>();
	static ArrayList<String> maps = new ArrayList<>();
	static ArrayList<String> get = new ArrayList<>();
	static ArrayList<String> post = new ArrayList<>();
	static ArrayList<String> delete = new ArrayList<>();
	static ArrayList<String> put = new ArrayList<>();
	public static void main(String args[]) {
		String path = "C:\\Users\\Dhrumil\\OneDrive\\Desktop\\kaam\\Mhack\\src\\main\\java\\com\\example\\mhacks";
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
			      extract(code);
			      //System.out.println(code);
			      myReader.close();
			    } catch (FileNotFoundException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
		}
		System.out.println(get);
		System.out.println(post);
		System.out.println(put);
		System.out.println(delete);
	}
	
	static void RecursivePrint(File[] arr, int index, int level) {
        if (index == arr.length) return;
        if (arr[index].isFile()) {if(arr[index].getName().endsWith(".java") && !arr[index].getName().equals("FilesRead.java")) list.add(arr[index].getName());}	
        else if (arr[index].isDirectory()) RecursivePrint(arr[index].listFiles(), 0, level + 1);
        RecursivePrint(arr, ++index, level);
    }
	
	static void extract(String code) {
		int find = code.indexOf("class");
		while(true) {
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
				post.add(code.substring(code.indexOf("(", index1) + 2, code.indexOf(")", index1 + 1) - 1));
				//System.out.println(code.substring(index1, code.indexOf(")", index1) + 1));
				find = index1 + 1;
				continue;
			}
			if(index2 < index1 && index2 < index3 && index2 < index4) {
				get.add(code.substring(code.indexOf("(", index2) + 2, code.indexOf(")", index2 + 1) - 1));
				//System.out.println(code.substring(index2, code.indexOf(")", index2) + 1));
				find = index2 + 1;
				continue;
			}
			if(index3 < index1 && index3 < index2 && index3 < index4) {
				put.add(code.substring(code.indexOf("(", index3) + 2, code.indexOf(")", index3 + 1) - 1));
				//System.out.println(code.substring(index3, code.indexOf(")", index3) + 1));
				find = index3 + 1;
				continue;
			}
			if(index4 < index1 && index4 < index2 && index4 < index3) {
				delete.add(code.substring(code.indexOf("(", index4) + 2, code.indexOf(")", index4 + 1) - 1));
				//System.out.println(code.substring(index3, code.indexOf(")", index3) + 1));
				find = index4 + 1;
				continue;
			}
		}
	}
}
