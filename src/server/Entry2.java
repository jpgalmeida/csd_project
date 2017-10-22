package server;

import java.util.Map;

/**
 * Informa��o sobre servidor.
 */
public class Entry2 {
	/**
	 * key do servidor
	 */
	private String key;
	/**
	 * Dicionario com informa��o sobre o servidor.
	 */
	private Map<String, String> attributes;

	public Entry2() {
	}

	public Entry2(String key, Map<String, String> attributes) {
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
