/**********************************************************************
 * Copyright (c) 2004 IBM Canada Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class GCCCompleteParseExtensionsTest extends CompleteParseBaseTest {

	/**
	 * 
	 */
	public GCCCompleteParseExtensionsTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public GCCCompleteParseExtensionsTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

    public void testBug39695() throws Exception
    {
        Iterator i = parse("int a = __alignof__ (int);").getDeclarations(); //$NON-NLS-1$
        IASTVariable a = (IASTVariable) i.next();
        assertFalse( i.hasNext() );
        IASTExpression exp = a.getInitializerClause().getAssigmentExpression();
        assertEquals( exp.getExpressionKind(), IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID );
        assertEquals( exp.toString(), "__alignof__(int)"); //$NON-NLS-1$
    }
    
    public void testBug39684() throws Exception
    {
    	IASTFunction bar = (IASTFunction) parse("typeof(foo(1)) bar () { return foo(1); }").getDeclarations().next(); //$NON-NLS-1$
    	
    	IASTSimpleTypeSpecifier simpleTypeSpec = ((IASTSimpleTypeSpecifier)bar.getReturnType().getTypeSpecifier());
		assertEquals( simpleTypeSpec.getType(), IASTGCCSimpleTypeSpecifier.Type.TYPEOF );
    }

    public void testBug39698A() throws Exception
    {
        Iterator i = parse("int c = a <? b;").getDeclarations(); //$NON-NLS-1$
        IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a <? b" ); //$NON-NLS-1$
    }
    public void testBug39698B() throws Exception
    {
    	Iterator i = parse("int c = a >? b;").getDeclarations(); //$NON-NLS-1$
    	IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a >? b" ); //$NON-NLS-1$
    }

	public void testPredefinedSymbol_bug69791() throws Exception {
		Iterator i = 			parse("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();//$NON-NLS-1$
		IASTTypedefDeclaration td = (IASTTypedefDeclaration) i.next();
		assertFalse(i.hasNext());
	}

	public void testBug39697() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "__asm__( \"CODE\" );\n" ); //$NON-NLS-1$
		writer.write( "__inline__ int foo() { return 4; }\n"); //$NON-NLS-1$
		writer.write( "__const__ int constInt;\n"); //$NON-NLS-1$
		writer.write( "__volatile__ int volInt;\n"); //$NON-NLS-1$
		writer.write( "__signed__ int signedInt;\n"); //$NON-NLS-1$
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTASMDefinition asmDefinition = (IASTASMDefinition) i.next();
		assertEquals( asmDefinition.getBody(), "CODE"); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction) i.next();
		assertTrue( foo.isInline() );
		IASTVariable constInt = (IASTVariable) i.next();
		assertTrue( constInt.getAbstractDeclaration().isConst());
		IASTVariable volInt = (IASTVariable) i.next();
		assertTrue( volInt.getAbstractDeclaration().isVolatile() );
		IASTVariable signedInt = (IASTVariable) i.next();
		assertTrue( ((IASTSimpleTypeSpecifier) signedInt.getAbstractDeclaration().getTypeSpecifier()).isSigned() );
		assertFalse( i.hasNext() );
		for( int j = 0; j < 2; ++j )
		{
			writer = new StringWriter();
			writer.write( "int * __restrict__ resPointer1;\n"); //$NON-NLS-1$
			writer.write( "int * __restrict resPointer2;\n"); //$NON-NLS-1$
			i = parse( writer.toString(), true, ((j == 0 )? ParserLanguage.C : ParserLanguage.CPP) ).getDeclarations();
			int count = 0;
			while( i.hasNext() )
			{
				++count;
				IASTVariable resPointer = (IASTVariable) i.next();
				Iterator pOps = resPointer.getAbstractDeclaration().getPointerOperators();
				assertTrue( pOps.hasNext() );
				ASTPointerOperator op = (ASTPointerOperator) pOps.next();
				assertFalse( pOps.hasNext() );
				assertEquals( op, ASTPointerOperator.RESTRICT_POINTER );
			}
	
			assertEquals( count, 2 );
		}
	}

	public void testBug73954A() throws Exception{
	    StringWriter writer = new StringWriter();
		writer.write("void f(){							\n");//$NON-NLS-1$
		writer.write("	__builtin_expect( 23, 2); 		\n");//$NON-NLS-1$
		writer.write("	__builtin_prefetch( (const void *)0, 1, 2);				\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_val();			\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_valf();			\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_vall();			\n");//$NON-NLS-1$
		writer.write("	__builtin_inf();				\n");//$NON-NLS-1$
		writer.write("	__builtin_inff();				\n");//$NON-NLS-1$
		writer.write("	__builtin_infl();				\n");//$NON-NLS-1$
		writer.write("	__builtin_nan(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nanf(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nanl(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nans(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nansf(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nansl(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffs (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_clz (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_ctz (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_popcount (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_parity (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffsl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_clzl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_ctzl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_popcountl (0);		\n");//$NON-NLS-1$
		writer.write("	__builtin_parityl (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffsll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_clzll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ctzll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_popcountll (0);		\n");//$NON-NLS-1$
		writer.write("	__builtin_parityll (0); 		\n");//$NON-NLS-1$
		writer.write("}                                 \n"); //$NON-NLS-1$
		
	    parse( writer.toString() );
	}
}
