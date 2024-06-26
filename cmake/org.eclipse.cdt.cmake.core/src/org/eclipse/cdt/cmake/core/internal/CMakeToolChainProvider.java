package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.cdt.core.build.IToolChainProvider;

public class CMakeToolChainProvider implements IToolChainProvider {

	public static final String PROVIDER_ID = "org.eclipse.cdt.cmake.provider"; //$NON-NLS-1$

	@Override
	public String getId() {

		return PROVIDER_ID;
	}

}
