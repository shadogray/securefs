package at.tfr.securefs.beans;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.util.Arrays;

@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Logging
public class LoggingInterceptor {

	private Logger log = Logger.getLogger(getClass());
	
	@AroundInvoke
	public Object aroundInvoke(InvocationContext ctx) throws Throwable {
		if (log.isDebugEnabled()) {
			log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" params: "+Arrays.toString(ctx.getParameters()));
		}
		try {
			Object o = ctx.proceed();
			if (log.isDebugEnabled()) {
				log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" result: "+o);
			}
			return o;
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" exc: "+t);
				log.trace("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" exc: "+t, t);
			}
			throw t;
		}
	}
}
