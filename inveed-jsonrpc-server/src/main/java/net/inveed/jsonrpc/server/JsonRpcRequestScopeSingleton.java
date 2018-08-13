package net.inveed.jsonrpc.server;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import net.inveed.jsonrpc.core.annotation.JsonRpcRequestScope;

@Singleton
public class JsonRpcRequestScopeSingleton implements Context<JsonRpcRequestScope> {

	public static final class Instance {

        private final Map<ActiveDescriptor<?>, Object> store;

        private final AtomicInteger referenceCounter;

        private Instance() {
            this.store = new HashMap<ActiveDescriptor<?>, Object>();
            this.referenceCounter = new AtomicInteger(1);
        }

        private Instance getReference() {
            // TODO: replace counter with a phantom reference + reference queue-based solution
            referenceCounter.incrementAndGet();
            return this;
        }

        @SuppressWarnings("unchecked")
        <T> T get(ActiveDescriptor<T> descriptor) {
            return (T) store.get(descriptor);
        }

        @SuppressWarnings("unchecked")
        <T> T put(ActiveDescriptor<T> descriptor, T value) {
            if(store.containsKey(descriptor)) {
            	return null;
            }
            return (T) store.put(descriptor, value);
        }

        @SuppressWarnings("unchecked")
        <T> void remove(ActiveDescriptor<T> descriptor) {
            final T removed = (T) store.remove(descriptor);
            if (removed != null) {
                descriptor.dispose(removed);
            }
        }

        private <T> boolean contains(ActiveDescriptor<T> provider) {
            return store.containsKey(provider);
        }

        public void release() {
            if (referenceCounter.decrementAndGet() < 1) {
                try {
                    for (final ActiveDescriptor<?> descriptor : new ArrayList<>(store.keySet())) {
                        remove(descriptor);
                    }
                } finally {
                    //logger.debugLog("Released scope instance {0}", this);
                }
            }
        }
    }
	
	
	private final ThreadLocal<Instance> currentScopeInstance = new ThreadLocal<Instance>();
	private volatile boolean isActive = true;
	
	@Override
	public Class<? extends Annotation> getScope() {
		return JsonRpcRequestScope.class;
	}
    
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {

        final Instance instance = current();

        U retVal = instance.get(activeDescriptor);
        if (retVal == null) {
            retVal = activeDescriptor.create(root);
            instance.put(activeDescriptor, retVal);
        }
        return retVal;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        final Instance instance = current();
        return instance.contains(descriptor);
    }

    @Override
    public boolean supportsNullCreation() {
        return true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        final Instance instance = current();
        instance.remove(descriptor);
    }

    private void setCurrent(Instance instance) {
        checkState(isActive, "Request scope has been already shut down.");
        currentScopeInstance.set(instance);
    }

    private void resumeCurrent(Instance instance) {
        currentScopeInstance.set(instance);
    }
    
    @Override
    public void shutdown() {
        isActive = false;
    }
    
    public void runInScope(Runnable task) {
        final Instance oldInstance = retrieveCurrent();
        final Instance instance = createInstance();
        try {
            setCurrent(instance);
            task.run();
        } finally {
            instance.release();
            resumeCurrent(oldInstance);
        }
    }

    /**
     * Request scope injection binder.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(new JsonRpcRequestScopeSingleton()).to(JsonRpcRequestScopeSingleton.class);
        }
    }

    public Instance referenceCurrent() throws IllegalStateException {
        return current().getReference();
    }

    private Instance current() {
        checkState(isActive, "Request scope has been already shut down.");

        final Instance scopeInstance = currentScopeInstance.get();
        checkState(scopeInstance != null, "Not inside a request scope.");

        return scopeInstance;
    }

    private static void checkState(boolean bool, String string) {
		if (!bool) {
			throw new IllegalStateException(string);
		}
	}

	private Instance retrieveCurrent() {
        checkState(isActive, "Request scope has been already shut down.");
        return currentScopeInstance.get();
	}
	
    public Instance suspendCurrent() {
        final Instance scopeInstance = retrieveCurrent();
        if (scopeInstance == null) {
            return null;
        }
        try {
            return scopeInstance.getReference();
        } finally {
            //logger.debugLog("Returned a new reference of the request scope instance {0}", scopeInstance);
        }
    }

    public Instance createInstance() {
        return new Instance();
    }
}
