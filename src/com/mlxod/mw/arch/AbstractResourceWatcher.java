package com.mlxod.mw.arch;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.mlxod.mw.arch.IResourceListener;
import com.mlxod.mw.arch.IResourceWatcher;
import com.mlxod.mw.arch.IntervalThread;

public abstract class AbstractResourceWatcher extends IntervalThread implements
		IResourceWatcher {
	private Collection<IResourceListener> listeners = new LinkedList<IResourceListener>();

	public AbstractResourceWatcher(int intervalSeconds, String name) {
		super(intervalSeconds, name);
	}

	@Override
	public void addListener(IResourceListener listener) {
		this.listeners.add(listener);
	}

	public void removeAllListeners() {
		this.listeners.clear();
	}

	@Override
	public void removeListener(IResourceListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	protected abstract void doInterval() throws Exception;

	protected void monitoringStarted(Object monitoredResource) throws Exception {
		Iterator<IResourceListener> listIt = this.listeners.iterator();

		while (listIt.hasNext())
			listIt.next().onStart(monitoredResource);
	}

	protected void monitoringStopped(Object notMonitoredResource)
			throws Exception {
		Iterator<IResourceListener> listIt = this.listeners.iterator();

		while (listIt.hasNext())
			listIt.next().onStop(notMonitoredResource);
	}

	protected void resourceAdded(Object newResource) throws Exception {
		Iterator<IResourceListener> listIt = this.listeners.iterator();

		while (listIt.hasNext())
			listIt.next().onAdd(newResource);
	}
}
