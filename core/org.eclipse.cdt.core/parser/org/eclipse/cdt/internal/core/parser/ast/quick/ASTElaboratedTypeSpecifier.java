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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTElaboratedTypeSpecifier implements IASTElaboratedTypeSpecifier
{

	private NamedOffsets offsets = new NamedOffsets();
	private final String typeName;
	private final ASTClassKind classKind;
	private final ASTQualifiedNamedElement qualifiedName;   
    /**
     * @param elaboratedClassKind
     * @param typeName
     * @param startingOffset
     * @param endOffset
     */
    public ASTElaboratedTypeSpecifier(IASTScope scope, ASTClassKind elaboratedClassKind, String typeName, int startingOffset, int nameOffset, int nameEndOffset, int endOffset)
    {
    	classKind = elaboratedClassKind; 
    	this.typeName = typeName;
    	setStartingOffset( startingOffset );
    	setNameOffset( nameOffset );
    	setNameEndOffset( nameEndOffset );
    	setEndingOffset( endOffset );
    	qualifiedName = new ASTQualifiedNamedElement( scope, typeName );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getTypeName()
     */
    public String getName()
    {
        return typeName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getClassKind()
     */
    public ASTClassKind getClassKind()
    {
        return classKind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        offsets.setStartingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
    	offsets.setEndingOffset(o);
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
     */
    public int getStartingOffset()
    {
        return offsets.getStartingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#isResolved()
     */
    public boolean isResolved() throws ASTNotImplementedException
    {
    	throw new ASTNotImplementedException();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.acceptElaboratedForewardDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
	 */
	public int getNameOffset() {
		return offsets.getNameOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		offsets.setNameOffset(o);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset()
	{
		return offsets.getNameEndOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
	 */
	public void setNameEndOffset(int o)
	{
		offsets.setNameEndOffset(o);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind)
	 */
	public LookupResult lookup(String prefix, LookupKind kind) {
		// TODO Auto-generated method stub
		return null;
	}
}
