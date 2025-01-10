<<<<<<< HEAD
package Client;

import util.Document_Helper;
import util.GZIPCompression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Client {
	
	
	public HashMap<Integer, byte[]> documents = new HashMap<Integer, byte[]>();
	public HashMap<String, ArrayList<Integer>> inverted_index;
	
    
    public Client() {}
    
    public void loadDatabase() throws IOException {
    	Document_Helper document_helper = new Document_Helper();
    	
    	// load inverted index
    	this.inverted_index = document_helper.readInvertedIndex("../emails_parsed/inveted_index_400000.txt");
    	
    	// load documents
    	ArrayList<byte[]> document_list = document_helper.readDocuments("../emails_parsed/emails_400000.txt");
    	for (int ii = 0; ii < document_list.size(); ii++)
    		this.documents.put(ii, document_list.get(ii));
    }
    
    public void decodeDocs(ArrayList<byte[]> response) throws IOException {
    	for (byte[] document: response) {
    		GZIPCompression.decompress(document);
    	}
    }
}
=======
package Client;

import util.Document_Helper;
import util.GZIPCompression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Client {
	
	
	public HashMap<Integer, byte[]> documents = new HashMap<Integer, byte[]>();
	public HashMap<String, ArrayList<Integer>> inverted_index;
	
    
    public Client() {}
    
    public void loadDatabase() throws IOException {
    	Document_Helper document_helper = new Document_Helper();
    	
    	// load inverted index
    	this.inverted_index = document_helper.readInvertedIndex("../emails_parsed/inveted_index_400000.txt");
    	
    	// load documents
    	ArrayList<byte[]> document_list = document_helper.readDocuments("../emails_parsed/emails_400000.txt");
    	for (int ii = 0; ii < document_list.size(); ii++)
    		this.documents.put(ii, document_list.get(ii));
    }
    
    public void decodeDocs(ArrayList<byte[]> response) throws IOException {
    	for (byte[] document: response) {
    		GZIPCompression.decompress(document);
    	}
    }
}
>>>>>>> bccd9ba9978cb8cfb4a4ee3cb7a46123bc50738e
