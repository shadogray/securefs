/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

public enum Role {
	user, operator, admin, $local;
	
	public static final String USER = "user";
	public static final String OPERATOR = "operator";
	public static final String ADMIN = "admin";
	public static final String LOCAL = "$local";
}
