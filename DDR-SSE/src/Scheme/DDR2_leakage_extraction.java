<<<<<<< HEAD
package Scheme;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;
import util.Document_Helper;

public class DDR2_leakage_extraction {


    public static void main(String[] args) throws Exception {
    	
    	Integer bucket_size = 200;
    	Integer N_queries = 1200;
    	
    	Client client = new Client();
    	
    	// load inverted index and documents
    	client.loadDatabase(args[0], bucket_size);
    	
    	
    	// client setup
    	client.setup();
    	System.out.println("Index and documents encrypted.");
    	
    	// server setup
    	Server server = new Server(client.xor_EMM, client.EDocs, client.XOR_LEVEL, client.STORAGE_XOR);
    	System.out.println("Server setup.");
    	
    	
    	// leakage extraction
    	ArrayList<String> keywords = new ArrayList<String>(client.keyword_frequency.keySet());
    	keywords.sort((a1, a2)-> client.keyword_frequency.get(a2) - client.keyword_frequency.get(a1));
    	Integer[] percentiles = {100, 95, 90, 85, 80, 75};
    	
    	for (Integer percentile : percentiles) {
    		
    		PrintWriter writer_leakage = new PrintWriter(String.format("../leakage/leakage_%d_%d_%d_%d.txt", client.EDocs.size()/2, N_queries, bucket_size, percentile), "UTF-8");
    	
	    	// load queries
    		ArrayList<String> queries = Document_Helper.loadQueries("../emails_parsed/", args[0], N_queries, percentile);
	    	Collections.shuffle(queries);
	
	    	System.out.println(String.format("Queries on the %d-th percentile started.", percentile));
	    	
	    	for (Integer ii = 0; ii < N_queries; ii++) {
	    		if (ii % 100 == 0) {
	    			System.out.println("Query Progress: " + ii);
	    		}
	    		
	    		String keyword = queries.get(ii);
	    		
	    		// query the index
	    		byte[] tk_key = client.indexQueryGen(keyword);
	    		server.Query_Xor(tk_key, client.keyword_frequency.get(keyword));
	    		
	    		ArrayList<byte[]> c_key = server.Get_C_key();
	    		ArrayList<Integer> matching_indices = client.indexResultDecrypt(c_key, keyword);
	    		
	    		ArrayList<String> docAddrs = client.documentQueryGen(matching_indices, keyword);
	    		server.Query_docs(docAddrs);
	    		
	    		ArrayList<byte[]> encryptedDocuments = server.get_matching_docs();
	    		ArrayList<String> matching_documents = client.decryptDocuments(encryptedDocuments);
	    		

	    		writer_leakage.write(keyword + ",");
	    		for (int add_idx = 0; add_idx < docAddrs.size()-1; add_idx++) {
	    			writer_leakage.write(docAddrs.get(add_idx) + ",");
	    		}
	    		writer_leakage.write(docAddrs.get(docAddrs.size()-1) + "\n");
	    		writer_leakage.flush();
	    		
	    		server.Clear();
	    	}
	    	
	    	System.out.println("Queries done.");
	    	
	    	writer_leakage.close();
    	}
    	
    }
}
=======
package Scheme;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;
import util.Document_Helper;

public class DDR2_leakage_extraction {


    public static void main(String[] args) throws Exception {
    	
    	Integer bucket_size = 200;
    	Integer N_queries = 1200;
    	
    	Client client = new Client();
    	
    	// load inverted index and documents
    	client.loadDatabase(args[0], bucket_size);
    	
    	
    	// client setup
    	client.setup();
    	System.out.println("Index and documents encrypted.");
    	
    	// server setup
    	Server server = new Server(client.xor_EMM, client.EDocs, client.XOR_LEVEL, client.STORAGE_XOR);
    	System.out.println("Server setup.");
    	
    	
    	// leakage extraction
    	ArrayList<String> keywords = new ArrayList<String>(client.keyword_frequency.keySet());
    	keywords.sort((a1, a2)-> client.keyword_frequency.get(a2) - client.keyword_frequency.get(a1));
    	Integer[] percentiles = {100, 95, 90, 85, 80, 75};
    	
    	for (Integer percentile : percentiles) {
    		
    		PrintWriter writer_leakage = new PrintWriter(String.format("../leakage/leakage_%d_%d_%d_%d.txt", client.EDocs.size()/2, N_queries, bucket_size, percentile), "UTF-8");
    	
	    	// load queries
    		ArrayList<String> queries = Document_Helper.loadQueries("../emails_parsed/", args[0], N_queries, percentile);
	    	Collections.shuffle(queries);
	
	    	System.out.println(String.format("Queries on the %d-th percentile started.", percentile));
	    	
	    	for (Integer ii = 0; ii < N_queries; ii++) {
	    		if (ii % 100 == 0) {
	    			System.out.println("Query Progress: " + ii);
	    		}
	    		
	    		String keyword = queries.get(ii);
	    		
	    		// query the index
	    		byte[] tk_key = client.indexQueryGen(keyword);
	    		server.Query_Xor(tk_key, client.keyword_frequency.get(keyword));
	    		
	    		ArrayList<byte[]> c_key = server.Get_C_key();
	    		ArrayList<Integer> matching_indices = client.indexResultDecrypt(c_key, keyword);
	    		
	    		ArrayList<String> docAddrs = client.documentQueryGen(matching_indices, keyword);
	    		server.Query_docs(docAddrs);
	    		
	    		ArrayList<byte[]> encryptedDocuments = server.get_matching_docs();
	    		ArrayList<String> matching_documents = client.decryptDocuments(encryptedDocuments);
	    		

	    		writer_leakage.write(keyword + ",");
	    		for (int add_idx = 0; add_idx < docAddrs.size()-1; add_idx++) {
	    			writer_leakage.write(docAddrs.get(add_idx) + ",");
	    		}
	    		writer_leakage.write(docAddrs.get(docAddrs.size()-1) + "\n");
	    		writer_leakage.flush();
	    		
	    		server.Clear();
	    	}
	    	
	    	System.out.println("Queries done.");
	    	
	    	writer_leakage.close();
    	}
    	
    }
}
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e