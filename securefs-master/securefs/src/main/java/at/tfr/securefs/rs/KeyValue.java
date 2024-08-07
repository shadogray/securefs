package at.tfr.securefs.rs;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class KeyValue {
	private String key, value;

	public KeyValue() {
	}

	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}