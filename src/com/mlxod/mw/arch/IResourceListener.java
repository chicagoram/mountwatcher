package com.mlxod.mw.arch;

public interface IResourceListener {

	/**
	 * Something has been added to this resource, or the resource itself has
	 * been added.
	 * 
	 * @param newResource
	 *            the new resource
	 */
	public void onAdd(Object newResource) throws Exception;

	/**
	 * Monitoring has just started on this new resource.
	 * 
	 * @param monitoredResource
	 *            the resource now being monitored.
	 */
	public void onStart(Object monitoredResource) throws Exception;

	/**
	 * Monitoring has just ended on this new resource.
	 * 
	 * @param notMonitoredResource
	 *            the resource not being monitored anymore.
	 */
	public void onStop(Object notMonitoredResource) throws Exception;

}
