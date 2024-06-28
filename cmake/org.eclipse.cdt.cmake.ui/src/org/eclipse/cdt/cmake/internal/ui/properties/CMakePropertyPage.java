/*******************************************************************************
 * Copyright (c) 2016 IAR Systems AB
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IAR Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.internal.ui.properties;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.internal.CMakeToolChainProvider;
import org.eclipse.cdt.cmake.core.internal.EmptyToolChain;
import org.eclipse.cdt.cmake.ui.internal.Activator;
import org.eclipse.cdt.cmake.ui.internal.CMakeAutoPresetsReader;
import org.eclipse.cdt.cmake.ui.internal.CMakePropertyCombo;
import org.eclipse.cdt.cmake.ui.internal.CMakePropertyText;
import org.eclipse.cdt.cmake.ui.internal.ICMakePropertyPageControl;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Property page for CMake projects. The only thing we have here at the moment is a button
 * to launch the CMake GUI configurator (cmake-qt-gui).
 *
 * We assume that the build directory is in project/build/configname, which is where
 * the CMake project wizard puts it. We also assume that "cmake-gui" is in the user's
 * PATH.
 */
public class CMakePropertyPage extends PropertyPage implements IWorkbenchPreferencePage {

	private Text cmakePathText;
	private Text ninjaPathText;
	private Button withPresetsCheckbox;
	private Combo presetsComboCfg;
	private Combo presetsComboBld;
	private Text configCommandText;
	private Text buildCommandText;
	private ArrayList<String> presetsConfig;
	private ArrayList<String> presetsBuild;

