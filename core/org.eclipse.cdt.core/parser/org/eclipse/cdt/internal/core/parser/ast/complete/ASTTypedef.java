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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTTypedef extends ASTSymbol implements IASTTypedefDeclaration
{

    private final IASTAbstractDeclaration mapping;
    private NamedOffsets offsets = new NamedOffsets();
    private final ASTQualifiedNamedElement qualifiedName;
    private final ASTReferenceStore referenceStore; 

    /**
     * @param newSymbol
     * @param mapping
     * @param startingOffset
     * @param nameOffset
     * @param references
     */
    public ASTTypedef(ISymbol newSymbol, IASTAbstractDeclaration mapping, int startingOffset, int nameOffset, int nameEndOffset, List references)
    {
        super( newSymbol );
        this.mapping = mapping;
        this.referenceStore = new ASTReferenceStore( references );
        this.qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), newSymbol.getName());
        setStartingOffset(startingOffset);
        setNameOffset(nameOffset);
        setNameEndOffset( nameEndOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration#getName()
     */
    public String getName()
    {
        return getSymbol().getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration#getAbstractDeclarator()
     */
    public IASTAbstractDeclaration getAbstractDeclarator()
    {
        return mapping;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptTypedefDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        referenceStore.processReferences(requestor);
        getAbstractDeclarator().acceptElement( requestor );
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
    public int getNameOffset()
    {
        return offsets.getNameOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        offsets.setNameOffset(o);
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
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return offsets.getStartingOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
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
