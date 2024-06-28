/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class CMakeBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.cmake.core.provider"; //$NON-NLS-1$

	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public synchronized ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {

		CMakeToolChainProvider provider = new CMakeToolChainProvider();
		EmptyToolChain tc = new EmptyToolChain(provider);
		IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);

		toolChainManager.addToolChain(tc);

		CMakeBuildConfiguration cmakeConfig = new CMakeBuildConfiguration(config, tc);
		IPreferencesService preferencesService = Activator.getPreferencesService();
		cmakeConfig.withPreset = Boolean
				.parseBoolean(preferencesService.getString("org.eclipse.cdt.cmake.ui", "withPresets", "true", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cmakeConfig.setPreset(preferencesService.getString("org.eclipse.cdt.cmake.ui", "selectedPreset", "", null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				preferencesService.getString("org.eclipse.cdt.cmake.ui", "selectedPresetBld", "", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cmakeConfig.ninja = preferencesService.getString("org.eclipse.cdt.cmake.ui", "ninjaPath", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cmakeConfig.cmake = preferencesService.getString("org.eclipse.cdt.cmake.ui", "cmakePath", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cmakeConfig.cmakeCommand = preferencesService.getString("org.eclipse.cdt.cmake.ui", "cmakeCommand", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		configManager.addBuildConfiguration(config, cmakeConfig);
		return cmakeConfig;

	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {

		// create config
		StringBuilder configName = new StringBuilder("cmake."); //$NON-NLS-1$
		configName.append(launchMode);

		String name = configName.toString();
		IBuildConfiguration config = null;
		// reuse any IBuildConfiguration with the same name for the project
		// so adding the CBuildConfiguration will override the old one stored
		// by the CBuildConfigurationManager
		if (configManager.hasConfiguration(this, project, name)) {
			config = project.getBuildConfig(this.getId() + '/' + name);
		}
		if (config == null) {
			config = configManager.createBuildConfiguration(this, project, name, monitor);
		}

		CMakeToolChainProvider provider = new CMakeToolChainProvider();
		EmptyToolChain tc = new EmptyToolChain(provider);
		CMakeBuildConfiguration cmakeConfig = new CMakeBuildConfiguration(config, tc);

		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setActiveBuildConfig("org.eclipse.cdt.cmake.core.provider/cmake."); //$NON-NLS-1$

		project.setDescription(projectDescription, new NullProgressMonitor());
		configManager.addBuildConfiguration(config, cmakeConfig);
		return cmakeConfig;
	}

}
