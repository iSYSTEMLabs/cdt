/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public interface IASTNode {
	
	 public static class LookupKind extends Enum {

		public static final LookupKind ALL = new LookupKind( 0 );
		public static final LookupKind STRUCTURES = new LookupKind( 1 );
		public static final LookupKind FUNCTIONS = new LookupKind( 2 );
		public static final LookupKind LOCAL_VARIABLES = new LookupKind( 3 );
		public static final LookupKind METHODS = new LookupKind( 4 );
		public static final LookupKind FIELDS = new LookupKind( 5 );
		public static final LookupKind NAMESPACES = new LookupKind( 6 ); 

		/**
		 * @param enumValue
		 */
		protected LookupKind(int enumValue) {
			super(enumValue);
			// TODO Auto-generated constructor stub
		}
	 }
	 
	 public static class InvalidLookupKind extends Exception
	 {
	 }
	 
	 public static interface LookupResult 
	 {
	 	public String getPrefix(); 
	 	public Iterator getNodes(); 
	 	public Iterator getKeywords();  
	 }

	public LookupResult lookup( String prefix, LookupKind kind );
}

