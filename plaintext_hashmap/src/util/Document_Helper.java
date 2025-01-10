<<<<<<< HEAD
package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;



public class Document_Helper {
	
	public Document_Helper() {}
	
	public HashMap<String, ArrayList<Integer>> readInvertedIndex(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader br = new BufferedReader(fileReader);
		
		HashMap<String, ArrayList<Integer>> inverted_index = new HashMap<String, ArrayList<Integer>>();
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] kvs = line.split(",");
			
			String keyword = kvs[0];
			ArrayList<Integer> doc_ids = new ArrayList<Integer>();
			for (int ii = 1; ii < kvs.length; ii++) {
				doc_ids.add(Integer.parseInt(kvs[ii]));
			}
			inverted_index.put(keyword, doc_ids);
		}
		br.close();
		fileReader.close();
		return inverted_index;
	}
	

	
	public ArrayList<byte[]> readDocuments(String filename) throws IOException {
		ArrayList<byte[]> documents = new ArrayList<byte[]>();
		
		Integer doc_counter = 0;
		
	    Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path, StandardCharsets.US_ASCII);
		
		br.readLine();
		
		String document = "";
		String line = "";
		boolean continue_flag = true;
		while ((line = br.readLine()) != null) {
			if (line.equals("NEW_FILE")) {
				if (document.length() == 0) {
					document = " ";
				}
				byte[] document_compressed = GZIPCompression.compress(document);
				
				documents.add(document_compressed);
				doc_counter += 1;

				document = "";
				
				if (doc_counter % 20000 == 0) {
					System.out.println("Reading document " + (doc_counter));
				}
			}
			else {
				document += line;
			}
		}
		
		if (document.length() == 0) {
			document = " ";
		}
		byte[] document_compressed = GZIPCompression.compress(document);
		documents.add(document_compressed);
		
		System.out.println("Documents loaded.");
		
		
		
		return documents;
	}
}
=======
package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;



public class Document_Helper {
	
	public Document_Helper() {}
	
	public HashMap<String, ArrayList<Integer>> readInvertedIndex(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader br = new BufferedReader(fileReader);
		
		HashMap<String, ArrayList<Integer>> inverted_index = new HashMap<String, ArrayList<Integer>>();
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] kvs = line.split(",");
			
			String keyword = kvs[0];
			ArrayList<Integer> doc_ids = new ArrayList<Integer>();
			for (int ii = 1; ii < kvs.length; ii++) {
				doc_ids.add(Integer.parseInt(kvs[ii]));
			}
			inverted_index.put(keyword, doc_ids);
		}
		br.close();
		fileReader.close();
		return inverted_index;
	}
	

	
	public ArrayList<byte[]> readDocuments(String filename) throws IOException {
		ArrayList<byte[]> documents = new ArrayList<byte[]>();
		
		Integer doc_counter = 0;
		
	    Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path, StandardCharsets.US_ASCII);
		
		br.readLine();
		
		String document = "";
		String line = "";
		boolean continue_flag = true;
		while ((line = br.readLine()) != null) {
			if (line.equals("NEW_FILE")) {
				if (document.length() == 0) {
					document = " ";
				}
				byte[] document_compressed = GZIPCompression.compress(document);
				
				documents.add(document_compressed);
				doc_counter += 1;

				document = "";
				
				if (doc_counter % 20000 == 0) {
					System.out.println("Reading document " + (doc_counter));
				}
			}
			else {
				document += line;
			}
		}
		
		if (document.length() == 0) {
			document = " ";
		}
		byte[] document_compressed = GZIPCompression.compress(document);
		documents.add(document_compressed);
		
		System.out.println("Documents loaded.");
		
		
		
		return documents;
	}
}
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e
