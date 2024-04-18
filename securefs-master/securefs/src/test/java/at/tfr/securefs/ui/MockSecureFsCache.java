package at.tfr.securefs.ui;

import at.tfr.securefs.cache.SecureFsCache;

import java.io.Serializable;

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
