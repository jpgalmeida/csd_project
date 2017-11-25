package resources;

import java.util.Map;

/**
 * Informação sobre servidor.
 */
public class Entry implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * key do servidor
	 */
	private String key;
	/**
	 * Dicionario com informação sobre o servidor.
	 */
	private Map<String, byte[]> attributes;

	public Entry() {
	}

	public Entry(String key, Map<String, byte[]> attributes) {
		this.key = key;
		this.attributes = attributes;
	}

	public void setkey(String key) {
		this.key = key;
	}

	public void setAttributes(Map<String, byte[]> attributes) {
		this.attributes = attributes;
	}

	public String getkey() {
		return key;
	}

	public Map<String, byte[]> getAttributes() {
		return attributes;
	}

	public String toString() {
		return String.format("%s : %s", key, attributes);
	}

}
