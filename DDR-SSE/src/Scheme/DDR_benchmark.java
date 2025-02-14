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
    	
    	String model = Files.lines(Paths.get("/proc/cpuinfo"))
    			   .filter(line -> line.startsWith("model name"))
    			   .map(line -> line.replaceAll(".*: ", ""))
    			   .findFirst().orElse("");
    	System.out.println(model);
    	
    	Integer bucket_size = 200;

    	String padding_len = "4096";
    	if (args.length == 2)
    		padding_len = args[1];
    	
    	PrintWriter writer_benchmark 	= new PrintWriter("benchmark_DDR_SSE_" + padding_len + ".txt", "UTF-8");
    	
    	Client client = new Client();
    	
    	// load inverted index and documents
    	if (args.length == 1)
    		client.loadDatabase(args[0], bucket_size);
    	if (args.length == 2)
    		client.loadDatabase(args[0], args[1], bucket_size);
    	
    	// client setup
    	client.setup();
    	System.out.println("Index and documents encrypted.");
    	
    	// server setup
    	Server server = new Server(client.xor_EMM, client.EDocs, client.XOR_LEVEL, client.STORAGE_XOR);
    	System.out.println("Server setup.");
    	
    	
    	writer_benchmark.write(client.EDocs.size() + "\n");
    	writer_benchmark.write(client.getKVList().length + "\n");
    	writer_benchmark.write(client.setup_time_index + "\n");
    	writer_benchmark.write(client.setup_time_documents + "\n");
    	
    	// make queries
    	ArrayList<String> queries = new ArrayList<String>(client.keyword_frequency.keySet());
    	Integer N_queries = 5000;
    	Collections.shuffle(queries);

    	System.out.println("Queries started.");
    	
    	long startTime = 0;
    	long time1 = 0;
    	long time2 = 0;
    	
    	for (Integer ii = 0; ii < N_queries; ii++) {
    		if (ii % 100 == 0) {
    			System.out.println("Query Progress: " + ii);
    		}
    		
    		String keyword = queries.get(ii);
    		
        	startTime = System.nanoTime();
    		
    		// query the index
    		byte[] tk_key = client.indexQueryGen(keyword);
    		server.Query_Xor(tk_key, client.keyword_frequency.get(keyword));
    		
    		ArrayList<byte[]> c_key = server.Get_C_key();
    		ArrayList<Integer> matching_indices = client.indexResultDecrypt(c_key, keyword);
    		
    		time1 = System.nanoTime() - startTime;
    		startTime = System.nanoTime();

    		// query the documents
    		ArrayList<String> docAddrs = client.documentQueryGen(matching_indices, keyword);
    		server.Query_docs(docAddrs);
    		
    		ArrayList<byte[]> encryptedDocuments = server.get_matching_docs();
			ArrayList<String> results = client.decryptDocuments(encryptedDocuments);		
    		
    		time2 = System.nanoTime() - startTime;
    		
        	writer_benchmark.write(keyword + "," + client.keyword_frequency_real.get(keyword) + "," + (time1+time2) + "," + time1 + "," + time2 + "\n");
        	writer_benchmark.flush();
    		
    		server.Clear();
    		if (ii % 20 == 0)
    			System.gc();
    	}
    	
    	System.out.println("Queries done.");
    	
    	
    	writer_benchmark.close();
    	
    }
}
