package Server;

import util.GGM;
import util.tool;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {
	private HashMap<String, byte[]> EMetadata;
    private byte[][] EMM;
    private static int MAX_VOLUME_LENGTH = 1024;
    private static int server_level;
    private static int server_DEFAULT_INITIAL_CAPACITY;
    private ArrayList<byte[]> C_key = new ArrayList<byte[]>();
    
    private HashMap<String, byte[]> EDocs = new HashMap<String, byte[]>();
    private ArrayList<byte[]> matching_docs = new ArrayList<byte[]>();
    
    public Server(){}


    public Server(HashMap<String, byte[]> EMetadata, byte[][] EMM, HashMap<String, byte[]> EDocs, int level, int DEFAULT_INITIAL_CAPACITY){
    	this.EMetadata = EMetadata;
        this.EMM = EMM;
        this.EDocs = EDocs;
        //MAX_VOLUME_LENGTH = volume_length;
        server_level = level;
        server_DEFAULT_INITIAL_CAPACITY = DEFAULT_INITIAL_CAPACITY;
    }
    
    public void  Query_Xor(byte[] hash){
        for (int i = 0;i<MAX_VOLUME_LENGTH;i++ ) {
                byte[] father_Node = GGM.Tri_GGM_Path(hash, server_level, tool.TtS(i, 3, server_level));
                int t0 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 1 , 9),server_DEFAULT_INITIAL_CAPACITY,0);
                int t1 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 11, 19),server_DEFAULT_INITIAL_CAPACITY,1);
                int t2 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 21, 29),server_DEFAULT_INITIAL_CAPACITY,2);
                byte[] res = tool.Xor(tool.Xor(EMM[t0], EMM[t1]), EMM[t2]);
                C_key.add(res);
            }

    }
    

    public void  Query_Xor(byte[] hash, Integer query_len){
        for (int i = 0;i<query_len;i++ ) {
                byte[] father_Node = GGM.Tri_GGM_Path(hash, server_level, tool.TtS(i, 3, server_level));
                int t0 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 1 , 9),server_DEFAULT_INITIAL_CAPACITY,0);
                int t1 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 11, 19),server_DEFAULT_INITIAL_CAPACITY,1);
                int t2 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 21, 29),server_DEFAULT_INITIAL_CAPACITY,2);
                byte[] res = tool.Xor(tool.Xor(EMM[t0], EMM[t1]), EMM[t2]);
                C_key.add(res);
            }
    }
    
    public void  Query_Xor(byte[] hash, String EMetadataAddr, byte[] EMatadataMask){
    	byte[] EMetadataEntry = EMetadata.get(EMetadataAddr);
    	byte[] query_len_bytes = new byte[4];
    	for (int ii = 0; ii < 4; ii++)
    		query_len_bytes[ii] = (byte) (EMetadataEntry[ii+4] ^ EMatadataMask[ii]);
    	
    	int query_len = ByteBuffer.wrap(query_len_bytes).getInt();
    	
        for (int i = 0;i<query_len;i++ ) {
                byte[] father_Node = GGM.Tri_GGM_Path(hash, server_level, tool.TtS(i, 3, server_level));
                int t0 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 1 , 9),server_DEFAULT_INITIAL_CAPACITY,0);
                int t1 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 11, 19),server_DEFAULT_INITIAL_CAPACITY,1);
                int t2 = GGM.Map2Range(Arrays.copyOfRange(father_Node, 21, 29),server_DEFAULT_INITIAL_CAPACITY,2);
                byte[] res = tool.Xor(tool.Xor(EMM[t0], EMM[t1]), EMM[t2]);
                C_key.add(res);
            }
    }
    
    public void Query_docs(ArrayList<String> docAddrs) {
    	for (String docAddr: docAddrs) {
    		this.matching_docs.add(this.EDocs.get(docAddr));
    	}
    }
    
   
    public ArrayList<byte[]> Get_C_key(){ return this.C_key; }
    
    public ArrayList<byte[]> get_matching_docs() { return this.matching_docs; }
    
    public void Clear() { 
    	this.C_key.clear();
    	this.matching_docs.clear();
    }

    public void Store_Server(String text) {
        try {
            FileOutputStream file = new FileOutputStream("Server_"+text+".dat");
            for (int i = 0; i < this.EMM.length; i++) {
                file.write(this.EMM[i]);
            }
            file.close();
        } catch (IOException e) {
            System.out.println("Error - " + e.toString());
        }
    }


	public byte[] Query_EMetadata(String eMetadataAddr) {
		return EMetadata.get(eMetadataAddr);
	}



}
