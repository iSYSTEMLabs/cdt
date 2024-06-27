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

	public ArrayList<String> presetsConfig = new ArrayList<>();
	public ArrayList<String> presetsBuild = new ArrayList<>();

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

	public void processCMakePresets() {
		ArrayList<ArrayList<String>> presets = new ArrayList<>();
		presets.add(presetsConfig);
		presets.add(presetsBuild);
		try {

			int versionIndex = content.indexOf("\"version\"");
			int configurePresetsIndex = content.indexOf("\"configurePresets\"");
			int buildPresetsIndex = content.indexOf("\"buildPresets\"");

			String configurePresets = extractArray(content, configurePresetsIndex);
			String buildPresets = extractArray(content, buildPresetsIndex);
			String[] temp = new String[] { configurePresets, buildPresets };
			for (int i = 0; i < temp.length; i++) {
				String[] lines = temp[i].split("}");

				for (String line : lines) {
					if (line.contains("\"name\":")) {

						String name = line.trim().split(":")[1].trim().replaceAll("\"", "").split(",")[0];
						presets.get(i).add(name);

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractArray(String json, int index) {
		int start = json.indexOf('[', index);
		int end = findEndOfArray(json, start);
		return json.substring(start, end + 1);
	}

	private int findEndOfArray(String json, int startIndex) {
		int count = 1;
		int index = startIndex + 1;

		while (index < json.length() && count > 0) {
			char currentChar = json.charAt(index);
			if (currentChar == '[') {
				count++;
			} else if (currentChar == ']') {
				count--;
			}
			index++;
		}

		return index - 1;
	}

}