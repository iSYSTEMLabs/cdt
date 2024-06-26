package org.eclipse.cdt.cmake.ui.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class CMakeAutoPresetsReader {

	private IFile file;

	private String content;

	public CMakeAutoPresetsReader(IFile file) {
		this.file = file;
	}

	public void readContents() throws IOException, CoreException {
		StringBuilder contents = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		String line;
		while ((line = reader.readLine()) != null) {
			contents.append(line);
		}
		reader.close();
		content = contents.toString();

	}

	public ArrayList<String> processCMakePresets() {
		ArrayList<String> presets = new ArrayList<>();
		try {

			int versionIndex = content.indexOf("\"version\"");
			int configurePresetsIndex = content.indexOf("\"configurePresets\"");
			int buildPresetsIndex = content.indexOf("\"buildPresets\"");

			String configurePresets = extractArray(content, configurePresetsIndex);
			String buildPresets = extractArray(content, buildPresetsIndex);

			for (String array : new String[] { configurePresets, buildPresets }) {
				String[] lines = array.split("}");

				for (String line : lines) {
					if (line.contains("\"name\":")) {

						String name = line.trim().split(":")[1].trim().replaceAll("\"", "").split(",")[0];
						presets.add(name);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return presets;
	}

	private String extractArray(String json, int index) {
		int start = json.indexOf('[', index);
		int end = json.indexOf(']', start) + 1;
		return json.substring(start, end);
	}

}