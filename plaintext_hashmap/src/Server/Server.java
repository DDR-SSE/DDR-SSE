<<<<<<< HEAD
package Server;

import java.io.IOException;
import java.util.*;

import util.GZIPCompression;

public class Server {
	public HashMap<Integer, byte[]> documents;
	public HashMap<String, ArrayList<Integer>> inverted_index;
    
    public Server(){}


    public Server(HashMap<String, ArrayList<Integer>> inverted_index, HashMap<Integer, byte[]> documents){
    	this.documents = documents;
    	this.inverted_index = inverted_index;
    }
    
    
    public void query(String keyword) throws IOException {
    	ArrayList<Integer> ids = inverted_index.get(keyword);
    	for (Integer doc_id: ids)
    		GZIPCompression.decompress(documents.get(doc_id));
    }

}
=======
package Server;

import java.io.IOException;
import java.util.*;

import util.GZIPCompression;

public class Server {
	public HashMap<Integer, byte[]> documents;
	public HashMap<String, ArrayList<Integer>> inverted_index;
    
    public Server(){}


    public Server(HashMap<String, ArrayList<Integer>> inverted_index, HashMap<Integer, byte[]> documents){
    	this.documents = documents;
    	this.inverted_index = inverted_index;
    }
    
    
    public void query(String keyword) throws IOException {
    	ArrayList<Integer> ids = inverted_index.get(keyword);
    	for (Integer doc_id: ids)
    		GZIPCompression.decompress(documents.get(doc_id));
    }

}
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e
