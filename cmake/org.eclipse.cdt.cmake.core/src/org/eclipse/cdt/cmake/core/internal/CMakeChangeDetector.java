package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class CMakeChangeDetector implements IResourceChangeListener {

	public CMakeChangeDetector() {

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			// Traverse the delta and check for changes in CMakeLists.txt and CMakePresets.json
			try {
				delta.accept(deltaVisitor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Implement a visitor to traverse the resource delta
	IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
		@Override
		public boolean visit(IResourceDelta delta) {
			if (delta.getResource().getType() == IResource.FILE) {
				String fileName = delta.getResource().getName();
				if ("CMakeLists.txt".equals(fileName) || "CMakePresets.json".equals(fileName)) {
					// Check if the file has been changed
					if ((delta.getKind() & IResourceDelta.CHANGED) != 0) {
						// File has been changed, you can trigger your CMake build here
						System.out.println("File changed: " + delta.getResource().getFullPath());
						IProject project = delta.getResource().getProject();

						Activator.fileChange = true;

						deleteCMakeCacheFiles(project);
					}
				}
			}
			return true; // Continue visiting
		}
	};

	public void dispose() {
		// Unregister the listener when it's no longer needed
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void deleteCMakeCacheFiles(IContainer container) {
		Job deleteJob = new Job("Delete CMakeCache Files") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Get all members (files and folders) in the container
					for (org.eclipse.core.resources.IResource resource : container.members()) {
						// Check if the resource is a file and its name is "CMakeCache.txt"
						if (resource instanceof IFile && resource.getName().equals("CMakeCache.txt")) {
							IFile file = (IFile) resource;
							// Delete the file
							file.delete(true, monitor);
							System.out.println("Deleted: " + file.getFullPath());
						} else if (resource instanceof IContainer) {
							// If the resource is a container (folder), recursively call the method
							deleteCMakeCacheFiles((IContainer) resource);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "YourPluginID", "Error deleting files", e);
				}
				return Status.OK_STATUS;
			}

		};

		// Schedule the job to run
		deleteJob.schedule();
	}
}