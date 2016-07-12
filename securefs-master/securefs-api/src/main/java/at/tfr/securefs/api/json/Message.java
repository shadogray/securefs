/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.json;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message implements Serializable {

	public enum MessageType {
		OPEN, CLOSE, ERROR, DATA
	}
	
	public enum MessageSubType {
		READ, WRITE, OK, ACK, ERROR
	}

	private MessageType type;
	private MessageSubType subType;
	private String path;
	private String uniqueKey;
	private byte[] bytes;

	public Message() {
	}

	public Message(MessageType type, String path, byte[] bytes, int length) {
		this(type, path, Arrays.copyOf(bytes, length));
	}
	
	public Message(MessageType type, String path, ByteBuffer fromBuffer) {
		this(type, path, fromBuffer.array(), fromBuffer.limit());
	}

	public Message(MessageType type, String path, byte[] bytes) {
		this.type = type;
		this.path = path;
		this.bytes = bytes;
	}

	public Message(MessageType type, MessageSubType subType, String path) {
		this.type = type;
		this.subType = subType;
		this.path = path;
	}

	public Message(MessageType type, String path) {
		this.type = type;
		this.path = path;
	}

	public Message reply(MessageSubType subType, ByteBuffer buffer) {
		Message copy = reply(subType);
		copy.bytes = Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
		return copy;
	}

	public Message reply(MessageSubType subType) {
		Message copy = copy();
		copy.subType = subType;
		return copy;
	}
	
	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public MessageSubType getSubType() {
		return subType;
	}
	
	public void setSubType(MessageSubType subType) {
		this.subType = subType;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	
	public Message key(String uniqueKey) {
		this.uniqueKey = uniqueKey;
		return this;
	}

	@Override
	public String toString() {
		return "Message [type=" + type + ", path=" + path + ", uniqueKey=" + uniqueKey + "]";
	}

	public Message copy() {
		Message copy = new Message();
		copy.type = type;
		copy.subType = subType;
		copy.path = path;
		copy.uniqueKey = uniqueKey;
		return copy;
	}
}
