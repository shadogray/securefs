package at.tfr.securefs.beans;

import java.security.Principal;
import java.util.Arrays;

import javax.inject.Inject;
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
		Principal principal = null;
		if (log.isDebugEnabled()) {
			log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" by: "+principal+" params: "+getParams(ctx));
		}
		try {
			Object o = ctx.proceed();
			log.info("done: "+ctx.getTarget().getClass().getSimpleName()+":"+ctx.getMethod().getName()+" by: "+principal);
			return o;
		} catch (Throwable t) {
			log.info("invoke: "+ctx.getTarget().getClass().getSimpleName()+":"+ctx.getMethod().getName()+" by: "+principal+" params: "+getParams(ctx)+" exc: "+t);
			if (log.isDebugEnabled()) {
				log.debug("invoke: "+ctx.getTarget()+":"+ctx.getMethod()+" by: "+principal+" params: "+getParams(ctx)+" exc: "+t, t);
			}
			throw t;
		}
	}

	private String getParams(InvocationContext ctx) {
		try {
			return Arrays.toString(ctx.getParameters());
		} catch (Exception e) {
			log.info("cannot get parameters: " + e, e);
		}
		return "";
	}
}
