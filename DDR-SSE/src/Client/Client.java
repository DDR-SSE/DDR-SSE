package Client;

import Client.entity.KV;
import util.AESUtil;
import util.Document_Helper;

import util.Hash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;


public class Client {

    private KV[] kv_list;
    private ArrayList<byte[]> documents;
    private int beta;
    public int XOR_LEVEL;
    public int ELEMENT_SIZE;
    public int STORAGE_XOR;
    
    private byte[] metadataKey;
    public HashMap<String, byte[]> EMetadata = new HashMap<String, byte[]>();
    
	private long indexKey_d;
	private int indexKey_e;
	
	private byte[] docAddrKey1;
	private byte[] docAddrKey2;
	private byte[] docEncKey;
	private long docQuerySeed;
	
	public byte[][] xor_EMM;
	public HashMap<String, Integer> keyword_frequency = new HashMap<String, Integer>();
	public HashMap<String, Integer> keyword_frequency_real = new HashMap<String, Integer>();
	public HashMap<String, byte[]> EDocs;
	
	private int midpoint;
	private int side;
	
	public long setup_time_metadata = 0;
	public long setup_time_index = 0;
	public long setup_time_documents = 0;
	
    
    public Client() {}
    
    
    
    public void loadDatabase(String N_docs, Integer bucket_size) throws IOException {
    	// load documents
    	Document_Helper document_helper = new Document_Helper();
    	this.documents = document_helper.readDocuments("../emails_parsed/emails_" + N_docs + "_4096"  + ".txt");
    	
    	// load inverted index
    	this.kv_list = document_helper.readInvertedIndexWithBucketization("../emails_parsed/inveted_index_" + N_docs + "_4096" + ".txt", this.documents.size(), bucket_size);
    	this.keyword_frequency = document_helper.keyword_frequency;
    	this.keyword_frequency_real = document_helper.keyword_frequency_real;
    	
    	//maximum volume length
        int MAX_VOLUME_LENGTH = 0;
        for (KV key_value_pair : kv_list) {
        	if (key_value_pair.counter+1 > MAX_VOLUME_LENGTH) {
        		MAX_VOLUME_LENGTH = key_value_pair.counter+1;
        	}
        }
        this.XOR_LEVEL = (int) Math.ceil(Math.log(MAX_VOLUME_LENGTH) / Math.log(3.0));//GGM Tree level for xor hash

        //data size
        int power_size = (int) Math.ceil(Math.log(kv_list.length) / Math.log(2));
        this.ELEMENT_SIZE = (int) Math.pow(2, power_size);

        //storage size
        this.beta = 0;//parameter for xor hash
        this.STORAGE_XOR = (int) Math.floor(((ELEMENT_SIZE * 1.23) + beta) / 3);

    }
    
    
    public void loadDatabase(String N_docs, String padding_length, Integer bucket_size) throws IOException {
    	// load documents
    	Document_Helper document_helper = new Document_Helper();
    	document_helper.set_padding_length(Integer.parseInt(padding_length));
    	this.documents = document_helper.readDocuments("../emails_parsed/emails_" + N_docs + "_" + padding_length + ".txt");
    	
    	// load inverted index
    	this.kv_list = document_helper.readInvertedIndexWithBucketization("../emails_parsed/inveted_index_" + N_docs + "_" + padding_length + ".txt", this.documents.size(), bucket_size);
    	this.keyword_frequency = document_helper.keyword_frequency;
    	this.keyword_frequency_real = document_helper.keyword_frequency_real;
    	
    	//maximum volume length
        int MAX_VOLUME_LENGTH = 0;
        for (KV key_value_pair : kv_list) {
        	if (key_value_pair.counter+1 > MAX_VOLUME_LENGTH) {
        		MAX_VOLUME_LENGTH = key_value_pair.counter+1;
        	}
        }
        this.XOR_LEVEL = (int) Math.ceil(Math.log(MAX_VOLUME_LENGTH) / Math.log(3.0));//GGM Tree level for xor hash

        //data size
        int power_size = (int) Math.ceil(Math.log(kv_list.length) / Math.log(2));
        this.ELEMENT_SIZE = (int) Math.pow(2, power_size);

        //storage size
        this.beta = 0;//parameter for xor hash
        this.STORAGE_XOR = (int) Math.floor(((ELEMENT_SIZE * 1.23) + beta) / 3);
        
        
    }
    
    
    public void setup() throws Exception {
    	// metadata
    	long startTime = System.nanoTime();
    	
    	KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        
    	this.metadataKey = keyGen.generateKey().getEncoded();
    	
    	for (String keyword: this.keyword_frequency.keySet()) {
    		String addr = metadataQueryGenAddr(keyword);
    		byte[] mask1 = metadataQueryGenMask1(keyword);
    		byte[] mask2 = metadataQueryGenMask2(keyword);
    		
    		ByteBuffer buffer = ByteBuffer.allocate(8);
    		buffer.putInt(this.keyword_frequency_real.get(keyword));
            buffer.putInt(this.keyword_frequency.get(keyword));
            byte[] payload = buffer.array();
            for (int ii = 0; ii < 4; ii++) 
            {
            	payload[ii]   = (byte) (payload[ii] ^ mask1[ii]);
            	payload[ii+4] = (byte) (payload[ii+4] ^ mask2[ii]);
            }
            
            this.EMetadata.put(addr, payload);
    	}
    	
    	this.setup_time_metadata = System.nanoTime() - startTime;
    	
    	// index
    	startTime = System.nanoTime();
    	
        Xor_Hash xor = new Xor_Hash(this.beta);
        xor.XorMM_setup(kv_list, ELEMENT_SIZE, XOR_LEVEL);
        
        this.setup_time_index = System.nanoTime() - startTime;
        
        
        this.indexKey_d = xor.Get_K_d();
        this.indexKey_e = xor.Get_K_e();
        this.xor_EMM = xor.Get_EMM();
        
        System.out.println("Index built.");
        
        // documents
        startTime = System.nanoTime();
        
        this.docAddrKey1 = keyGen.generateKey().getEncoded();
        this.docAddrKey2 = keyGen.generateKey().getEncoded();
        this.docEncKey = keyGen.generateKey().getEncoded();
        
        byte[] docQueryKey = keyGen.generateKey().getEncoded();
        this.docQuerySeed = 0;
        for (int ii = 0; ii < 8; ii++) {
        	this.docQuerySeed += ((long) docQueryKey[ii] & 0xffL) << (8 * ii);
        }
        	
        Document_Helper document_helper = new Document_Helper();
        this.EDocs = document_helper.getEDocs(this.documents, this.docAddrKey1, this.docAddrKey2, this.docEncKey);
        
        this.setup_time_documents = System.nanoTime() - startTime;
        
        System.out.println("Documents encrypted.");
    }
    
