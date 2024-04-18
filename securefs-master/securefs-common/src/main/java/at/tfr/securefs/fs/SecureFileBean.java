/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.fs;

import at.tfr.securefs.api.Buffer;
import at.tfr.securefs.api.SecureRemoteFile;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.event.Events;
import at.tfr.securefs.event.SecfsEventType;
import at.tfr.securefs.event.SecureFsFile;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import javax.imageio.IIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful(passivationCapable=false)
@LocalBean
@Remote(SecureRemoteFile.class)
@CacheConfig(idleTimeoutSeconds = 15, removalTimeoutSeconds = 5, maxSize = 10000)
//@RolesAllowed({Role.USER, Role.LOCAL})
@PermitAll
@DependsOn({"CrypterProvider", "EventsBean"})
@Logging
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileBean implements SecureRemoteFile {

    private Logger log = Logger.getLogger(getClass());
    @Resource
    private SessionContext ctx;
    private Events events;
    private transient InputStream in;
    private transient OutputStream out;
    private String pathName;
    private AtomicBoolean closed = new AtomicBoolean();
    private transient Path path;
    
    public SecureFileBean() {
	}
    
    @Inject
    public SecureFileBean(Events events) {
    	this.events = events;
	}

    @PostConstruct
    private void init() {
        log.debug("created ctx=" + ctx);
        events.sendEvent(new SecureFsFile(pathName, false, SecfsEventType.construct));
    }

    @Override
    public int read() throws IOException {
        if (in == null) {
            throw new IIOException("not readable: " + path);
        }
        return in.read();
    }

    @Override
    public Buffer read(final int maxCount) throws IOException {
        if (in == null) {
            throw new IIOException("not readable: " + path);
        }
        byte[] arr = new byte[maxCount];
        int count = 0;
        for (int nextCount = 0; count < maxCount; count += nextCount) {

            try {
                nextCount = in.read(arr, count, maxCount - count);
            } catch (ArrayIndexOutOfBoundsException a) {
                log.warn("iteration failed: path=" + path + ", nextCount=" + nextCount + ", count=" + count
                        + ", maxCount=" + maxCount);
                throw new ArrayIndexOutOfBoundsException(
                        "iteration failed: nextCount=" + nextCount + ", count=" + count + ", maxCount=" + maxCount);
            }

            if (nextCount < 0 && count == 0) {
                return new Buffer(nextCount);
            }

            if (nextCount < 0) {
                break;
            }
        }
        return new Buffer((count == arr.length) ? arr : Arrays.copyOfRange(arr, 0, count), count);
    }

    @Override
    public void write(int b) throws IOException {
        if (out == null) {
            throw new IIOException("not writeable: " + path);
        }
        out.write(b);
    }

    @Override
    public void write(Buffer buffer) throws IOException {
        if (out == null) {
            throw new IIOException("not writeable: " + path);
        }
        out.write(buffer.getData(), 0, buffer.getLength());
        out.flush();
    }

    @Override
    public boolean isOpen() {
        return in != null || out != null;
    }

    @Override
    @Remove
    @PreDestroy
    public void close() {
        if (isOpen()) {
            logInfo("close: " + path, null);
            if (closed.get()) {
                log.warn("closing after close: "+(in != null ? "IN" : out != null ? "OUT" : "UNDEF")+" still open : " + path);
            }
        }
        if (in != null) {
            try {
                in.close();
                log.debug("closed IN: " + path);
            } catch (Throwable t) {
                logInfo("cannot close IN correctly: " + path + " : " + t, t);
            } finally {
                in = null;
            }
        }
        if (out != null) {
            try {
                out.close();
                log.debug("closed OUT: " + path);
            } catch (Throwable t) {
                logInfo("cannot close OUT correctly: " + path + " : " + t, t);
            } finally {
                out = null;
            }
        }
    	boolean isclosed = closed.getAndSet(true);
    	if (!isclosed) {
    		events.sendEvent(new SecureFsFile(pathName, false, SecfsEventType.destroy));
    	}
    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        this.pathName = path.toString();
        log.debug("setPath: " + path + " ctx=" + ctx);
        events.sendEvent(new SecureFsFile(pathName, false, SecfsEventType.init));
    }

    public SecureRemoteFile getRemote() {
        return ctx.getBusinessObject(SecureRemoteFile.class);
    }
    
    @PostLoad
    public void load() {
    	path = Paths.get(pathName);
    }

    private void logInfo(String info, Throwable t) {
    	log.info("User: "+ctx.getCallerPrincipal()+" : "+info + (t!=null ? " : " + t.getMessage() : ""), t);
    }
}
