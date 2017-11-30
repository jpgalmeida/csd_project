package resources;

import java.util.Map;

/**
 * Informao sobre servidor.
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
	 * Dicionario com informao sobre o servidor.
	 */
	private Map<String, String> attributes;

	public Entry() {
	}

	public Entry(String key, Map<String, String> attributes) {
		this.key = key;
		this.attributes = attributes;
	}

	public void setkey(String key) {
		this.key = key;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getkey() {
		return key;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String toString() {
		return String.format("%s : %s", key, attributes);
	}

}
