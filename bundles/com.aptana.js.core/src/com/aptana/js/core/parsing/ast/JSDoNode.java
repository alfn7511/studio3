/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.js.core.parsing.ast;

import com.aptana.parsing.ast.IParseNode;

import beaver.Symbol;

public class JSDoNode extends JSNode
{
	private Symbol _leftParenthesis;
	private Symbol _rightParenthesis;

	/**
	 * Used by ANTLR AST
	 * 
	 * @param leftParenthesis
	 * @param rightParenthesis
	 */
	public JSDoNode(int start, int end, Symbol leftParenthesis, Symbol rightParenthesis)
	{
		super(IJSNodeTypes.DO);

		this._leftParenthesis = leftParenthesis;
		this._rightParenthesis = rightParenthesis;
		this.setLocation(start, end);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSNode#accept(com.aptana.editor.js.parsing.ast.JSTreeWalker)
	 */
	@Override
	public void accept(JSTreeWalker walker)
	{
		walker.visit(this);
	}

	/**
	 * getBody
	 * 
	 * @return
	 */
	public IParseNode getBody()
	{
		return this.getChild(0);
	}

	/**
	 * getCondition
	 * 
	 * @return
	 */
	public IParseNode getCondition()
	{
		return this.getChild(1);
	}

	/**
	 * getLeftParenthesis
	 * 
	 * @return
	 */
	public Symbol getLeftParenthesis()
	{
		return this._leftParenthesis;
	}

	/**
	 * getRightParenthesis
	 * 
	 * @return
	 */
	public Symbol getRightParenthesis()
	{
		return this._rightParenthesis;
	}
}
