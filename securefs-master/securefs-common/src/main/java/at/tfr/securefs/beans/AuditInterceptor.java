package at.tfr.securefs.beans;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.security.Principal;
import java.util.Arrays;

@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Audit
public class AuditInterceptor {

	private Logger log = Logger.getLogger(getClass());
	@Inject
	private Instance<Principal> identity;

	@AroundInvoke
	public Object aroundInvoke(InvocationContext ctx) throws Throwable {
		String principal = getPrincipal();
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

	private String getPrincipal() {
		if (identity.isUnsatisfied()) {
			return "undef";
		}
		try {
			return ""+identity.get();
		} catch (ContextNotActiveException e) {
			return "async";
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
