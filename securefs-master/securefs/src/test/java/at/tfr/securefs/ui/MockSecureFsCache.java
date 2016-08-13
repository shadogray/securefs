package at.tfr.securefs.ui;

import java.io.Serializable;

import at.tfr.securefs.cache.SecureFsCache;

public class MockSecureFsCache implements SecureFsCache {

	@Override
	public <T extends Serializable> T get(String key) {
		return null;
	}

	@Override
	public void put(String key, Serializable value) {
	}

	@Override
	public void remove(String key) {
	}

	@Override
	public String getNodeName() {
		return "null";
	}

}
