package org.eclipse.cdt.cmake.core.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.cmake.core.CMakeErrorParser;
import org.eclipse.cdt.cmake.core.CMakeExecutionMarkerFactory;
import org.eclipse.cdt.cmake.core.ParsingConsoleOutputStream;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;

@SuppressWarnings("restriction")
public class CMakeBuildConfiguration implements ICBuildConfiguration, IMarkerGenerator {

	static final IEnvironmentVariable[] STRINGS = {};
	List<String> stringList = new ArrayList<>();
	private final IBuildConfiguration config;
	private String configPreset;
	private ICommandLauncher launcher;
	public String cmake;
	public String ninja;
	public boolean withPreset;
	public String configCommand;
	public String buildCommand;
	private IToolChain tc;
	public boolean fileChange;
	private String buildPreset;

	public CMakeBuildConfiguration(IBuildConfiguration config, IToolChain tc) {

		this.tc = tc;
		this.config = config;

	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(ICBuildConfiguration.class)) {

			return adapter.cast(this);
		} else {
			return Platform.getAdapterManager().getAdapter(this, adapter);
		}
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {

		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {

	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {

	}

	public IProject getProject() {
		try {
			return getBuildConfiguration().getProject();
		} catch (CoreException e) {

			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IBuildConfiguration getBuildConfiguration() throws CoreException {

		return config;
	}

	@Override
	public IToolChain getToolChain() throws CoreException {

		return tc;
	}

	@Override
	public List<String> getBinaryParserIds() throws CoreException {
		return stringList;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) throws CoreException {

		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() throws CoreException {

		return null;
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {

		LoadingRequiredVariables();

		monitor.slice(2);

		ConsoleOutputStream consoleStream = console.getOutputStream();
		IEnvironmentVariable[] env = STRINGS;
		org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getProjectDirectory().toString());
		IContainer srcFolder = getProject();

		try {

			if (Activator.fileChange == true) {

				consoleStream.write("Configurating ...\n"); //$NON-NLS-1$
				try (CMakeErrorParser errorParser = new CMakeErrorParser(new CMakeExecutionMarkerFactory(srcFolder))) {
					ParsingConsoleOutputStream errStream = new ParsingConsoleOutputStream(console.getErrorStream(),
							errorParser);
					IConsole errConsole = new CMakeConsoleWrapper(console, errStream);

					List<String> commands = new ArrayList<>();

					commands.add("--preset " + configPreset); //$NON-NLS-1$

					java.lang.Process p;
					if (withPreset) {
						p = startBuildProcess(cmake, commands, env, workingDir, console, monitor);
					} else {
						String[] parts = configCommand.trim().split("\\s+"); //$NON-NLS-1$
						String[] flags = new String[parts.length - 1];
						System.arraycopy(parts, 1, flags, 0, parts.length - 1);
						List<String> flagsArray = Arrays.asList(flags);
						if (parts[0].matches("cmake") == false) { //$NON-NLS-1$
							console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
							Activator.fileChange = true;
							return null;
						}
						p = startBuildProcess(cmake, flagsArray, env, workingDir, console, monitor);
					}

					if (p == null) {
						console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
						Activator.fileChange = true;
						return null;
					}

					Activator.fileChange = false;

					if (watchProcess(errConsole, monitor) != ICommandLauncher.OK) {
						Activator.fileChange = true;
					}

				}
			}

			consoleStream.write("Building ...\n"); //$NON-NLS-1$
			try (ErrorParserManager epm = new ErrorParserManager(getProject(), getBuildDirectoryURI(), this,
					ErrorParserManager.getErrorParserAvailableIds())) {
				epm.setOutputStream(console.getOutputStream());

				List<String> cmake_build = new ArrayList<>();
				cmake_build.add("--build"); //$NON-NLS-1$
				cmake_build.add("--preset=" + buildPreset); //$NON-NLS-1$

				java.lang.Process p2;
				if (withPreset) {

					workingDir = new org.eclipse.core.runtime.Path(getProjectDirectory().toString());
					p2 = startBuildProcess(cmake, cmake_build, env, workingDir, console, monitor);
				} else {
					String[] parts = buildCommand.trim().split("\\s+"); //$NON-NLS-1$
					String[] flags = new String[parts.length - 1];
					System.arraycopy(parts, 1, flags, 0, parts.length - 1);
					List<String> ninja_build = Arrays.asList(flags);
					p2 = startBuildProcess(parts[0], ninja_build, env, workingDir, console, monitor);
				}

				if (p2 == null) {
					Activator.fileChange = true;
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}
				watchProcess(new IConsoleParser[] { epm }, monitor);

			} catch (IOException e) {
				Activator.fileChange = true;
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.CMakeBuildConfiguration_Building, getProject().getName()), e));
			}

		} catch (IOException e) {
			Activator.fileChange = true;
			e.printStackTrace();
		}

		monitor.done();

		return null;
	}

	private void LoadingRequiredVariables() {

		IPreferencesService preferencesService = Activator.getPreferencesService();

		if (configCommand == null) {

			this.withPreset = Boolean.parseBoolean(
					preferencesService.getString("org.eclipse.cdt.cmake.ui", "withPresets", "true", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			setPreset(preferencesService.getString("org.eclipse.cdt.cmake.ui", "selectedPreset", "", null), //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					preferencesService.getString("org.eclipse.cdt.cmake.ui", "selectedPresetBld", "", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			this.ninja = preferencesService.getString("org.eclipse.cdt.cmake.ui", "ninjaPath", "", null); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
			this.cmake = preferencesService.getString("org.eclipse.cdt.cmake.ui", "cmakePath", "", null); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
			this.configCommand = preferencesService.getString("org.eclipse.cdt.cmake.ui", "configCommand", "", null); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
			this.buildCommand = preferencesService.getString("org.eclipse.cdt.cmake.ui", "buildCommand", "", null); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

		}

	}

	protected int watchProcess(IConsole console, IProgressMonitor monitor) throws CoreException {
		assertLauncherNotNull(launcher);
		return launcher.waitAndRead(console.getInfoStream(), console.getErrorStream(), monitor);
	}

	private int watchProcess(IConsoleParser[] consoleParsers, IProgressMonitor monitor) throws CoreException {

		assertLauncherNotNull(launcher);
		ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(consoleParsers);
		return launcher.waitAndRead(sniffer.getOutputStream(), sniffer.getErrorStream(), monitor);
	}

	private void assertLauncherNotNull(ICommandLauncher launcher) {
		Assert.isNotNull(launcher, "Only processes launched with startBuildProcess can be watched."); //$NON-NLS-1$
	}

	private URI getBuildDirectoryURI() {
		return getBuildDirectory().getLocationURI();
	}

	private IFolder getBuildDirectory() {
		IFolder file = null;
		try {
			if (withPreset) {
				IProject project = getProject();
				return project.getFolder("build"); //$NON-NLS-1$
			} else {

				IProject project = getProject();
				String[] path = getBuildPathFromCommand();
				file = project.getFolder(path[0]);

				for (int i = 1; i < path.length; i++) {
					file = file.getFolder(path[i]);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;

	}

	private String[] getBuildPathFromCommand() {
		String flag = "-B"; //$NON-NLS-1$
		int flagIndex = configCommand.indexOf(flag);

		if (flagIndex == -1) {

			return new String[] { "build" }; // -B flag not found //$NON-NLS-1$
		}

		int startIndex = flagIndex + flag.length();
		// Skip any spaces after the -B flag
		while (startIndex < configCommand.length() && configCommand.charAt(startIndex) == ' ') {
			startIndex++;
		}

		if (startIndex >= configCommand.length()) {
			return new String[] { "build" }; // No build directory specified after -B //$NON-NLS-1$
		}

		int endIndex = configCommand.indexOf(' ', startIndex);
		if (endIndex == -1) {
			endIndex = configCommand.length(); // No more spaces, take the rest of the string
		}

		return configCommand.split("/"); //$NON-NLS-1$
	}

	private Path getProjectDirectory() {
		IProject project = getProject();
		return Paths.get(project.getLocationURI());

	}

	private java.lang.Process startBuildProcess(String command, List<String> args, IEnvironmentVariable[] envVars,
			IPath buildDirectory, IConsole console, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);

		String[] commandArray = args.toArray(new String[args.size()]);

		try {
			monitor.subTask("Building " + command); //$NON-NLS-1$
			java.lang.Process result = launcher.execute(new org.eclipse.core.runtime.Path(command), commandArray,
					getEnvironmentVariables(envVars), buildDirectory, monitor);

			if (result != null) {
				return result;
			} else {
				this.fileChange = true;

				throw new CoreException(new Status(IStatus.ERROR, "YourPluginID", //$NON-NLS-1$
						"Error: Illegal command. Check the command line and build environment. ")); //$NON-NLS-1$
			}
		} finally {
			monitor.done();
		}
	}

	private String[] getEnvironmentVariables(IEnvironmentVariable[] envVars) {

		return Arrays.stream(envVars).map(envVar -> envVar.getName() + "=" + envVar.getValue()).toArray(String[]::new); //$NON-NLS-1$

	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {

		IFolder build = this.getBuildDirectory();
		build.delete(true, monitor);
		Activator.fileChange = true;

	}

	public String getName() {

		return "cmake."; //$NON-NLS-1$
	}

	public String getPreset() {

		return configPreset;
	}

	public void setPreset(String configPreset, String buildPreset) {
		this.configPreset = configPreset;
		this.buildPreset = buildPreset;
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar, null));
	}

	@SuppressWarnings("null")
	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		try {
			IProject project = config.getProject();
			IResource markerResource = problemMarkerInfo.file;
			if (markerResource == null) {
				markerResource = project;
			}
			String externalLocation = null;
			if (problemMarkerInfo.externalPath != null && !problemMarkerInfo.externalPath.isEmpty()) {
				externalLocation = problemMarkerInfo.externalPath.toOSString();
			}

			// Try to find matching markers and don't put in duplicates
			IMarker[] markers = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_ONE);
			for (IMarker m : markers) {
				int line = m.getAttribute(IMarker.LINE_NUMBER, -1);
				int sev = m.getAttribute(IMarker.SEVERITY, -1);
				String msg = (String) m.getAttribute(IMarker.MESSAGE);
				if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity)
						&& msg.equals(problemMarkerInfo.description)) {
					String extloc = (String) m.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
					if (extloc == externalLocation || (extloc != null && extloc.equals(externalLocation))) {
						if (project == null || project.equals(markerResource.getProject())) {
							return;
						}
						String source = (String) m.getAttribute(IMarker.SOURCE_ID);
						if (project.getName().equals(source)) {
							return;
						}
					}
				}
			}

			String type = problemMarkerInfo.getType();
			if (type == null) {
				type = ICModelMarker.C_MODEL_PROBLEM_MARKER;
			}

			IMarker marker = markerResource.createMarker(type);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, problemMarkerInfo.startChar);
			marker.setAttribute(IMarker.CHAR_END, problemMarkerInfo.endChar);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (externalLocation != null) {
				URI uri = null;
				try {
					uri = new URI(externalLocation);
				} catch (URISyntaxException e) {

					e.printStackTrace();
				}
				if (uri.getScheme() != null) {
					marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, externalLocation);
					String locationText = String.format(Messages.CMakeBuildConfiguration_ProcCompJson,
							problemMarkerInfo.lineNumber, externalLocation);
					marker.setAttribute(IMarker.LOCATION, locationText);
				}
			} else if (problemMarkerInfo.lineNumber == 0) {
				marker.setAttribute(IMarker.LOCATION, " "); //$NON-NLS-1$
			}
			// Set source attribute only if the marker is being set to a file
			// from different project
			if (project != null && !project.equals(markerResource.getProject())) {
				marker.setAttribute(IMarker.SOURCE_ID, project.getName());
			}

			// Add all other client defined attributes.
			Map<String, String> attributes = problemMarkerInfo.getAttributes();
			if (attributes != null) {
				for (Entry<String, String> entry : attributes.entrySet()) {
					marker.setAttribute(entry.getKey(), entry.getValue());
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	private int mapMarkerSeverity(int severity) {
		switch (severity) {
		case SEVERITY_ERROR_BUILD:
		case SEVERITY_ERROR_RESOURCE:
			return IMarker.SEVERITY_ERROR;
		case SEVERITY_INFO:
			return IMarker.SEVERITY_INFO;
		case SEVERITY_WARNING:
			return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}

}
