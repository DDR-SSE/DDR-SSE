package Scheme;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;
import util.Document_Helper;

public class DDR_leakage_extraction {


    public static void main(String[] args) throws Exception {
    	
    	Integer bucket_size = 200;
    	Integer N_queries = 1200;
    	
    	Client client = new Client();
    	
    	// load inverted index and documents
    	if (args.length == 2) {
    		bucket_size = Integer.parseInt(args[1]);
    		client.loadDatabase(args[0], bucket_size);
    	}
    		
    	if (args.length == 3) {
    		bucket_size = Integer.parseInt(args[2]);
    		client.loadDatabase(args[0], args[1], bucket_size);
    	}
    	
    	
    	// client setup
    	client.setup();
    	System.out.println("Index and documents encrypted.");
    	
    	// server setup
    	Server server = new Server(client.EMetadata, client.xor_EMM, client.EDocs, client.XOR_LEVEL, client.STORAGE_XOR);
    	System.out.println("Server setup.");
    	
    	client.removePlaintextDB();
    	
    	// leakage extraction
    	ArrayList<String> keywords = new ArrayList<String>(client.keyword_frequency.keySet());
    	keywords.sort((a1, a2)-> client.keyword_frequency.get(a2) - client.keyword_frequency.get(a1));
    	Integer[] percentiles = {100, 95, 90, 85, 80, 75};
    	
    	for (Integer percentile : percentiles) {
    		
    		System.gc();
    		
    		PrintWriter writer_leakage = new PrintWriter(String.format("../leakage/leakage_%s_%d_%d_%d.txt", args[0], N_queries, bucket_size, percentile), "UTF-8");
    	
	    	// load queries
    		ArrayList<String> queries = Document_Helper.loadQueries("../emails_parsed/", args, N_queries, percentile);
	    	Collections.shuffle(queries);
	
	    	System.out.println(String.format("Queries on the %d-th percentile started.", percentile));
	    	
	    	for (Integer ii = 0; ii < N_queries; ii++) {
	    		if (ii % 100 == 0) {
	    			System.out.println("Query Progress: " + ii + "/" + N_queries);
	    			System.out.flush();
	    		}
	    		
	    		String keyword = queries.get(ii);
	    		
	    		// query the index
	        	String EMetadataAddr = client.metadataQueryGenAddr(keyword);
	        	//byte[] EMetadataMask1 = client.metadataQueryGenMask1(keyword);
	        	byte[] EMetadataMask2 = client.metadataQueryGenMask2(keyword);
	        	
	        	//byte[] EMetadataEntry = server.Query_EMetadata(EMetadataAddr);
	        	//int frequency_real = client.get_real_frequency(EMetadataEntry, EMetadataMask1);
	        	
	    		byte[] tk_key = client.indexQueryGen(keyword);
	    		server.Query_Xor(tk_key, EMetadataAddr, EMetadataMask2);
	    		
	    		ArrayList<byte[]> c_key = server.Get_C_key();
	    		ArrayList<Integer> matching_indices = client.indexResultDecrypt(c_key, keyword);
	    		
	    		ArrayList<String> docAddrs = client.documentQueryGen(matching_indices, keyword);
	    		//server.Query_docs(docAddrs);
	    		
	    		//ArrayList<byte[]> encryptedDocuments = server.get_matching_docs();
	    		//ArrayList<String> matching_documents = client.decryptDocuments(encryptedDocuments);
	    		

	    		writer_leakage.write(keyword + ",");
	    		for (int add_idx = 0; add_idx < docAddrs.size()-1; add_idx++) {
	    			writer_leakage.write(docAddrs.get(add_idx) + ",");
	    		}
	    		if (docAddrs.size() > 0)
	    			writer_leakage.write(docAddrs.get(docAddrs.size()-1) + "\n");
	    		else
	    			writer_leakage.write("\n");
	    		writer_leakage.flush();
	    		
	    		EMetadataAddr = null;
	    		EMetadataMask2 = null;
	    		tk_key = null;
	    		c_key = null;
	    		matching_indices = null;
	    		docAddrs = null;
	    		
	    		
	    		server.Clear();
	    	}
	    	
	    	System.out.println("Queries done.");
	    	
	    	writer_leakage.close();
    	}
    	
    }
}
