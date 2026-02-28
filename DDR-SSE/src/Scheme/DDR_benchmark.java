package Scheme;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;

public class DDR_benchmark {


    public static void main(String[] args) throws Exception {
    	
    	//String model = Files.lines(Paths.get("/proc/cpuinfo"))
    	//		   .filter(line -> line.startsWith("model name"))
    	//		   .map(line -> line.replaceAll(".*: ", ""))
    	//		   .findFirst().orElse("");
    	//System.out.println(model);
    	
    	Integer bucket_size = 400;

    	String padding_len = "4096";
    	if (args.length == 3)
    		padding_len = args[1];
    	
    	PrintWriter writer_benchmark 	= new PrintWriter("benchmark_DDR_SSE_" + padding_len + ".txt", "UTF-8");
    	
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
    	System.gc();
    	
    	
    	writer_benchmark.write("#Docs: " + client.EDocs.size() + "\n");
    	writer_benchmark.write("#KWs: " + client.EMetadata.size() + "\n");
    	writer_benchmark.write("#KDPs: " + client.getKVList().length + "\n");
    	writer_benchmark.write("Setup time (metadata): " + client.setup_time_metadata + "\n");
    	writer_benchmark.write("Setup time (index): " + client.setup_time_index + "\n");
    	writer_benchmark.write("Setup time (documents): " + client.setup_time_documents + "\n");
    	
    	// make queries
    	ArrayList<String> queries = new ArrayList<String>(client.keyword_frequency.keySet());
    	Integer N_queries = 5000;
    	Collections.shuffle(queries);

    	System.out.println("Queries started.");
    	
    	long startTime = 0;
    	long time1 = 0;
    	long time2 = 0;
    	long time3 = 0;
    	
    	for (Integer ii = 0; ii < N_queries; ii++) {
    		if (ii % 100 == 0) {
    			System.out.println("Query Progress: " + ii + "/" + N_queries);
    			System.out.flush();
    		}
    		
    		String keyword = queries.get(ii);
    		
        	startTime = System.nanoTime();
    		
    		// query the index
        	String EMetadataAddr = client.metadataQueryGenAddr(keyword);
        	byte[] EMetadataMask1 = client.metadataQueryGenMask1(keyword);
        	byte[] EMetadataMask2 = client.metadataQueryGenMask2(keyword);
        	
        	byte[] EMetadataEntry = server.Query_EMetadata(EMetadataAddr);
        	int frequency_real = client.get_real_frequency(EMetadataEntry, EMetadataMask1);
        	
        	time1 = System.nanoTime() - startTime;
    		startTime = System.nanoTime();
        	
    		byte[] tk_key = client.indexQueryGen(keyword);
    		server.Query_Xor(tk_key, EMetadataAddr, EMetadataMask2);
    		
    		ArrayList<byte[]> c_key = server.Get_C_key();
    		ArrayList<Integer> matching_indices = client.indexResultDecrypt(c_key, keyword);
    		
    		time2 = System.nanoTime() - startTime;
    		startTime = System.nanoTime();

    		// query the documents
    		ArrayList<String> docAddrs = client.documentQueryGen(matching_indices, keyword);
    		server.Query_docs(docAddrs);
    		
    		ArrayList<byte[]> encryptedDocuments = server.get_matching_docs();
			ArrayList<String> results = client.decryptDocuments(encryptedDocuments, frequency_real);		
    		
    		time3 = System.nanoTime() - startTime;
    		
        	writer_benchmark.write(keyword + "," + client.keyword_frequency_real.get(keyword) + "," + (time1+time2+time3) + "," + time1 + "," + time2 + "," + time3 + "\n");
        	writer_benchmark.flush();
    		
        	EMetadataAddr = null;
        	EMetadataMask1 = null;
        	EMetadataMask2 = null;
        	EMetadataEntry = null;
        	tk_key = null;
        	c_key = null;
        	matching_indices = null;
        	docAddrs = null;
        	results = null;
        	
    		server.Clear();
    		if (ii % 20 == 0)
    			System.gc();
    	}
    	
    	System.out.println("Queries done.");
    	
    	
    	writer_benchmark.close();
    	
    }
}
