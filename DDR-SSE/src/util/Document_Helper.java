package util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Client.entity.KV;

public class Document_Helper {
	public HashMap<String, Integer> keyword_frequency = new HashMap<String, Integer>();
	public HashMap<String, Integer> keyword_frequency_real = new HashMap<String, Integer>();
	private Integer PADDED_LENGTH = 4096;
	private ArrayList<Integer> permutation = new ArrayList<Integer>();
	
	public Document_Helper() {}
	
	public void set_padding_length(Integer length) {
		this.PADDED_LENGTH = length;
	}
	
	public KV[] readInvertedIndex(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader br = new BufferedReader(fileReader);
		
		ArrayList<KV> kv_list = new ArrayList<KV>();
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] kvs = line.split(",");
			
			//kvs[kvs.length-1] = kvs[kvs.length-1].split("\n")[0];
			
			String keyword = kvs[0];
			for (int ii = 1; ii < kvs.length; ii++) {
				KV key_value_pair = new KV(keyword, kvs[ii], ii-1);
				kv_list.add(key_value_pair);
			}
		}
		
		KV[] kv_arr = new KV[kv_list.size()];
		for (int ii = 0; ii < kv_arr.length; ii++)
			kv_arr[ii] = kv_list.get(ii);
		
		br.close();
		fileReader.close();
		
		return kv_arr;
	}
	
	
	public KV[] readInvertedIndexWithBucketization(String filename, Integer N_docs, Integer bucket_size) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader br = new BufferedReader(fileReader);
		
		HashMap<String, ArrayList<Integer>> inverted_index = new HashMap<String, ArrayList<Integer>>();
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] kvs = line.split(",");
			
			String keyword = kvs[0];
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int ii = 1; ii < kvs.length; ii++) {
				values.add(this.permutation.get(Integer.parseInt(kvs[ii])));
			}
			
			inverted_index.put(keyword, values);
		}
		br.close();
		fileReader.close();
		
		System.out.println("Index read.");
		
		// bucketization
		ArrayList<String> keys = new ArrayList<String>(inverted_index.keySet());
		Collections.sort(keys, (a1,a2)->inverted_index.get(a2).size() - inverted_index.get(a1).size());

		Random rand = new Random();
		for (int ii = 0; ii < keys.size(); ii++) {
			String targetKeyword = keys.get((int) Math.floor(ii / bucket_size) * bucket_size);
			Integer targetSize = inverted_index.get(targetKeyword).size();
			
			this.keyword_frequency_real.put(keys.get(ii), inverted_index.get(keys.get(ii)).size());
			
			while (inverted_index.get(keys.get(ii)).size() < targetSize) {
				Integer newDocId = rand.nextInt(N_docs);
				while (inverted_index.get(keys.get(ii)).contains(newDocId)) {
					newDocId = rand.nextInt(N_docs);
				}
				inverted_index.get(keys.get(ii)).add(newDocId);
			}
			
			this.keyword_frequency.put(keys.get(ii), targetSize);
			
			if ((ii+1) % 1000 == 0) {
				System.out.println("Bucketized keyword " + (ii+1));
			}
		}
		
		System.out.println("Bucketization complete.");
		
		ArrayList<KV> kv_list = new ArrayList<KV>();
		for (String keyword: inverted_index.keySet()) {
			Integer counter = 0;
			for (Integer docId: inverted_index.get(keyword)) {
				KV key_value_pair = new KV(keyword, String.valueOf(docId), counter);
				kv_list.add(key_value_pair);
				counter += 1;
			}
		}
		
		KV[] kv_arr = new KV[kv_list.size()];
		for (int ii = 0; ii < kv_arr.length; ii++)
			kv_arr[ii] = kv_list.get(ii);
		
		System.out.println("Index flattened.");
		
		return kv_arr;
	}

	
	public ArrayList<byte[]> readDocuments(String filename) throws IOException {
		ArrayList<byte[]> documents_tmp = new ArrayList<byte[]>();
		
		Integer doc_counter = 0;
		
	    Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path, StandardCharsets.US_ASCII);
		
		br.readLine();
		
		String document = "";
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.equals("NEW_FILE")) {
				if (document.length() == 0) {
					document = " ";
				}
				byte[] document_compressed = GZIPCompression.compress(document);
				
				Integer padding_len = document_compressed.length % PADDED_LENGTH;
				padding_len = PADDED_LENGTH - padding_len;
				if (padding_len == 0) {
					padding_len = PADDED_LENGTH;
				}
								
				byte[] padding = new byte[padding_len];
				padding[0] = 1;
					
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				outputStream.write(document_compressed);
				outputStream.write(padding);
					
				documents_tmp.add(outputStream.toByteArray());
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
		
		Integer padding_len = document_compressed.length % PADDED_LENGTH;
		padding_len = PADDED_LENGTH - padding_len;
		if (padding_len == 0) {
			padding_len = PADDED_LENGTH;
		}
						
		byte[] padding = new byte[padding_len];
		padding[0] = 1;
			
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(document_compressed);
		outputStream.write(padding);
			
		documents_tmp.add(outputStream.toByteArray());
		
		if (doc_counter % 20000 == 0) {
			System.out.println("Reading document " + (doc_counter));
		}
		
		br.close();
		
		// shuffle documents
		for (Integer ii = 0; ii < documents_tmp.size(); ii++) {
			this.permutation.add(ii);
		}
		Collections.shuffle(permutation);


		ArrayList<byte[]> documents = new ArrayList<byte[]>();
		for (Integer ii = 0; ii < documents_tmp.size(); ii++) {
			documents.add(documents_tmp.get(ii));
		}
		
		System.out.println("Documents loaded.");
		
		
		
		return documents;
	}
	
	public HashMap<String, byte[]> getEDocs(ArrayList<byte[]> documents, byte[] docAddrKey1, byte[] docAddrKey2, byte[] docEncKey) throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
		HashMap<String, byte[]> EDocs = new HashMap<String, byte[]>();
		
		for (int idx = 0; idx < documents.size(); idx++) {
			byte[] addr = Hash.Get_HMAC_SHA_256(docAddrKey1, BigInteger.valueOf(idx).toByteArray());
			byte[] edoc = AESUtil.encrypt(docEncKey, documents.get(idx));
			
			EDocs.put(new String(Base64.getEncoder().encode(addr)), edoc);
		}
		
		for (int idx = 0; idx < documents.size(); idx++) {
			byte[] addr = Hash.Get_HMAC_SHA_256(docAddrKey2, BigInteger.valueOf(idx).toByteArray());
			byte[] edoc = AESUtil.encrypt(docEncKey, documents.get(idx));
			
			EDocs.put(new String(Base64.getEncoder().encode(addr)), edoc);
		}
		
		return EDocs;
	}
	
	public static String decryptAndDecodeDocument(byte[] edoc, byte[] docEncKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, IOException {
		byte[] padded_doc = AESUtil.decrypt(docEncKey, edoc);
		
		if (padded_doc == null) {
			throw new InvalidKeyException();
		}
		
		int stop_idx = padded_doc.length - 1;
		
		while (padded_doc[stop_idx] == 0) {
			stop_idx -= 1;
 		}
		
		byte[] compressed_doc = Arrays.copyOfRange(padded_doc, 0, stop_idx);
		String doc = GZIPCompression.decompress(compressed_doc);
		return doc;
	}
	
	public static ArrayList<String> loadQueries(String path, String[] args, Integer N_queries, Integer percentile) throws IOException {
		FileReader fileReader;
		if (args.length == 1)
			fileReader = new FileReader(path + "queries_" + args[0] + "_4096_" + N_queries + "_" + percentile + ".txt");
		else
			fileReader = new FileReader(path + "queries_" + args[0] + "_" + args[1] + "_" + N_queries + "_" + percentile + ".txt");
		BufferedReader br = new BufferedReader(fileReader);
		
		ArrayList<String> queries = new ArrayList<String>();
		
		String query = "";
		while ((query = br.readLine()) != null) {
			queries.add(query);
		}
		
		br.close();
		fileReader.close();
			
		return queries;
	}
}