    public String metadataQueryGenAddr(String keyword) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(metadataKey);
		outputStream.write(keyword.getBytes());
		outputStream.write(0);
		byte[] addr = Hash.Get_SHA_256(outputStream.toByteArray());
    	
		return new String(Base64.getEncoder().encode(addr));
    }
    
    public byte[] metadataQueryGenMask1(String keyword) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    	outputStream.write(metadataKey);
		outputStream.write(keyword.getBytes());
		outputStream.write(1);
		byte[] mask1 = Hash.Get_SHA_256(outputStream.toByteArray());
		return mask1;
    }
    
    public byte[] metadataQueryGenMask2(String keyword) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    	outputStream.write(metadataKey);
		outputStream.write(keyword.getBytes());
		outputStream.write(2);
		byte[] mask2 = Hash.Get_SHA_256(outputStream.toByteArray());
		return mask2;
    }
    
	public int get_real_frequency(byte[] eMetadataEntry, byte[] eMetadataMask1) {
		if (eMetadataEntry == null)
			return 0;
		byte[] result = new byte[4];
		for (int ii = 0; ii < 4; ii++)
			result[ii] = (byte) (eMetadataEntry[ii] ^ eMetadataMask1[ii]);
		return ByteBuffer.wrap(result).getInt();
	}
    
    
    public byte[] indexQueryGen(String keyword) {
    	//search token
    	 byte[] tk_key = Hash.Get_SHA_256((keyword + this.indexKey_d).getBytes(StandardCharsets.UTF_8));
    	 
    	 return tk_key;
    }
    
    public ArrayList<Integer> indexResultDecrypt(ArrayList<byte[]> C_key, String keyword) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
    	HashSet<Integer> results = new HashSet<Integer>();
    	
    	byte[] K = Hash.Get_Sha_128((this.indexKey_e+keyword).getBytes(StandardCharsets.UTF_8));
    	
    	for (int i = 0; i < C_key.size(); i++)
        {
            byte[] str_0 = AESUtil.decrypt(K,C_key.get(i));
            if(str_0!=null){
                String s = new String(str_0);
                try {
                	results.add(Integer.parseInt(s));
                }
                catch (Error e) {
                	
                }
            }
        }
    	
    	return new ArrayList<Integer>(results);
    }
    
    public ArrayList<String> documentQueryGen(ArrayList<Integer> documentIds, String keyword) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
    	ArrayList<String> docAddrs = new ArrayList<String>();
    	
    	// fix randomness
    	Random rand = new Random();
    	
    	long seed = this.docQuerySeed;
    	byte[] keyword_bytes = keyword.getBytes();
    	for (byte keyword_byte: keyword_bytes)
    		seed *= (long) keyword_byte & 0xffL;
    	rand.setSeed(seed);
    	
    	this.midpoint = rand.nextInt(documentIds.size()+1);
    	this.side = rand.nextInt(2);
    	
    	for (int ii = 0; ii < documentIds.size(); ii++) {
    		if (ii < midpoint && side == 0) {
    			byte[] addr = Hash.Get_HMAC_SHA_256(this.docAddrKey1, BigInteger.valueOf(documentIds.get(ii)).toByteArray());
    			docAddrs.add(new String(Base64.getEncoder().encode(addr)));
    		}
    		
    		else if (ii < midpoint && side == 1) {
    			byte[] addr = Hash.Get_HMAC_SHA_256(this.docAddrKey2, BigInteger.valueOf(documentIds.get(ii)).toByteArray());
    			docAddrs.add(new String(Base64.getEncoder().encode(addr)));
    		}
    		
    		else if (ii >= midpoint && side == 0) {
    			byte[] addr = Hash.Get_HMAC_SHA_256(this.docAddrKey2, BigInteger.valueOf(documentIds.get(ii)).toByteArray());
    			docAddrs.add(new String(Base64.getEncoder().encode(addr)));
    		}
    		
    		else if (ii >= midpoint && side == 1) {
    			byte[] addr = Hash.Get_HMAC_SHA_256(this.docAddrKey1, BigInteger.valueOf(documentIds.get(ii)).toByteArray());
    			docAddrs.add(new String(Base64.getEncoder().encode(addr)));
    		}
    		
    	}
    	
    	return docAddrs;
    }
    
    public ArrayList<String> decryptDocuments(ArrayList<byte[]> encryptedDocuments) {
    	ArrayList<String> documents = new ArrayList<String>();
    	
    	for (byte[] encryptedDocument: encryptedDocuments) {
    		try {
    			String document = Document_Helper.decryptAndDecodeDocument(encryptedDocument, this.docEncKey);
        		documents.add(document);
    		}
    		catch (Error | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | IOException e) {
    			System.err.println("Error");
    		}
    		
    	}
    	return documents;
    } 
    
    public ArrayList<String> decryptDocuments(ArrayList<byte[]> encryptedDocuments, int frequency_real) {
    	ArrayList<String> documents = new ArrayList<String>();
    	
    	for (int ii = 0; ii < frequency_real; ii++) {
    		byte[] encryptedDocument = encryptedDocuments.get(ii);
    		try {
    			String document = Document_Helper.decryptAndDecodeDocument(encryptedDocument, this.docEncKey);
        		documents.add(document);
    		}
    		catch (Error | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | IOException e) {
    			System.err.println("Error");
    		}
    		
    	}
    	return documents;
	}
    
    public KV[] getKVList() {
    	return this.kv_list;
    }



	




}
