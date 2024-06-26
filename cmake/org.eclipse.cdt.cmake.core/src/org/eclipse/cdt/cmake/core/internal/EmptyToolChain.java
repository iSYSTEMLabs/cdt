package org.eclipse.cdt.cmake.core.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.Platform;

public class EmptyToolChain implements IToolChain {

	public static final String TYPE_ID = "org.eclipse.cdt.build.cmake"; //$NON-NLS-1$

	private final Map<String, String> properties = new HashMap<>();
	private IToolChainProvider provider;

	public EmptyToolChain(IToolChainProvider provider) {
		this.provider = provider;

	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToolChainProvider getProvider() {

		return provider;
	}

	@Override
	public String getId() {
		return TYPE_ID;
	}

	@Override
	public String getVersion() {
		return "1.0"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return "CMakeToolChain"; //$NON-NLS-1$
	}

	@Override
	public String getProperty(String key) {

		switch (key) {
		case ATTR_OS:
			return Platform.getOS();
		case ATTR_ARCH:

			return Platform.getOSArch();

		}

		return null;
	}

	@Override
	public void setProperty(String key, String value) {

		properties.put(key, value);
	}

	@Override
	public IEnvironmentVariable[] getVariables() {

		return null;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {

		return null;
	}

	@Override
	public String[] getErrorParserIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getBinaryParserIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getCommandPath(Path command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getCompileCommands() {
		// TODO Auto-generated method stub
		return null;
	}

}
