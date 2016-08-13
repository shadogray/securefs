/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class ModuleStatistics implements Serializable {

	private AtomicInteger calls = new AtomicInteger();
	private AtomicInteger successes = new AtomicInteger();
	private AtomicInteger failures = new AtomicInteger();
	private AtomicInteger errors = new AtomicInteger();
	private String name = getClass().getSimpleName();

	public ModuleStatistics() {
	}

	public ModuleStatistics(String name) {
		this.name = name;
	}
	
	public AtomicInteger getCalls() {
		return calls;
	}

	public ModuleStatistics setCalls(int calls) {
		this.calls.set(calls);
		return this;
	}

	public AtomicInteger getSuccesses() {
		return successes;
	}

	public ModuleStatistics setSuccesses(int successes) {
		this.successes.set(successes);
		return this;
	}

	public AtomicInteger getFailures() {
		return failures;
	}

	public ModuleStatistics setFailures(int failures) {
		this.failures.set(failures);
		return this;
	}

	public AtomicInteger getErrors() {
		return errors;
	}

	public ModuleStatistics setErrors(int errors) {
		this.errors.set(errors);
		return this;
	}

	@Override
	public String toString() {
		return name + " [calls=" + calls + ", successes=" + successes + ", failures=" + failures + ", errors="
				+ errors + "]";
	}
}
