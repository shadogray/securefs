package at.tfr.securefs.beans;

import java.util.Arrays;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

@Interceptor
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
