<<<<<<< HEAD
package Scheme;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;
import util.GZIPCompression;

public class plaintext_benchmark {


    public static void main(String[] args) throws Exception {
    	//String model = Files.lines(Paths.get("/proc/cpuinfo"))
    	//		   .filter(line -> line.startsWith("model name"))
    	//		   .map(line -> line.replaceAll(".*: ", ""))
    	//		   .findFirst().orElse("");
    	//System.out.println(model);


    	PrintWriter writer_benchmark 	= new PrintWriter("benchmark_plaintext.txt", "UTF-8");
    	
    	Client client = new Client();
    	
    	
    	// load inverted index and documents
    	client.loadDatabase();
    	System.out.println("Index and documents loaded.");
    	
    	// server setup
    	Server server = new Server(client.inverted_index, client.documents);
    	System.out.println("Server setup.");
    	
    	// make queries
    	ArrayList<String> queries = new ArrayList<String>(client.inverted_index.keySet());
    	Integer N_queries = 5000;
    	Collections.shuffle(queries);

    	System.out.println("Queries started.");
    	
    	long startTime = 0;
    	long timeTaken = 0;
    	
    	for (Integer ii = 0; ii < N_queries; ii++) {
    		if (ii % 100 == 0) {
    			System.out.println("Query Progress: " + ii);
    		}
    		
    		String keyword = queries.get(ii);

    		startTime = System.nanoTime();
    		server.query(keyword);
    		//client.decodeDocs(response);
    		
    		//System.out.println(keyword + "," + client.inverted_index.get(keyword).size() + "," + response.size());

    		timeTaken = System.nanoTime() - startTime;
    		
        	writer_benchmark.write(keyword + "," + client.inverted_index.get(keyword).size() + "," + timeTaken + "\n");
        	writer_benchmark.flush();
        	System.gc();
    	}
    	
    	System.out.println("Queries done.");
    	
    	
    	writer_benchmark.close();
    	
    }
}
=======
package Scheme;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import Client.Client;
import Server.Server;
import util.GZIPCompression;

public class plaintext_benchmark {


    public static void main(String[] args) throws Exception {
    	//String model = Files.lines(Paths.get("/proc/cpuinfo"))
    	//		   .filter(line -> line.startsWith("model name"))
    	//		   .map(line -> line.replaceAll(".*: ", ""))
    	//		   .findFirst().orElse("");
    	//System.out.println(model);


    	PrintWriter writer_benchmark 	= new PrintWriter("benchmark_plaintext.txt", "UTF-8");
    	
    	Client client = new Client();
    	
    	
    	// load inverted index and documents
    	client.loadDatabase();
    	System.out.println("Index and documents loaded.");
    	
    	// server setup
    	Server server = new Server(client.inverted_index, client.documents);
    	System.out.println("Server setup.");
    	
    	// make queries
    	ArrayList<String> queries = new ArrayList<String>(client.inverted_index.keySet());
    	Integer N_queries = 5000;
    	Collections.shuffle(queries);

    	System.out.println("Queries started.");
    	
    	long startTime = 0;
    	long timeTaken = 0;
    	
    	for (Integer ii = 0; ii < N_queries; ii++) {
    		if (ii % 100 == 0) {
    			System.out.println("Query Progress: " + ii);
    		}
    		
    		String keyword = queries.get(ii);

    		startTime = System.nanoTime();
    		server.query(keyword);
    		//client.decodeDocs(response);
    		
    		//System.out.println(keyword + "," + client.inverted_index.get(keyword).size() + "," + response.size());

    		timeTaken = System.nanoTime() - startTime;
    		
        	writer_benchmark.write(keyword + "," + client.inverted_index.get(keyword).size() + "," + timeTaken + "\n");
        	writer_benchmark.flush();
        	System.gc();
    	}
    	
    	System.out.println("Queries done.");
    	
    	
    	writer_benchmark.close();
    	
    }
}
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e
