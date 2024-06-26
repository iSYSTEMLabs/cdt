/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	private static Activator plugin;
	public static boolean fileChange;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		Activator.plugin = this;
		bundleContext.registerService(ICMakeToolChainManager.class, new CMakeToolChainManager(), null);
		new CMakeChangeDetector();
		Activator.fileChange = true;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		Activator.plugin = null;
	}

	public static Activator getPlugin() {
		return plugin;
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(errorStatus(e.getLocalizedMessage(), e));
		}
	}

	public static void error(String message, Throwable cause) {
		plugin.getLog().log(errorStatus(message, cause));
	}

	public static IStatus errorStatus(String message, Throwable cause) {
		return new Status(IStatus.ERROR, getId(), message, cause);
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static IPreferencesService getPreferencesService() {
		return Platform.getPreferencesService();
	}

}
