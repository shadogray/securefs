package at.tfr.securefs.beans;

import java.util.Arrays;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

@Interceptor
@Audit
public class AuditInterceptor {

	private Logger log = Logger.getLogger(getClass());
	
	@AroundInvoke
	public Object aroundInvoke(InvocationContext ctx) throws Throwable {
		log.info("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" params: "+Arrays.toString(ctx.getParameters()));
		try {
			Object o = ctx.proceed();
			log.info("done: "+ctx.getTarget()+":"+ctx.getMethod());
			return o;
		} catch (Throwable t) {
			log.info("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" exc: "+t);
			log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" exc: "+t, t);
			throw t;
		}
	}
}
