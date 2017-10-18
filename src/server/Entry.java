package server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

/**
 * Informação sobre servidor.
 */
public class Entry {
	/**
	 * key do servidor
	 */
	private String key;
	/**
	 * Dicionario com informação sobre o servidor.
	 */
	private Map<String, Object> attributes;

	public Entry() {
	}

	public Entry(String key, Map<String, Object> attributes) {
		this.key = key;
		this.attributes = attributes;
	}

	public void setkey(String key) {
		this.key = key;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getkey() {
		return key;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public String toString() {
		return String.format("%s : %s", key, attributes);
	}

//	/**
//	 * Devolve identificador gerado deterministicamente a partir do key.
//	 */
//	public String generateId() {
//		try {
//			byte[] hash = MessageDigest.getInstance("MD5").digest(key.getBytes());
//			return DatatypeConverter.printHexBinary(hash);
//		} catch (NoSuchAlgorithmException e) {
//			return "" + (key.hashCode() >>> 1);
//		}
//
//	}
}
