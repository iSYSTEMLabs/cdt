/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class LibraryPathTab extends AbstractLangsListTab implements IPathEntryStoreListener {
	IPathEntryStore fStore;
	private static final int[] PRIVATE_SASH_WEIGHTS = new int[] { 0, 30 };

	public void additionalTableSet() {
		  TableColumn c = new TableColumn(table, SWT.NONE);
		  c.setWidth(300);
		  c.setText(UIMessages.getString("LibraryPathTab.0")); //$NON-NLS-1$
		  table.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
                    public void getName(AccessibleEvent e) {
                            e.result = UIMessages.getString("LibraryPathTab.0"); //$NON-NLS-1$
                    }
                }
		  );
	}

	public void createControls(Composite parent) {
		super.createControls(parent);
//		((GridData)langTree.getLayoutData()).widthHint = 0;
  	    sashForm.setWeights(PRIVATE_SASH_WEIGHTS);
		langTree.setVisible(false);
	}
	
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		updateData(getResDesc());
	}

	public ICLanguageSettingEntry doAdd() {
		IncludeDialog dlg = new IncludeDialog(
				usercomp.getShell(), IncludeDialog.NEW_DIR,
				UIMessages.getString("LibraryPathTab.1"),  //$NON-NLS-1$ 
				EMPTY_STR, getResDesc().getConfiguration(), 0);
		if (dlg.open() && dlg.text1.trim().length() > 0 ) {
			toAllCfgs = dlg.check1;
			toAllLang = dlg.check3;
			int flags = 0;
			if (dlg.check2) flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			return new CLibraryPathEntry(dlg.text1, flags);
		}
		return null;
	}

	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		IncludeDialog dlg = new IncludeDialog(
				usercomp.getShell(), IncludeDialog.OLD_DIR,
				UIMessages.getString("LibraryPathTab.2"),  //$NON-NLS-1$ 
				ent.getValue(), getResDesc().getConfiguration(),
				(ent.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH));
		if (dlg.open() && dlg.text1.trim().length() > 0 ) {
			int flags = 0;
			if (dlg.check2) flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			return new CLibraryPathEntry(dlg.text1, flags);
		}
		return null;
	}
	
	public int getKind() { return ICSettingEntry.LIBRARY_PATH; }
}
