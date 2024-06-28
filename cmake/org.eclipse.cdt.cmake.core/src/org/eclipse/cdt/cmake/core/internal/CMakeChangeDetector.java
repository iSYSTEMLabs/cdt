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

			try {
				delta.accept(deltaVisitor);
			} catch (CoreException e) {

				e.printStackTrace();
			}
		}
	}

	IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
		@Override
		public boolean visit(IResourceDelta delta) {
			if (delta.getResource().getType() == IResource.FILE) {
				String fileName = delta.getResource().getName();
				if ("CMakeLists.txt".equals(fileName) || "CMakePresets.json".equals(fileName)) { //$NON-NLS-1$//$NON-NLS-2$

					if ((delta.getKind() & IResourceDelta.CHANGED) != 0) {

						IProject project = delta.getResource().getProject();

						Activator.fileChange = true;

						deleteCMakeCacheFiles(project);
					}
				}
			}
			return true;
		}
	};

	public void dispose() {

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void deleteCMakeCacheFiles(IContainer container) {
		Job deleteJob = new Job("Delete CMakeCache Files") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					for (org.eclipse.core.resources.IResource resource : container.members()) {

						if (resource instanceof IFile && resource.getName().equals("CMakeCache.txt")) { //$NON-NLS-1$
							IFile file = (IFile) resource;

							file.delete(true, monitor);
						} else if (resource instanceof IContainer) {

							deleteCMakeCacheFiles((IContainer) resource);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "YourPluginID", "Error deleting files", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return Status.OK_STATUS;
			}

		};

		deleteJob.schedule();
	}
}