	@Override
	protected Control createContents(Composite parent) {

		IProject project = getElement().getAdapter(IProject.class);
		if (project != null) {
			try {
				readCMakePresetsFile(project);

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		createCMakeSettingsGroup(composite);
		createPresetsGroup(composite);

		loadData();

		return composite;

	}

	private String readCMakePresetsFile(IProject project) throws CoreException {
		IFile file = project.getFile("CMakePresets.json"); //$NON-NLS-1$
		if (file.exists()) {
			try {
				CMakeAutoPresetsReader ar = new CMakeAutoPresetsReader(file);
				ar.readContents();
				ar.processCMakePresets();
				presetsConfig = ar.presetsConfig;
				presetsBuild = ar.presetsBuild;

			} catch (IOException | CoreException e) {

				e.printStackTrace();
			}

		}
		return null;
	}

	private void loadData() {

		IProject project = getElement().getAdapter(IProject.class);
		if (project != null) {

			IPreferenceStore preferenceStore = Activator.getPlugin().getPreferenceStore();

			cmakePathText.setText(preferenceStore.getString("cmakePath")); //$NON-NLS-1$
			ninjaPathText.setText(preferenceStore.getString("ninjaPath")); //$NON-NLS-1$
			presetsComboCfg.setText(preferenceStore.getString("selectedPreset")); //$NON-NLS-1$
			configCommandText.setText(preferenceStore.getString("configCommand")); //$NON-NLS-1$
			presetsComboBld.setText(preferenceStore.getString("selectedPresetBld")); //$NON-NLS-1$
			buildCommandText.setText(preferenceStore.getString("buildCommand")); //$NON-NLS-1$
			withPresetsCheckbox.setSelection(Boolean.parseBoolean(preferenceStore.getString("withPresets"))); //$NON-NLS-1$

			handleWithPresetsSelection();
		}

	}

	private void createCMakeSettingsGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("CMake Settings"); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label cmakePathLabel = new Label(group, SWT.NONE);
		cmakePathLabel.setText("CMake Path:"); //$NON-NLS-1$

		cmakePathText = new Text(group, SWT.BORDER);
		cmakePathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label ninjaPathLabel = new Label(group, SWT.NONE);
		ninjaPathLabel.setText("Ninja Path:"); //$NON-NLS-1$

		ninjaPathText = new Text(group, SWT.BORDER);
		ninjaPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createPresetsGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Presets"); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		withPresetsCheckbox = new Button(group, SWT.CHECK);
		withPresetsCheckbox.setText("With Presets"); //$NON-NLS-1$
		withPresetsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleWithPresetsSelection();
			}
		});

		new Label(group, SWT.NONE);
		Label label = new Label(group, SWT.NONE);
		label.setText("config preset:"); //$NON-NLS-1$

		presetsComboCfg = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		presetsComboCfg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		for (String preset : this.presetsConfig) {
			presetsComboCfg.add(preset);
		}

		Label labelBld = new Label(group, SWT.NONE);
		labelBld.setText("build preset:"); //$NON-NLS-1$

		presetsComboBld = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		presetsComboBld.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		for (String preset : this.presetsBuild) {
			presetsComboBld.add(preset);
		}

		Label configCommandLabel = new Label(group, SWT.NONE);
		configCommandLabel.setText("Config Command:"); //$NON-NLS-1$

		configCommandText = new Text(group, SWT.BORDER);
		configCommandText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label buildCommandLabel = new Label(group, SWT.NONE);
		buildCommandLabel.setText("Build Command:"); //$NON-NLS-1$

		buildCommandText = new Text(group, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Initially, set the combo box and text box visibility based on the checkbox state
		handleWithPresetsSelection();
	}

	private void handleWithPresetsSelection() {
		boolean withPresets = withPresetsCheckbox.getSelection();
		presetsComboCfg.setEnabled(withPresets);
		configCommandText.setEnabled(!withPresets);
		presetsComboBld.setEnabled(withPresets);
		buildCommandText.setEnabled(!withPresets);
	}

	@Override
	protected void performApply() {
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		savePreferences();
		IProject project = (IProject) getElement();
		ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

		IBuildConfiguration config = null;

		try {
			if (!project.hasBuildConfig("org.eclipse.cdt.cmake.core.provider/cmake.")) { //$NON-NLS-1$
				config = configManager.createBuildConfiguration(
						configManager.getProvider("org.eclipse.cdt.cmake.core.provider"), project, "cmake.", //$NON-NLS-1$ //$NON-NLS-2$
						new NullProgressMonitor());
				CMakeToolChainProvider provider = new CMakeToolChainProvider();
				EmptyToolChain tc = new EmptyToolChain(provider);
				CMakeBuildConfiguration cmakeConfig = new CMakeBuildConfiguration(config, tc);

				configManager.addBuildConfiguration(config, cmakeConfig);

			}

			IBuildConfiguration Config = project.getBuildConfig("org.eclipse.cdt.cmake.core.provider/cmake."); //$NON-NLS-1$
			ICBuildConfiguration cmakeConfig = configManager.getBuildConfiguration(Config);
			IPreferenceStore preferenceStore = Activator.getPlugin().getPreferenceStore();
			((CMakeBuildConfiguration) cmakeConfig).withPreset = Boolean
					.parseBoolean(preferenceStore.getString("withPresets")); //$NON-NLS-1$
			((CMakeBuildConfiguration) cmakeConfig).setPreset(preferenceStore.getString("selectedPreset"), //$NON-NLS-1$
					preferenceStore.getString("selectedPresetBld")); //$NON-NLS-1$

			((CMakeBuildConfiguration) cmakeConfig).ninja = preferenceStore.getString("ninjaPath"); //$NON-NLS-1$
			((CMakeBuildConfiguration) cmakeConfig).cmake = preferenceStore.getString("cmakePath"); //$NON-NLS-1$
			((CMakeBuildConfiguration) cmakeConfig).configCommand = preferenceStore.getString("configCommand"); //$NON-NLS-1$
			((CMakeBuildConfiguration) cmakeConfig).buildCommand = preferenceStore.getString("buildCommand"); //$NON-NLS-1$

			IProjectDescription projectDescription = project.getDescription();
			projectDescription.setActiveBuildConfig("org.eclipse.cdt.cmake.core.provider/cmake."); //$NON-NLS-1$
			project.setDescription(projectDescription, new NullProgressMonitor());

			project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());

		} catch (CoreException e) {

			e.printStackTrace();
		}

		return true;
	}

	private void savePreferences() {

		String cmakePath = cmakePathText.getText();
		String ninjaPath = ninjaPathText.getText();
		boolean withPresets = withPresetsCheckbox.getSelection();
		String selectedPreset = presetsComboCfg.getText();
		String selectedPresetBld = presetsComboBld.getText();
		String configCommand = configCommandText.getText();
		String buildCommand = buildCommandText.getText();

		// Save project-specific preferences
		IProject project = getElement().getAdapter(IProject.class);
		if (project != null) {
			// Use a preference store to save and load preferences
			ScopedPreferenceStore preferenceStore = (ScopedPreferenceStore) Activator.getPlugin().getPreferenceStore();

			preferenceStore.setValue("cmakePath", cmakePath); //$NON-NLS-1$
			preferenceStore.setValue("ninjaPath", ninjaPath); //$NON-NLS-1$
			preferenceStore.setValue("selectedPreset", selectedPreset); //$NON-NLS-1$
			preferenceStore.setValue("selectedPresetBld", selectedPresetBld); //$NON-NLS-1$
			preferenceStore.setValue("configCommand", configCommand); //$NON-NLS-1$
			preferenceStore.setValue("buildCommand", buildCommand); //$NON-NLS-1$
			preferenceStore.setValue("withPresets", String.valueOf(withPresets)); //$NON-NLS-1$

			try {
				preferenceStore.save();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		//test paths
		if (testPath(cmakePath, "cmake") && testPath(ninjaPath, "ninja")) { //$NON-NLS-1$ //$NON-NLS-2$
			MessageDialog.openInformation(getShell(), "Preferences Saved", "Preferences saved successfully!"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	private boolean testPath(String Path, String Type) {
		ProcessBuilder processBuilder = new ProcessBuilder(Path, "-h"); //$NON-NLS-1$
		processBuilder.redirectErrorStream(true);

		try {
			Process process = processBuilder.start();
			InputStream inputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			StringBuilder output = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n"); //$NON-NLS-1$
			}

			int exitCode = process.waitFor();
			if (exitCode == 127) {
				MessageDialog.openError(getShell(), Type + " Path Test", //$NON-NLS-1$
						Type + " command not found:\n" + output.toString()); //$NON-NLS-1$
				return false;
			} else {
				return true;
			}
		} catch (IOException | InterruptedException e) {
			MessageDialog.openError(getShell(), "Error", "Error testing " + Type + "  path:\n" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}

	}

	public enum ParseState {
		INIT, SEENCOMMENT
	}

	/**
	 * Parse output of cmake -LAH call to determine options to show to user
	 * @param stdout - ByteArrayOutputStream containing output of command
	 * @param composite - Composite to add Controls to
	 * @return - list of Controls
	 */
	List<ICMakePropertyPageControl> parseConfigureOutput(ByteArrayOutputStream stdout, Composite composite) {
		List<ICMakePropertyPageControl> controls = new ArrayList<>();

		try {
			ParseState state = ParseState.INIT;

			String output = stdout.toString(StandardCharsets.UTF_8.name());
			String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$
			Pattern commentPattern = Pattern.compile("//(.*)"); //$NON-NLS-1$
			Pattern argPattern = Pattern.compile("(\\w+):([a-zA-Z]+)=(.*)"); //$NON-NLS-1$
			Pattern optionPattern = Pattern.compile(".*?options are:((\\s+\\w+(\\(.*\\))?)+).*"); //$NON-NLS-1$

			String lastComment = ""; //$NON-NLS-1$
			for (String line : lines) {
				line = line.trim();
				switch (state) {
				case INIT:
					Matcher commentMatcher = commentPattern.matcher(line);
					if (commentMatcher.matches()) {
						state = ParseState.SEENCOMMENT;

						lastComment = commentMatcher.group(1);
					}
					break;
				case SEENCOMMENT:
					Matcher argMatcher = argPattern.matcher(line);
					if (argMatcher.matches()) {
						String name = argMatcher.group(1);
						String type = argMatcher.group(2);
						String initialValue = argMatcher.group(3);
						Matcher optionMatcher = optionPattern.matcher(lastComment);
						if (optionMatcher.matches()) {
							String optionString = optionMatcher.group(1).trim();
							String[] options = optionString.split("\\s+"); //$NON-NLS-1$
							for (int i = 0; i < options.length; ++i) {
								options[i] = options[i].replaceAll("\\(.*?\\)", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
							}
							ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name, options,
									initialValue, lastComment);
							controls.add(control);
						} else {
							if ("BOOL".equals(type)) { //$NON-NLS-1$
								if ("ON".equals(initialValue) || ("OFF".equals(initialValue))) { //$NON-NLS-1$ //$NON-NLS-2$
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "ON", "OFF" }, //$NON-NLS-1$ //$NON-NLS-2$
											initialValue, lastComment);
									controls.add(control);
								} else if ("YES".equals(initialValue) || "NO".equals(initialValue)) { //$NON-NLS-1$ //$NON-NLS-2$
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "YES", "NO" }, //$NON-NLS-1$ //$NON-NLS-2$
											initialValue, lastComment);
									controls.add(control);
								} else {
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "TRUE", "FALSE" }, //$NON-NLS-1$ //$NON-NLS-2$
											"TRUE".equals(initialValue) ? "TRUE" : "FALSE", lastComment); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									controls.add(control);
								}
							} else {
								ICMakePropertyPageControl control = new CMakePropertyText(composite, name, initialValue,
										lastComment);
								controls.add(control);
							}
						}
					}
					state = ParseState.INIT;
					break;
				}
			}

		} catch (UnsupportedEncodingException e) {
			return controls;
		}

		return controls;
	}

	@Override
	public void init(IWorkbench workbench) {
		// Load project-specific preferences
		IProject project = getElement().getAdapter(IProject.class);
		if (project != null) {

		}
	}

}