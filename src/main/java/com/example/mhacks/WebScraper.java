package com.example.mhacks;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class WebScraper {

    public static void main(String[] args) {
        // URL of the website you want to scrape
        String url = "https://codeforces.com/problemset/problem/1905/D";

        try {
            // Connect to the website and get the HTML document
            Document document = Jsoup.connect(url).get();
            int p = 0;
            while(true) {
            	String id = "test-example-line test-example-line-even test-example-line-" + p;
            	String id2 = "test-example-line test-example-line-odd test-example-line-" + p;
            	p++;
            	Elements sampleTestElement = document.getElementsByClass(id);
            	Elements sampleTestElementd = document.getElementsByClass(id2);
            	if(sampleTestElement == null && sampleTestElementd == null) break;
            	if(sampleTestElement.size() == 0 && sampleTestElementd.size() == 0) break;
            	if(sampleTestElement.size() > sampleTestElementd.size()) {
            		System.out.println("Content of the element: " + sampleTestElement.text());
            	}
            	else {
            		System.out.println("Content of the element: " + sampleTestElementd.text());
            	}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
