package com.directory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.mlxod.mw.util.Util;

public abstract class AbstractResourceWatcher extends IntervalThread implements
		IResourceWatcher {
	private Collection<IResourceListener> listeners = Collections
			.synchronizedList(new LinkedList<IResourceListener>());

	public AbstractResourceWatcher(int intervalSeconds, String name) {
		super(intervalSeconds, name);
	}

	public void removeAllListeners() {
		this.listeners.clear();
	}

	@Override
	public void addListener(IResourceListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(IResourceListener listener) {
		this.listeners.remove(listener);
	}

	protected void resourceAdded(Object newResource) throws Exception {
		synchronized (listeners) {
			Iterator<IResourceListener> listIt = this.listeners.iterator();

			while (listIt.hasNext()){
				
				listIt.next().onAdd(newResource);
			}
		}
	}

	protected void monitoringStarted(Object monitoredResource) throws Exception {
		synchronized (listeners) {
			Iterator<IResourceListener> listIt = this.listeners.iterator();

			while (listIt.hasNext())
				listIt.next().onStart(monitoredResource);
		}
	}

	protected void monitoringStopped(Object notMonitoredResource)
			throws Exception {
		synchronized (listeners) {
			Iterator<IResourceListener> listIt = this.listeners.iterator();

			while (listIt.hasNext())
				listIt.next().onStop(notMonitoredResource);
		}
	}

	@Override
	protected abstract void doInterval() throws Exception;
}