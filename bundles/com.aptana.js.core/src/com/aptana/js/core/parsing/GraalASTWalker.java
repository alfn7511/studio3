package com.aptana.js.core.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.internal.utils.StringPool;

import com.aptana.core.util.StringUtil;
import com.aptana.js.core.JSLanguageConstants;
import com.aptana.js.core.parsing.ast.JSAbstractForNode;
import com.aptana.js.core.parsing.ast.JSArgumentsNode;
import com.aptana.js.core.parsing.ast.JSArrayNode;
import com.aptana.js.core.parsing.ast.JSArrowFunctionNode;
import com.aptana.js.core.parsing.ast.JSAssignmentNode;
import com.aptana.js.core.parsing.ast.JSBinaryArithmeticOperatorNode;
import com.aptana.js.core.parsing.ast.JSBinaryBooleanOperatorNode;
import com.aptana.js.core.parsing.ast.JSBreakNode;
import com.aptana.js.core.parsing.ast.JSCaseNode;
import com.aptana.js.core.parsing.ast.JSCatchNode;
import com.aptana.js.core.parsing.ast.JSClassNode;
import com.aptana.js.core.parsing.ast.JSCommaNode;
import com.aptana.js.core.parsing.ast.JSComputedPropertyNameNode;
import com.aptana.js.core.parsing.ast.JSConditionalNode;
import com.aptana.js.core.parsing.ast.JSConstructNode;
import com.aptana.js.core.parsing.ast.JSContinueNode;
import com.aptana.js.core.parsing.ast.JSDeclarationNode;
import com.aptana.js.core.parsing.ast.JSDefaultNode;
import com.aptana.js.core.parsing.ast.JSDoNode;
import com.aptana.js.core.parsing.ast.JSElisionNode;
import com.aptana.js.core.parsing.ast.JSEmptyNode;
import com.aptana.js.core.parsing.ast.JSExportNode;
import com.aptana.js.core.parsing.ast.JSFalseNode;
import com.aptana.js.core.parsing.ast.JSFinallyNode;
import com.aptana.js.core.parsing.ast.JSForInNode;
import com.aptana.js.core.parsing.ast.JSForNode;
import com.aptana.js.core.parsing.ast.JSForOfNode;
import com.aptana.js.core.parsing.ast.JSFunctionNode;
import com.aptana.js.core.parsing.ast.JSGeneratorFunctionNode;
import com.aptana.js.core.parsing.ast.JSGetElementNode;
import com.aptana.js.core.parsing.ast.JSGetPropertyNode;
import com.aptana.js.core.parsing.ast.JSGetterNode;
import com.aptana.js.core.parsing.ast.JSGroupNode;
import com.aptana.js.core.parsing.ast.JSIdentifierNode;
import com.aptana.js.core.parsing.ast.JSIfNode;
import com.aptana.js.core.parsing.ast.JSImportNode;
import com.aptana.js.core.parsing.ast.JSImportSpecifierNode;
import com.aptana.js.core.parsing.ast.JSInvokeNode;
import com.aptana.js.core.parsing.ast.JSLabelledNode;
import com.aptana.js.core.parsing.ast.JSNameValuePairNode;
import com.aptana.js.core.parsing.ast.JSNode;
import com.aptana.js.core.parsing.ast.JSNullNode;
import com.aptana.js.core.parsing.ast.JSNumberNode;
import com.aptana.js.core.parsing.ast.JSObjectNode;
import com.aptana.js.core.parsing.ast.JSParametersNode;
import com.aptana.js.core.parsing.ast.JSParseRootNode;
import com.aptana.js.core.parsing.ast.JSPostUnaryOperatorNode;
import com.aptana.js.core.parsing.ast.JSPreUnaryOperatorNode;
import com.aptana.js.core.parsing.ast.JSRegexNode;
import com.aptana.js.core.parsing.ast.JSRestElementNode;
import com.aptana.js.core.parsing.ast.JSReturnNode;
import com.aptana.js.core.parsing.ast.JSSetterNode;
import com.aptana.js.core.parsing.ast.JSSpreadElementNode;
import com.aptana.js.core.parsing.ast.JSStatementsNode;
import com.aptana.js.core.parsing.ast.JSStringNode;
import com.aptana.js.core.parsing.ast.JSSwitchNode;
import com.aptana.js.core.parsing.ast.JSThrowNode;
import com.aptana.js.core.parsing.ast.JSTrueNode;
import com.aptana.js.core.parsing.ast.JSTryNode;
import com.aptana.js.core.parsing.ast.JSVarNode;
import com.aptana.js.core.parsing.ast.JSWhileNode;
import com.aptana.js.core.parsing.ast.JSWithNode;
import com.aptana.js.core.parsing.ast.JSYieldNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.parsing.ast.ParseNode;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.BreakNode;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.CaseNode;
import com.oracle.js.parser.ir.CatchNode;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ContinueNode;
import com.oracle.js.parser.ir.EmptyNode;
import com.oracle.js.parser.ir.Expression;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IfNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.JoinPredecessorExpression;
import com.oracle.js.parser.ir.LabelNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.LiteralNode.ArrayLiteralNode;
import com.oracle.js.parser.ir.Module;
import com.oracle.js.parser.ir.Module.ExportEntry;
import com.oracle.js.parser.ir.Module.ImportEntry;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.ReturnNode;
import com.oracle.js.parser.ir.Statement;
import com.oracle.js.parser.ir.SwitchNode;
import com.oracle.js.parser.ir.TernaryNode;
import com.oracle.js.parser.ir.ThrowNode;
import com.oracle.js.parser.ir.TryNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.WhileNode;
import com.oracle.js.parser.ir.WithNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;

import beaver.Symbol;

class GraalASTWalker extends NodeVisitor<LexicalContext>
{

	private IParseRootNode fRootNode;
	private Stack<IParseNode> fNodeStack = new Stack<IParseNode>();
	private boolean wipeNextIdent;
	private Map<Expression, JSNode> pushOnLeave;
	private final String source;

	private StringPool pool; // not sure right now if pooling helps our RAM usage much (or maybe even makes it worse!)

	private Module module;

	public GraalASTWalker(String source, LexicalContext lc)
	{
		super(lc);
		this.source = source;
		fRootNode = new JSParseRootNode();
		fNodeStack.push(fRootNode);
		wipeNextIdent = false;
		pushOnLeave = new HashMap<Expression, JSNode>();
		pool = new StringPool();
	}

	@Override
	public boolean enterEmptyNode(EmptyNode emptyNode)
	{
		JSNode node = new JSEmptyNode(toSymbol(emptyNode));
		node.setSemicolonIncluded(true);
		addChildToParent(node);
		return super.enterEmptyNode(emptyNode);
	}

	@Override
	public boolean enterLiteralNode(LiteralNode<?> literalNode)
	{
		if (literalNode.isNumeric())
		{
			// If there's a trailing comment, the end location may be off by length of comment!
			// do start position + length of raw value? That could be wrong because they convert numbers based on hex/octal/decimal/whatever!
			// So... maybe look for first whitespace or '/' and cut off before that?
			int start = literalNode.getStart();
			int finish = literalNode.getFinish();
			String raw = this.source.substring(start, finish);
			int lastSlash = raw.indexOf('/');
			if (lastSlash != -1) {
				raw = raw.substring(0, lastSlash);
			}
			raw = raw.trim();
			finish = start + raw.length() - 1; // we use inclusive end, so subtract one!
			addChildToParent(
					new JSNumberNode(toSymbol(JSTokenType.NUMBER, start, finish, pool.add(raw))));
		}
		else if (literalNode.isNull())
		{
			addChildToParent(new JSNullNode(toSymbol(JSTokenType.NULL, literalNode)));
		}
		else if (literalNode.isString())
		{
			// Peek at offset before literal to sniff the single/double quotes
			int start = literalNode.getStart() - 1; // go back one char to include open quote!
			int finish = literalNode.getFinish(); // graal uses exclusive end, so offset is already one past string value (i.e. includes quote)
			String value = literalNode.getString(); // This is the raw value without the quotes
			char c = source.charAt(start);
			value = c + value + c;
			addChildToParent(new JSStringNode(toSymbol(JSTokenType.STRING, start, finish, pool.add(value))));
		}
		else if (literalNode.isBoolean())
		{
			if (literalNode.getBoolean())
			{
				addChildToParent(new JSTrueNode(toSymbol(JSTokenType.TRUE, literalNode)));
			}
			else
			{
				addChildToParent(new JSFalseNode(toSymbol(JSTokenType.FALSE, literalNode)));
			}
		}
		else if (literalNode.isArray())
		{
			int lBracket = findChar('[', literalNode.getStart());
			int rBracket = findLastChar(']', literalNode.getFinish());
			addToParentAndPushNodeToStack(new JSArrayNode(toSymbol(JSTokenType.LBRACKET, lBracket),
					toSymbol(JSTokenType.RBRACKET, rBracket)));
		}
		else
		{
			addChildToParent(new JSRegexNode(toSymbol(JSTokenType.REGEX, literalNode, literalNode.toString())));
		}
		return super.enterLiteralNode(literalNode);
	}

	@Override
	public Node leaveLiteralNode(LiteralNode<?> literalNode)
	{
		if (literalNode.isArray())
		{
			JSArrayNode arrayNode = (JSArrayNode) getCurrentNode();

			IParseNode[] nonElidedChildren = arrayNode.getChildren();

			ArrayLiteralNode aln = (ArrayLiteralNode) literalNode;
			final List<Expression> oldValue = aln.getElementExpressions();
			int childCount = oldValue.size();

			if (nonElidedChildren.length != childCount)
			{
				// Re-insert empty nodes for elided elements!
				IParseNode[] elidedChildren = new IParseNode[childCount];
				int j = 0;
				for (int i = 0; i < childCount; i++)
				{
					Expression e = oldValue.get(i);
					if (e == null)
					{
						elidedChildren[i] = new JSElisionNode(new JSNullNode());
					}
					else
					{
						elidedChildren[i] = nonElidedChildren[j];
						j++;
					}
				}
				arrayNode.setChildren(elidedChildren);
			}
			popNode();
		}
		return super.leaveLiteralNode(literalNode);
	}

	@Override
	public boolean enterReturnNode(ReturnNode returnNode)
	{
		// may be a "generated" return from a single expression arrow function body with no braces!
		if (returnNode.isTokenType(TokenType.RETURN))
		{
			JSNode node = new JSReturnNode(returnNode.getStart(), returnNode.getFinish());
			node.setSemicolonIncluded(true);
			addToParentAndPushNodeToStack(node);
		}
		return super.enterReturnNode(returnNode);
	}

	@Override
	public Node leaveReturnNode(ReturnNode returnNode)
	{
		// may be a "generated" return from a single expression arrow function body with no braces!
		if (returnNode.isTokenType(TokenType.RETURN))
		{
			// If there's no child to return node, add JSEmptyNode!
			if (!returnNode.hasExpression())
			{
				Symbol r = toSymbol(JSTokenType.RETURN, returnNode);
				addChildToParent(new JSEmptyNode(r));
			}
			popNode();
		}
		return super.leaveReturnNode(returnNode);
	}

	@Override
	public boolean enterIdentNode(IdentNode identNode)
	{
		if (wipeNextIdent)
		{
			wipeNextIdent = false;
		}
		else
		{
			if (identNode.isRestParameter())
			{
				addChildToParent(new JSRestElementNode(null, new JSIdentifierNode(identifierSymbol(identNode))));
			}
			else if (identNode.isDefaultParameter())
			{
				JSIdentifierNode ident = new JSIdentifierNode(identifierSymbol(identNode));
				// find first expression statement, holding a binary node, whose lhs has an identNode matching this.
				// rhs is ternarynode whose trueExpr is the default value!
				BinaryNode matchingInitializer = matchingInitializer(identNode);
				if (matchingInitializer != null)
				{
					TernaryNode tn = (TernaryNode) matchingInitializer.rhs();
					// FIXME What are the correct offsets? Where's the =?
					JSDeclarationNode declNode = new JSDeclarationNode(identNode.getStart(), identNode.getFinish() - 1,
							null);
					addToParentAndPushNodeToStack(declNode);
					addChildToParent(ident);
					tn.getTrueExpression().accept(this);
					popNode();
				}
				else
				{
					addChildToParent(ident);
				}
			}
			else
			{
				addChildToParent(new JSIdentifierNode(identifierSymbol(identNode)));
			}
		}
		return super.enterIdentNode(identNode);
	}

	private BinaryNode matchingInitializer(IdentNode identNode)
	{
		Block funcBody = lc.getCurrentFunction().getBody();
		List<Statement> statements = funcBody.getStatements();
		for (Statement stmt : statements)
		{
			if (stmt instanceof ExpressionStatement)
			{
				ExpressionStatement es = (ExpressionStatement) stmt;
				Expression e = es.getExpression();
				if (e instanceof BinaryNode)
				{
					BinaryNode possible = (BinaryNode) e;
					if (possible.lhs() instanceof IdentNode)
					{
						IdentNode lhs = (IdentNode) possible.lhs();
						if (lhs.getName().equals(identNode.getName()))
						{
							return possible;
						}
					}
				}
			}
		}
		return null;
	}

	private Symbol toSymbol(Node ident)
	{
		return new Symbol((short) 0, ident.getStart(), ident.getFinish() - 1, ident.toString());
	}

	private Symbol identifierSymbol(Node ident)
	{
		return identifierSymbol(ident, ident.toString());
	}

	private Symbol identifierSymbol(Node ident, String value)
	{
		return identifierSymbol(ident.getStart(), ident.getFinish() - 1, value);
	}

	private Symbol identifierSymbol(int start, int finish, String value)
	{
		return toSymbol(JSTokenType.IDENTIFIER, start, finish, pool.add(value));
	}

	private Symbol identifierSymbol(String value)
	{
		return identifierSymbol(-1, -1, value);
	}

	private Symbol toSymbol(JSTokenType type, Node ident)
	{
		return toSymbol(type, ident.getStart(), ident.getFinish() - 1);
	}

	private Symbol toSymbol(JSTokenType type, Node ident, Object value)
	{
		return toSymbol(type, ident.getStart(), ident.getFinish() - 1, value);
	}

	/**
	 * For single-character symbols (i.e. LPAREN, RPAREN, COLON, SEMICOLON)
	 * 
	 * @param type
	 * @param offset
	 *            single character offset. Symbols assume inclusive range, so start and end will be the same index here!
	 * @return
	 */
	private Symbol toSymbol(JSTokenType type, int offset)
	{
		return toSymbol(type, offset, offset);
	}

	private Symbol toSymbol(JSTokenType type, int start, int finish)
	{
		return toSymbol(type, start, finish, type.getName());
	}

	private Symbol toSymbol(JSTokenType type, int start, int finish, Object value)
	{
		return new Symbol(type.getIndex(), start, finish, value);
	}

	@Override
	public boolean enterVarNode(VarNode varNode)
	{
		if (!varNode.isFunctionDeclaration() && !(varNode.getInit() instanceof ClassNode))
		{
			JSTokenType type = JSTokenType.VAR;
			int adjustStartOffsetBy = 4; // The start offset points to where the name starts, not the beginning of
											// var/let/const keyword!
			if (varNode.isConst())
			{
				type = JSTokenType.CONST;
				adjustStartOffsetBy = 5;
			}
			else if (varNode.isLet())
			{
				type = JSTokenType.LET;
			}
			Symbol var = toSymbol(type, varNode.getStart() - adjustStartOffsetBy, varNode.getFinish() - 1);

			ExportedStatus exportStatus = getExportStatus(varNode.getName());
			JSNode node = new JSVarNode(var);
			if (exportStatus.isExported)
			{
				// push export node
				JSExportNode exportNode = new JSExportNode(exportStatus.isDefault, node);
				exportNode.setSemicolonIncluded(true);
				addToParentAndPushNodeToStack(exportNode);
				fNodeStack.push(node); // now push var node to top of stack (it's already hooked as child)
			}
			else
			{
				node.setSemicolonIncluded(true);
				addToParentAndPushNodeToStack(node);
			}
			int equalOffset = findChar('=', varNode.getName().getFinish(),
					varNode.getInit() != null ? varNode.getInit().getStart() : varNode.getFinish());
			addToParentAndPushNodeToStack(new JSDeclarationNode(varNode.getStart(), varNode.getFinish() - 1,
					toSymbol(JSTokenType.EQUAL, equalOffset)));
		}
		else if (varNode.getInit() instanceof ClassNode)
		{
			// class decl
			IdentNode name = varNode.getName();
			ClassNode classNode = (ClassNode) varNode.getInit();
			if (name != null && name.getName().equals(Module.DEFAULT_EXPORT_BINDING_NAME))
			{
				JSClassNode jsClassNode = new JSClassNode(false, classNode.getClassHeritage() != null);
				addChildToParent(new JSExportNode(true, jsClassNode));
				fNodeStack.push(jsClassNode);
			}
			else
			{
				addToParentAndPushNodeToStack(new JSClassNode(name != null, classNode.getClassHeritage() != null));
			}

		}

		return super.enterVarNode(varNode);
	}

	@Override
	public Node leaveVarNode(VarNode varNode)
	{
		Expression init = varNode.getInit();
		// assignment is right associative, so we end up visiting the value before the name. We have to invert the
		// children
		if (!varNode.isFunctionDeclaration() && !(init instanceof ClassNode))
		{
			// Invert the two children of the declaration!
			IParseNode node = getCurrentNode();
			if (node.getChildCount() > 1)
			{
				IParseNode value = node.getChild(0);
				IParseNode name = node.getChild(1);
				((JSNode) node).setChildren(new IParseNode[] { name, value });
			}
			else
			{
				// If we only have name, add an empty node for value
				node.addChild(new JSEmptyNode(node.getEndingOffset()));
			}
			JSDeclarationNode declNode = (JSDeclarationNode) popNode(); // decl node
			popNode(); // var node

			IdentNode nameNode = varNode.getName();
			ExportedStatus exportStatus = getExportStatus(nameNode);
			if (exportStatus.isExported)
			{
				JSExportNode exportNode = (JSExportNode) popNode(); // export node
				// Handle when we're exporting default function!
				if (init instanceof FunctionNode && Module.DEFAULT_EXPORT_BINDING_NAME.equals(nameNode.getName()))
				{
					// hoist the function up to be child of export itself
					JSFunctionNode funcNode = (JSFunctionNode) declNode.getValue();
					exportNode.setChildren(new IParseNode[] { funcNode });
				}
			}
		}
		else if (init instanceof ClassNode)
		{
			// Swap order of body and name
			// FIXME What if no name?!
			IParseNode node = getCurrentNode();
			int childCount = node.getChildCount(); // optional first child is superclass/heritage
			IParseNode body = node.getChild(childCount - 2); // second-last child should be class body
			IParseNode name = node.getChild(childCount - 1); // last child should be the class name
			if (childCount == 2)
			{
				((JSNode) node).setChildren(new IParseNode[] { name, body });
			}
			else
			{
				IParseNode heritage = node.getFirstChild();
				((JSNode) node).setChildren(new IParseNode[] { name, heritage, body });
			}

			popNode(); // class node
		}

		return super.leaveVarNode(varNode);
	}

	@Override
	public boolean enterFunctionNode(FunctionNode functionNode)
	{
		// FIXME: The shape of the AST seems wrong for test1.js
		// it'd be good to do an AST dump on old parser versus new to see what it is
		// Because when formatting it appears to expect a "parent node" in the format stack
		// I think I messed up the relationship between JSVarNode, JSDeclarationNode and JSFunctionNode?
		// for cases like: var myFunc = function() {};
		if (!functionNode.isProgram())
		{
			IdentNode ident = functionNode.getIdent();
			Block body = functionNode.getBody();
			// If anonymous "normal" function, ident points at start of function keyword
			int funcStart = ident.getStart();
			int funcEnd = body.getFinish() + 1;
			JSFunctionNode funcNode;
			switch (functionNode.getKind())
			{
				case ARROW:
					funcNode = new JSArrowFunctionNode(funcStart, funcEnd);
					break;
				case GENERATOR:
					funcNode = new JSGeneratorFunctionNode(funcStart, funcEnd);
					break;
				default:
					if (!functionNode.isAnonymous())
					{
						funcStart -= 9; // TODO: if named, ident points at start of name, so we need to subtract 9
									// ("function ")
					}
					funcNode = new JSFunctionNode(funcStart, funcEnd);
					break;
			}
			
			ExportedStatus exportStatus = getExportStatus(ident);
			if (exportStatus.isExported)
			{
				addToParentAndPushNodeToStack(new JSExportNode(exportStatus.isDefault, funcNode));
				fNodeStack.push(funcNode); // make function node top of stack
			}
			else
			{
				addToParentAndPushNodeToStack(funcNode);
			}
			// Visit the name
			if (!functionNode.isAnonymous())
			{
				addChildToParent(new JSIdentifierNode(identifierSymbol(ident)));
			}
			else if (!(funcNode instanceof JSArrowFunctionNode))
			{
				// use empty node for anonymous functions (but arrow functions have no name always)
				addChildToParent(new JSEmptyNode(toSymbol(ident)));
			}
			// Need to explicitly visit the params
			int lParen = findChar('(', ident.getFinish(), body.getStart());
			int rParen = findLastChar(')', body.getStart());
			addToParentAndPushNodeToStack(new JSParametersNode(lParen, rParen));
			functionNode.visitParameters(this);
			popNode(); // parameters
		}
		else
		{
			module = functionNode.getModule();
			if (module != null)
			{
				handleImportsAndExports();
			}
		}
		return super.enterFunctionNode(functionNode);
	}

	private void handleImportsAndExports()
	{
		handleImports();
		handleExports();
	}

	protected void handleImports()
	{
		List<ImportEntry> imports = module.getImportEntries();
		for (ImportEntry entry : imports)
		{
			String from = entry.getModuleRequest(); // FIXME If we have multiple from same module, need to make
													// them
													// JSNamedImportsNode!
			String as = entry.getLocalName();
			String name = entry.getImportName();

			JSImportNode importNode = new JSImportNode("'" + from + "'");

			JSImportSpecifierNode node;
			JSIdentifierNode alias = null;
			if (!StringUtil.isEmpty(as))
			{
				alias = new JSIdentifierNode(identifierSymbol(as));
			}

			if (Module.STAR_NAME.equals(name))
			{
				node = new JSImportSpecifierNode(new Symbol(JSLanguageConstants.STAR), alias);
			}
			else
			{
				JSIdentifierNode importedName = new JSIdentifierNode(identifierSymbol(name));
				if (alias == null)
				{
					node = new JSImportSpecifierNode(importedName);
				}
				else
				{
					node = new JSImportSpecifierNode(importedName, alias);
				}
			}
			if (node != null)
			{
				importNode.addChild(node);
			}
			addChildToParent(importNode);
		}
	}

	protected void handleExports()
	{
		List<ExportEntry> starExports = module.getStarExportEntries();
		for (ExportEntry entry : starExports)
		{
			addChildToParent(new JSExportNode(false, (Symbol) null, "'" + entry.getModuleRequest() + "'"));
		}
	}

	@Override
	public Node leaveFunctionNode(FunctionNode functionNode)
	{
		if (!functionNode.isProgram())
		{

			popNode(); // func node
			ExportedStatus exportStatus = getExportStatus(functionNode.getIdent());
			if (exportStatus.isExported)
			{
				popNode(); // export node
			}
			// when the function node is the "init" of a parent VarNode, we need to avoid hitting the "name" IdentNode.
			if (!functionNode.isAnonymous() && !functionNode.isMethod())
			{
				wipeNextIdent = true;
			}
		}
		else
		{
			module = null;
			pool = null;
			pushOnLeave = null;
			// FIXME If nodestack is not empty, spit out an error message
			fNodeStack = null;
		}
		return super.leaveFunctionNode(functionNode);
	}

	@Override
	public boolean enterUnaryNode(UnaryNode unaryNode)
	{
		JSNode theNode = null;
		JSTokenType type;
		TokenType tokenType = unaryNode.tokenType();
		int start = unaryNode.getStart();
		int finish = unaryNode.getFinish() - 1; // Symbol is inclusive range, graal uses exclusive end
		switch (tokenType)
		{
			case NOT:
				type = JSTokenType.EXCLAMATION;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 1, type.getName()));
				break;
			case INCPREFIX:
				type = JSTokenType.PLUS_PLUS;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 2, type.getName()));
				break;
			case DECPREFIX:
				type = JSTokenType.MINUS_MINUS;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 2, type.getName()));
				break;
			case INCPOSTFIX:
				type = JSTokenType.PLUS_PLUS;
				theNode = new JSPostUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getFinish() - 2, unaryNode.getFinish(), type.getName()));
				break;
			case DECPOSTFIX:
				type = JSTokenType.MINUS_MINUS;
				theNode = new JSPostUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getFinish() - 2, unaryNode.getFinish(), type.getName()));
				break;
			case BIT_NOT:
				type = JSTokenType.TILDE;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 1, type.getName()));
				break;
			// case ELLIPSIS:
			// break;
			case DELETE:
				type = JSTokenType.DELETE;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 6, type.getName()));
				break;
			case NEW:
				type = JSTokenType.NEW;
				theNode = new JSConstructNode(start, finish);
				break;
			case TYPEOF:
				type = JSTokenType.TYPEOF;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 6, type.getName()));
				break;
			case VOID:
				type = JSTokenType.VOID;
				theNode = new JSPreUnaryOperatorNode(start, finish,
						new Symbol(type.getIndex(), unaryNode.getStart(), unaryNode.getStart() + 4, type.getName()));
				break;
			case ADD:
				type = JSTokenType.PLUS;
				theNode = new JSPreUnaryOperatorNode(start, finish, toSymbol(type, unaryNode.getStart()));
				break;
			case SUB:
				type = JSTokenType.MINUS;
				theNode = new JSPreUnaryOperatorNode(start, finish, toSymbol(type, unaryNode.getStart()));
				break;

			case YIELD:
				type = JSTokenType.YIELD;
				theNode = new JSYieldNode(start, finish, new Symbol(type.getIndex(), 0, 0, type.getName()));
				break;

			case SPREAD_ARRAY:
				type = JSTokenType.DOT_DOT_DOT;
				theNode = new JSSpreadElementNode(start, finish, new Symbol(type.getIndex(), 0, 0, type.getName()));
				break;

			case SPREAD_ARGUMENT:
				type = JSTokenType.DOT_DOT_DOT;
				theNode = new JSSpreadElementNode(start, finish, new Symbol(type.getIndex(), 0, 0, type.getName()));
				break;

			default:
				throw new IllegalStateException("Reached unhandled unary node type! " + unaryNode);
		}
		addToParentAndPushNodeToStack(theNode);
		return super.enterUnaryNode(unaryNode);
	}

	@Override
	public Node leaveUnaryNode(UnaryNode unaryNode)
	{
		TokenType tokenType = unaryNode.tokenType();
		if (tokenType == TokenType.NEW)
		{
			JSConstructNode cn = (JSConstructNode) getCurrentNode();
			// FIXME If this is a construct node and it's child is an invoke node:
			// Grab the first child of invoke node and make that the construct first child
			// grab the second child of invoke node (args) and make that construct second child (or empty node)
			JSNode firstChild = (JSNode) cn.getFirstChild();
			if (firstChild instanceof JSInvokeNode)
			{
				JSNode expression = (JSNode) firstChild.getChild(0);
				JSNode arguments = (JSNode) firstChild.getChild(1);
				IParseNode[] newChildren = new IParseNode[] { expression, arguments };
				cn.setChildren(newChildren);
			}
		}
		popNode();
		return super.leaveUnaryNode(unaryNode);
	}

	@Override
	public boolean enterBinaryNode(BinaryNode binaryNode)
	{
		JSNode theNode;
		JSTokenType type;
		TokenType tokenType = binaryNode.tokenType();
		int start = binaryNode.getStart();
		int finish = binaryNode.getFinish() - 1; // symbols are inclusive ranges, graal uses exclusive end
		// FIXME So we should be passing the start/end to the JSNode, but the symbols need to be "searched" for between
		// lhs end and rhs start!
		int symbolStart;
		switch (tokenType)
		{
			// JSBinaryBooleanOperatorNode
			case INSTANCEOF:
				type = JSTokenType.INSTANCEOF;
				symbolStart = findChar('i', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 9));
				break;
			case IN:
				type = JSTokenType.IN;
				symbolStart = findChar('i', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case EQ:
				type = JSTokenType.EQUAL_EQUAL;
				symbolStart = findChar('=', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case EQ_STRICT:
				type = JSTokenType.EQUAL_EQUAL_EQUAL;
				symbolStart = findChar('=', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 2));
				break;
			case NE:
				type = JSTokenType.EXCLAMATION_EQUAL;
				symbolStart = findChar('!', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case NE_STRICT:
				type = JSTokenType.EXCLAMATION_EQUAL_EQUAL;
				symbolStart = findChar('!', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case LE:
				type = JSTokenType.LESS_EQUAL;
				symbolStart = findChar('<', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case LT:
				type = JSTokenType.LESS;
				symbolStart = findChar('<', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case GE:
				type = JSTokenType.GREATER_EQUAL;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case GT:
				type = JSTokenType.GREATER;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case AND:
				type = JSTokenType.AMPERSAND_AMPERSAND;
				symbolStart = findChar('&', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case OR:
				type = JSTokenType.PIPE_PIPE;
				symbolStart = findChar('|', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryBooleanOperatorNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;

			// JSBinaryArithmeticOperatorNode
			case ADD:
				type = JSTokenType.PLUS;
				symbolStart = findChar('+', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case SUB:
				type = JSTokenType.MINUS;
				symbolStart = findChar('-', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			// shift operators
			case SHL:
				type = JSTokenType.LESS_LESS;
				symbolStart = findChar('<', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish,
						toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case SAR:
				type = JSTokenType.GREATER_GREATER;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish,
						toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case SHR:
				type = JSTokenType.GREATER_GREATER_GREATER;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish,
						toSymbol(type, symbolStart, symbolStart + 2));
				break;
			case BIT_AND:
				type = JSTokenType.AMPERSAND;
				symbolStart = findChar('&', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case BIT_XOR:
				type = JSTokenType.CARET;
				symbolStart = findChar('^', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case BIT_OR:
				type = JSTokenType.PIPE;
				symbolStart = findChar('|', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case MUL:
				type = JSTokenType.STAR;
				symbolStart = findChar('*', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case DIV:
				type = JSTokenType.FORWARD_SLASH;
				symbolStart = findChar('/', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case MOD:
				type = JSTokenType.PERCENT;
				symbolStart = findChar('%', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish, toSymbol(type, symbolStart));
				break;
			case EXP:
				type = JSTokenType.STAR_STAR;
				symbolStart = findChar('*', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSBinaryArithmeticOperatorNode(start, finish,
						toSymbol(type, symbolStart, symbolStart + 1));
				break;

			// Assignment
			case ASSIGN:
				type = JSTokenType.EQUAL;
				symbolStart = findChar('=', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart));
				break;
			case ASSIGN_BIT_AND:
				type = JSTokenType.AMPERSAND_EQUAL;
				symbolStart = findChar('&', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_ADD:
				type = JSTokenType.PLUS_EQUAL;
				symbolStart = findChar('+', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_SHR:
				type = JSTokenType.GREATER_GREATER_GREATER_EQUAL;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 3));
				break;
			case ASSIGN_BIT_OR:
				type = JSTokenType.PIPE_EQUAL;
				symbolStart = findChar('|', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_BIT_XOR:
				type = JSTokenType.CARET_EQUAL;
				symbolStart = findChar('^', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_DIV:
				type = JSTokenType.FORWARD_SLASH_EQUAL;
				symbolStart = findChar('/', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_MOD:
				type = JSTokenType.PERCENT_EQUAL;
				symbolStart = findChar('%', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_MUL:
				type = JSTokenType.STAR_EQUAL;
				symbolStart = findChar('*', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_SHL:
				type = JSTokenType.LESS_LESS_EQUAL;
				symbolStart = findChar('<', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 2));
				break;
			case ASSIGN_SAR:
				type = JSTokenType.GREATER_GREATER_EQUAL;
				symbolStart = findChar('>', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 2));
				break;
			case ASSIGN_SUB:
				type = JSTokenType.MINUS_EQUAL;
				symbolStart = findChar('-', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 1));
				break;
			case ASSIGN_EXP:
				type = JSTokenType.STAR_STAR_EQUAL;
				symbolStart = findChar('*', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSAssignmentNode(start, finish, toSymbol(type, symbolStart, symbolStart + 2));
				break;

			// comma...
			case COMMARIGHT:
				type = JSTokenType.COMMA;
				symbolStart = findChar(',', binaryNode.lhs().getFinish(), binaryNode.rhs().getStart());
				theNode = new JSCommaNode(start, finish, toSymbol(type, symbolStart));
				break;
			default:
				throw new IllegalStateException("Reached unhandled binary node type! " + binaryNode);
		}
		// push operator node to stack
		addToParentAndPushNodeToStack(theNode);

		// if lhs needs parens, add group node
		Expression lhs = binaryNode.lhs();
		boolean lhsNeedsParens = lhs != null && tokenType.needsParens(lhs.tokenType(), true);
		if (lhsNeedsParens)
		{
			// Hack this, it looks liek only place we aks for left/right parens here is in formatter as offsets to place
			// them
			int lParen = lhs.getStart();
			int rParen = lhs.getFinish() - 1;
			// Parens not included in binaryNode's position! We need to search backwards from start!
			// int lParen = findLastChar('(', binaryNode.getStart());
			// int rParen = findChar(')', lhs.getFinish(), binaryNode.getAssignmentSource().getStart());
			// FIXME, the AST is telling us we should inject parens to group, but there may be no parens in the actual
			// source!
			// if (lParen == -1)
			// {
			// throw new IllegalStateException("Unable to find left paren");
			// }
			// if (rParen == -1)
			// {
			// throw new IllegalStateException("Unable to find right paren");
			// }
			addToParentAndPushNodeToStack(
					new JSGroupNode(toSymbol(JSTokenType.LPAREN, lParen), toSymbol(JSTokenType.RPAREN, rParen)));
		}
		return super.enterBinaryNode(binaryNode);
	}

	@Override
	public Node leaveBinaryNode(BinaryNode binaryNode)
	{
		TokenType tokenType = binaryNode.tokenType();

		Expression lhs = binaryNode.lhs();
		boolean lhsNeedsParens = lhs != null && tokenType.needsParens(lhs.tokenType(), true); // incorrect?
		// boolean lhsNeedsParens = lhs != null && lhs.tokenType().needsParens(tokenType, true);
		boolean rhsNeedsParens = tokenType.needsParens(binaryNode.rhs().tokenType(), false); // incorrect?
		// boolean rhsNeedsParens = binaryNode.rhs().tokenType().needsParens(tokenType, false);
		JSNode parentNode = (JSNode) getCurrentNode();
		JSNode lastChild = (JSNode) parentNode.getLastChild();
		if (lhsNeedsParens || rhsNeedsParens)
		{
			// remove the last child, it'll need to be placed under new parent
			IParseNode[] children = parentNode.getChildren();
			IParseNode[] newChildren = new IParseNode[children.length - 1];
			System.arraycopy(children, 0, newChildren, 0, children.length - 1);
			parentNode.setChildren(newChildren);
		}

		if (lhsNeedsParens)
		{
			popNode(); // remove the LHS group node, now current node is operator
		}

		// where does the last child go now?
		if (rhsNeedsParens)
		{
			// to new group node for RHS parens
			// int lParen = findChar('(', lhs.getFinish(), binaryNode.rhs().getStart());
			// if (lParen == -1)
			// {
			// addChildToParent(lastChild);
			// // FIXME We may be told we *need* to put this in parens, but it may not actually have any!
			//// throw new IllegalStateException("Unable to find left paren");
			// } else {
			// int rParen = findChar(')', binaryNode.rhs().getFinish()); // right paren not included in positions..
			// if (rParen == -1)
			// {
			// throw new IllegalStateException("Unable to find right paren");
			// }
			int lParen = binaryNode.rhs().getStart();
			int rParen = binaryNode.rhs().getFinish() - 1;
			addToParentAndPushNodeToStack(
					new JSGroupNode(toSymbol(JSTokenType.LPAREN, lParen), toSymbol(JSTokenType.RPAREN, rParen)));
			addChildToParent(lastChild);
			popNode(); // pop RHS parens node, current node is operator again
			// }
		}
		else if (lhsNeedsParens)
		{
			// add last child to operator node (moved out from incorrect LHS grouping)
			addChildToParent(lastChild);
		}

		popNode(); // pop operator node
		return super.leaveBinaryNode(binaryNode);
	}

	@Override
	public Node leaveExpressionStatement(ExpressionStatement expressionStatement)
	{
		JSNode node = (JSNode) getLastNode();
		if (node != null)
		{
			node.setSemicolonIncluded(true);
		}
		else
		{
			// There may have been an error!
			// throw new IllegalStateException("Ended an expression with no last statement/child within parent node!");
		}
		return super.leaveExpressionStatement(expressionStatement);
	}

	@Override
	public boolean enterObjectNode(ObjectNode objectNode)
	{
		// lBrace, rBrace
		int lBrace = objectNode.getStart();
		int rBrace = objectNode.getFinish() - 1;
		addToParentAndPushNodeToStack(
				new JSObjectNode(toSymbol(JSTokenType.LCURLY, lBrace), toSymbol(JSTokenType.RCURLY, rBrace)));
		return super.enterObjectNode(objectNode);
	}

	@Override
	public Node leaveObjectNode(ObjectNode objectNode)
	{
		popNode();
		return super.leaveObjectNode(objectNode);
	}

	@Override
	public boolean enterBlock(Block block)
	{
		if (block.isParameterBlock())
		{
			// when we have default parameters, the parser generates a special "parameter block"
			// that is filled with generated expressions to initialize argument values
			// The last statement is a block statement holding the real function body/block.
			super.enterBlock(block); // print our current node
			// don't create a statements node. We basically need to ignore until last BlockStatement and go into that
			block.getLastStatement().accept(this);
			return false; // don't go into this fake node. We already manually went into the real function body
		}
		else if (!block.isSynthetic())
		{
			int lBrace = findChar('{', block.getStart());
			// This gives a bogus end position for empty block in a function definition for me (test95.js)
			int rBrace = block.getFinish() - 1;
			// if we can, search for closing brace backwards from end of parent node?
			IParseNode parent = getCurrentNode();
			if (parent != null) {
				rBrace = findLastChar('}', parent.getEndingOffset());
			}
			addToParentAndPushNodeToStack(new JSStatementsNode(lBrace, rBrace));
		}
		else if (block.getLastStatement() instanceof ForNode)
		{
			// Handle synthetic block that holds var/let/const declarations in for loops
			addToParentAndPushNodeToStack(new JSStatementsNode(block.getStart(), block.getFinish() - 1));
		}
		return super.enterBlock(block);
	}

	@Override
	public Node leaveBlock(Block block)
	{
		if (!block.isSynthetic())
		{
			popNode();
		}
		else if (block.getLastStatement() instanceof ForNode)
		{
			ForNode fN = (ForNode) block.getLastStatement();

			// if block is synthetic and last child is for node, gather all the preceding var nodes and assign them
			// as
			// init values for the for loop!
			JSStatementsNode statements = (JSStatementsNode) getCurrentNode();
			IParseNode statementsParent = statements.getParent();
			JSNode forNode = (JSNode) statements.getLastChild();

			// remove for node from statements block
			IParseNode[] children = statements.getChildren();
			IParseNode[] newCHildren = new IParseNode[children.length - 1];
			System.arraycopy(children, 0, newCHildren, 0, children.length - 1);
			statements.setChildren(newCHildren);

			// convert statements node (block) into the initializer of the for loop
			if (children.length > 1 || fN.getInit() == null)
			{
				JSNode combinedVarDecls = combineVarDeclarations(statements.getStartingOffset(),
						statements.getChildren());
				((JSAbstractForNode) forNode).replaceInit(combinedVarDecls);
				// TODO Fix the positions of the lParen, semicolon1!
			}

			// remove statements from it's original parent
			IParseNode[] parentChildren = statementsParent.getChildren();
			IParseNode[] parentNewCHildren = new IParseNode[parentChildren.length];
			System.arraycopy(parentChildren, 0, parentNewCHildren, 0, parentChildren.length - 1);
			parentNewCHildren[parentChildren.length - 1] = forNode; // move for node up to parent of statements as
																	// last
																	// child, replacing the statements node
			((ParseNode) statementsParent).setChildren(parentNewCHildren);

			popNode(); // statements node
		}
		return super.leaveBlock(block);
	}

	private JSNode combineVarDeclarations(int offset, IParseNode[] children)
	{
		if (children == null || children.length == 0)
		{
			return new JSEmptyNode(offset);
		}
		JSVarNode firstVarNode = (JSVarNode) children[0];
		firstVarNode.setSemicolonIncluded(false);
		JSDeclarationNode declNode = (JSDeclarationNode) firstVarNode.getFirstChild();
		declNode.setSemicolonIncluded(false);

		// loop through the rest and grab the single JSDeclarationNode child and append to first var node!
		for (int i = 1; i < children.length; i++)
		{
			declNode = (JSDeclarationNode) children[i].getFirstChild();
			// We need to set semicolon included to false on each decl node!
			declNode.setSemicolonIncluded(false);

			firstVarNode.addChild(declNode);
		}
		return firstVarNode;
	}

	@Override
	public boolean enterClassNode(ClassNode classNode)
	{
		// if we extend something, visit that first
		Expression heritage = classNode.getClassHeritage();
		if (heritage != null)
		{
			heritage.accept(this);
		}
		// then wrap body in a statements node
		// FIXME: Fix offset. Presumably they should be after heritage?
		addToParentAndPushNodeToStack(new JSStatementsNode(classNode.getStart(), classNode.getFinish() - 1));
		// manually walk constructor
		PropertyNode constructorNode = classNode.getConstructor();
		if (constructorNode != null)
		{
			boolean visitConstructor = !isSyntheticConstructor(classNode, constructorNode);

			if (visitConstructor)
			{
				constructorNode.accept(this);
			}
		}
		// manually walk the properties
		List<PropertyNode> classElements = classNode.getClassElements();
		for (PropertyNode prop : classElements)
		{
			prop.accept(this);
		}
		return super.enterClassNode(classNode);
	}

	private boolean isSyntheticConstructor(ClassNode classNode, PropertyNode constructorNode)
	{
		// constructor may be synthetic from parser
		Expression value = constructorNode.getValue();
		if (!(value instanceof FunctionNode))
		{
			return false;
		}

		String keyName = constructorNode.getKeyName();
		IdentNode nameNode = classNode.getIdent();
		String constructorName = "constructor"; //$NON-NLS-1$
		if (nameNode != null)
		{
			constructorName = nameNode.getName();
		}
		if (!constructorName.equals(keyName))
		{
			return false;
		}

		FunctionNode constructorFunction = (FunctionNode) value;
		Block body = constructorFunction.getBody();
		int numParams = constructorFunction.getNumOfParams();
		boolean isSubclass = (classNode.getClassHeritage() != null);

		// not a subclass
		if (!isSubclass)
		{
			return numParams == 0 && body.getStatementCount() == 0;
		}

		// TODO do we need to check the statement is an ExpressionStatement holding a CallNode with function "super" and
		// single "args" argument?
		return numParams == 1 && constructorFunction.hasDirectSuper()
				&& constructorFunction.getParameter(0).isRestParameter() && body.getStatementCount() == 1;
	}

	@Override
	public Node leaveClassNode(ClassNode classNode)
	{
		popNode();
		return super.leaveClassNode(classNode);
	}

	@Override
	public boolean enterForNode(ForNode forNode)
	{
		JSAbstractForNode blah;
		Expression init = forNode.getInit();
		// When finding left paren we want to start after 'for' keyword, and end... at body start (at worst)
		int lParen = findChar('(', forNode.getStart() + 3,
				init != null ? init.getStart() : forNode.getBody().getStart());
		// when finding right paren we want to try from: modify.getFinish() or if null, test.getFinish(), or if null,
		// init.getFinish(), or if null, lParen + 1
		JoinPredecessorExpression modify = forNode.getModify();
		int rParenStart = lParen + 1;
		if (modify != null)
		{
			rParenStart = modify.getFinish();
		}
		else if (forNode.getTest() != null)
		{
			rParenStart = forNode.getTest().getFinish();
		}
		else if (init != null)
		{
			rParenStart = init.getFinish();
		}
		int rParen = findChar(')', rParenStart, forNode.getBody().getStart());

		if (lParen == -1)
		{
			throw new IllegalStateException("Bad left paren index!");
		}
		if (rParen == -1)
		{
			throw new IllegalStateException("Bad left paren index!");
		}

		if (forNode.isForIn())
		{
			// TODO: Create new variant of findChar that searches for strings like "in" here
			int in = findChar('i', init.getFinish(), modify.getStart());
			blah = new JSForInNode(forNode.getStart(), forNode.getFinish() - 1, toSymbol(JSTokenType.LPAREN, lParen),
					toSymbol(JSTokenType.IN, in, in + 2), toSymbol(JSTokenType.RPAREN, rParen));
		}
		else if (forNode.isForOf())
		{
			blah = new JSForOfNode(forNode.getStart(), forNode.getFinish() - 1, toSymbol(JSTokenType.LPAREN, lParen),
					toSymbol(JSTokenType.RPAREN, rParen));
		}
		else
		{
			// Handle ugly cases where modify/test/init may be null!
			int semi1End = forNode.getBody().getStart(); // worst case scenario
			if (forNode.getTest() != null)
			{
				semi1End = forNode.getTest().getStart();
			}
			int semi1 = findChar(';', init != null ? init.getFinish() : lParen + 1, semi1End);

			int semi2Start = semi1 + 1; // worst case scenario
			if (forNode.getTest() != null)
			{
				semi2Start = forNode.getTest().getFinish();
			}
			int semi2End = forNode.getBody().getStart(); // worst case scenario
			if (modify != null)
			{
				semi2End = modify.getStart();
			}

			int semi2 = findChar(';', semi2Start, semi2End);
			blah = new JSForNode(forNode.getStart(), forNode.getFinish() - 1, toSymbol(JSTokenType.LPAREN, lParen),
					toSymbol(JSTokenType.SEMICOLON, semi1), toSymbol(JSTokenType.SEMICOLON, semi2),
					toSymbol(JSTokenType.RPAREN, rParen));
		}
		addToParentAndPushNodeToStack(blah);
		if (init == null)
		{
			// if current parent
			addChildToParent(new JSEmptyNode(forNode.getStart()));
			if (forNode.getTest() == null)
			{
				addChildToParent(new JSEmptyNode(forNode.getStart()));
			}
		}
		return super.enterForNode(forNode);
	}

	@Override
	public Node leaveForNode(ForNode forNode)
	{
		IParseNode theNode = getCurrentNode();
		if (!forNode.isForInOrOf() && theNode.getChildCount() != 4)
		{
			// if the body is empty, we need to add an empty JSStatementsNode as body!
			if (forNode.getBody().getStatementCount() == 0)
			{
				theNode.addChild(new JSStatementsNode(forNode.getBody().getStart(), forNode.getBody().getFinish() - 1));
			}

			// we may have added the 4th child now...
			if (theNode.getChildCount() != 4)
			{
				// Inject empty nodes for missing test/increment expressions!
				IParseNode[] newChildren = new IParseNode[4];
				newChildren[0] = theNode.getFirstChild(); // we always inject an empty node if necessary for init
															// expression
				newChildren[3] = theNode.getLastChild(); // body is always last and should be non-null/empty

				// if we didn't already inject an empty test node...
				if (forNode.getTest() == null && forNode.getInit() != null)
				{
					// inject empty node for "test" expression
					newChildren[1] = new JSEmptyNode(newChildren[0].getEndingOffset());
					if (forNode.getModify() == null)
					{
						// if modify expression is null too, add empty node
						newChildren[2] = new JSEmptyNode(newChildren[0].getEndingOffset());
					}
					else
					{
						// copy over modify expression to right place
						newChildren[2] = theNode.getChild(1);
					}
				}
				// init and test expressions are set, just need to inject empty node for modify
				else if (forNode.getModify() == null)
				{
					// init is good, test is good, body is good.
					newChildren[1] = theNode.getChild(1);
					newChildren[2] = new JSEmptyNode(newChildren[1].getEndingOffset());
				}
				else
				{
					throw new IllegalStateException("Failed to set second and third children on for loop node!");
				}
				((JSNode) theNode).setChildren(newChildren);
			}
		}
		popNode();
		return super.leaveForNode(forNode);

	}

	@Override
	public boolean enterPropertyNode(PropertyNode propertyNode)
	{
		if (propertyNode.getGetter() != null)
		{
			addToParentAndPushNodeToStack(new JSGetterNode(propertyNode.getStart(), propertyNode.getFinish() - 1));
		}
		else if (propertyNode.getSetter() != null)
		{
			addToParentAndPushNodeToStack(new JSSetterNode(propertyNode.getStart(), propertyNode.getFinish() - 1));
		}
		else if (propertyNode.getValue() instanceof FunctionNode)
		{
			addToParentAndPushNodeToStack(
					new JSNameValuePairNode(propertyNode.getStart(), propertyNode.getFinish() - 1));
		}
		else
		{
			Expression key = propertyNode.getKey();
			int colonOffset = findChar(':', key.getFinish(), propertyNode.getValue().getStart());
			Symbol colon = toSymbol(JSTokenType.COLON, colonOffset);
			addToParentAndPushNodeToStack(
					new JSNameValuePairNode(propertyNode.getStart(), propertyNode.getFinish() - 1, colon));
			// if the property name is computed, manually traverse
			if (!(key instanceof LiteralNode) && !(key instanceof IdentNode))
			{
				addToParentAndPushNodeToStack(new JSComputedPropertyNameNode());
				propertyNode.getKey().accept(this);
				popNode(); // computed property name node
				propertyNode.getValue().accept(this);
				popNode(); // name value pair node
				return false;
			}
		}
		return super.enterPropertyNode(propertyNode);
	}

	@Override
	public Node leavePropertyNode(PropertyNode propertyNode)
	{
		if (propertyNode.getGetter() != null)
		{
			// if getter, grab "value", which should be a function node
			// Grab function node's body. Replace our value with that body
			JSGetterNode getterNode = (JSGetterNode) getCurrentNode();
			JSFunctionNode funcValue = (JSFunctionNode) getterNode.getValue();
			JSStatementsNode bodyNode = (JSStatementsNode) funcValue.getBody();
			getterNode.replaceChild(1, bodyNode);
		}
		else if (propertyNode.getSetter() != null)
		{
			JSSetterNode setterNode = (JSSetterNode) getCurrentNode();
			JSFunctionNode funcValue = (JSFunctionNode) setterNode.getValue();
			JSStatementsNode bodyNode = (JSStatementsNode) funcValue.getBody();
			JSParametersNode paramsNode = (JSParametersNode) funcValue.getParameters();
			setterNode.replaceChild(1, paramsNode);
			setterNode.addChild(bodyNode);
		}
		// FIXME If value is a function, drop the name value pair node and just add the function to the parent
		// JSObjectNode or JSStatementsNode?
		else if (propertyNode.getValue() instanceof FunctionNode)
		{
			JSNameValuePairNode pairNode = (JSNameValuePairNode) getCurrentNode();
			JSFunctionNode funcValue = (JSFunctionNode) pairNode.getValue();
			if (propertyNode.isStatic())
			{
				funcValue.setStatic();
			}
			IParseNode parent = pairNode.getParent();
			int numChildren = parent.getChildCount();
			parent.replaceChild(numChildren - 1, funcValue);
		}
		popNode();
		return super.leavePropertyNode(propertyNode);
	}

	@Override
	public boolean enterTryNode(TryNode tryNode)
	{
		addToParentAndPushNodeToStack(new JSTryNode(tryNode.getStart(), tryNode.getFinish() - 1));
		// if finally block is empty, push empty node for it. We rely on the fact that finally block would typically be
		// first visited child here. see leave for more
		if (tryNode.getFinallyBody() == null)
		{
			addChildToParent(new JSEmptyNode(tryNode.getBody().getFinish() - 1));
		}
		return super.enterTryNode(tryNode);
	}

	@Override
	public Node leaveTryNode(TryNode tryNode)
	{
		// Add empty catch
		if (tryNode.getCatches().isEmpty())
		{
			addChildToParent(new JSEmptyNode(tryNode.getBody().getFinish() - 1));
		}

		// Fix the ordering of the children!
		// this visits in order:
		// - finally body
		// - body
		// - catch blocks FIXME Do these get added as multiple children?
		JSTryNode ourTryNode = (JSTryNode) getCurrentNode();
		IParseNode[] children = ourTryNode.getChildren();
		IParseNode[] orderedChildren = new IParseNode[children.length];
		orderedChildren[0] = children[1];
		orderedChildren[1] = children[2];
		JSNode firstChild = (JSNode) children[0];
		// if finally block is not empty, wrap in JSFinallyNode
		if (!(firstChild instanceof JSEmptyNode))
		{
			firstChild = new JSFinallyNode(firstChild);
		}
		orderedChildren[2] = firstChild;
		ourTryNode.setChildren(orderedChildren);

		popNode();
		return super.leaveTryNode(tryNode);
	}

	@Override
	public boolean enterCatchNode(CatchNode catchNode)
	{
		addToParentAndPushNodeToStack(new JSCatchNode(catchNode.getStart(), catchNode.getFinish() - 1));
		return super.enterCatchNode(catchNode);
	}

	@Override
	public Node leaveCatchNode(CatchNode catchNode)
	{
		popNode();
		return super.leaveCatchNode(catchNode);
	}

	@Override
	public boolean enterSwitchNode(SwitchNode switchNode)
	{
		// lParen, rParen, lBrace, rBrace
		// FIXME Use findChar to get more accurate positions!
		int lParen = switchNode.getExpression().getStart() - 1;
		int rParen = switchNode.getExpression().getFinish();
		int lBrace = rParen + 2; // generate fake position for left brace. I don't think we can do any better than this!
		int rBrace = switchNode.getFinish();
		addToParentAndPushNodeToStack(new JSSwitchNode(switchNode.getStart(), switchNode.getFinish() - 1,
				toSymbol(JSTokenType.LPAREN, lParen), toSymbol(JSTokenType.RPAREN, rParen),
				toSymbol(JSTokenType.LCURLY, lBrace), toSymbol(JSTokenType.RCURLY, rBrace)));
		return super.enterSwitchNode(switchNode);
	}

	@Override
	public Node leaveSwitchNode(SwitchNode switchNode)
	{
		popNode();
		return super.leaveSwitchNode(switchNode);
	}

	@Override
	public boolean enterCaseNode(CaseNode caseNode)
	{
		int offset = caseNode.getFinish() + 1;
		Symbol colon = toSymbol(JSTokenType.COLON, offset);
		if (caseNode.getTest() != null)
		{
			addToParentAndPushNodeToStack(new JSCaseNode(caseNode.getStart(), caseNode.getFinish() - 1, colon));
		}
		else
		{
			addToParentAndPushNodeToStack(new JSDefaultNode(caseNode.getStart(), caseNode.getFinish() - 1, colon));
		}
		return super.enterCaseNode(caseNode);
	}

	@Override
	public Node leaveCaseNode(CaseNode caseNode)
	{
		popNode();
		return super.leaveCaseNode(caseNode);
	}

	@Override
	public boolean enterWhileNode(WhileNode whileNode)
	{
		int start = whileNode.getTest().getStart();
		int end = whileNode.getTest().getFinish() + 1;
		Symbol leftParen = toSymbol(JSTokenType.LPAREN, start);
		Symbol rightParen = toSymbol(JSTokenType.RPAREN, end);
		if (whileNode.isDoWhile())
		{
			addToParentAndPushNodeToStack(
					new JSDoNode(whileNode.getStart(), whileNode.getFinish() - 1, leftParen, rightParen));
		}
		else
		{

			addToParentAndPushNodeToStack(
					new JSWhileNode(whileNode.getStart(), whileNode.getFinish() - 1, leftParen, rightParen));
		}
		return super.enterWhileNode(whileNode);
	}

	@Override
	public Node leaveWhileNode(WhileNode whileNode)
	{
		popNode();
		return super.leaveWhileNode(whileNode);
	}

	@Override
	public boolean enterBreakNode(BreakNode breakNode)
	{
		String labelName = breakNode.getLabelName();
		JSBreakNode bn;
		if (labelName != null)
		{
			int start = breakNode.getFinish() + 2;
			int finish = start + labelName.length();
			bn = new JSBreakNode(breakNode.getStart(), breakNode.getFinish() - 1,
					toSymbol(JSTokenType.IDENTIFIER, start, finish));
		}
		else
		{
			bn = new JSBreakNode(breakNode.getStart(), breakNode.getFinish() - 1);
		}
		bn.setSemicolonIncluded(true);
		addToParentAndPushNodeToStack(bn);
		return super.enterBreakNode(breakNode);
	}

	@Override
	public Node leaveBreakNode(BreakNode breakNode)
	{
		popNode();
		return super.leaveBreakNode(breakNode);
	}

	@Override
	public boolean enterCallNode(CallNode callNode)
	{
		addToParentAndPushNodeToStack(new JSInvokeNode(callNode.getStart(), callNode.getFinish() - 1));
		// We need to visit the expression first, then push the arguments node...
		int lParen = findChar('(', callNode.getFunction().getFinish(), callNode.getFinish()); // FIXME: prefer start of first arg as last index to search!
		int rParen = findLastChar(')', callNode.getFinish());
		pushOnLeave.put(callNode.getFunction(), new JSArgumentsNode(lParen, rParen));
		return super.enterCallNode(callNode);
	}

	@Override
	public Node leaveCallNode(CallNode callNode)
	{
		popNode(); // arguments node
		popNode(); // invoke node
		return super.leaveCallNode(callNode);
	}

	@Override
	protected Node leaveDefault(Node node)
	{
		// System.out.println("Leaving node: " + node.getClass().getName() + ": " + node);
		if (pushOnLeave != null && pushOnLeave.containsKey(node))
		{
			JSNode toPush = pushOnLeave.remove(node);
			addToParentAndPushNodeToStack(toPush);
		}
		return super.leaveDefault(node);
	}

	@Override
	public boolean enterIfNode(IfNode ifNode)
	{
		// lParen, rParen
		int lParen = findChar('(', ifNode.getStart() + 2, ifNode.getTest().getStart());
		int rParen = findChar(')', ifNode.getTest().getFinish(), ifNode.getPass().getStart());
		addToParentAndPushNodeToStack(new JSIfNode(ifNode.getStart(), ifNode.getFinish() - 1,
				toSymbol(JSTokenType.LPAREN, lParen), toSymbol(JSTokenType.RPAREN, rParen)));
		return super.enterIfNode(ifNode);
	}

	@Override
	public Node leaveIfNode(IfNode ifNode)
	{
		Block elseBlock = ifNode.getFail();
		if (elseBlock == null)
		{
			JSIfNode jsIfNode = (JSIfNode) getCurrentNode();
			int offset = jsIfNode.getTrueBlock().getEndingOffset();
			jsIfNode.addChild(new JSEmptyNode(offset));
		}
		popNode();
		return super.leaveIfNode(ifNode);
	}

	@Override
	public boolean enterAccessNode(AccessNode accessNode)
	{
		addToParentAndPushNodeToStack(new JSGetPropertyNode(accessNode.getStart(), accessNode.getFinish() - 1,
				toSymbol(JSTokenType.DOT, accessNode.getBase().getFinish())));
		return super.enterAccessNode(accessNode);
	}

	@Override
	public Node leaveAccessNode(AccessNode accessNode)
	{
		// manually add the property as an identifier
		int finish = accessNode.getFinish();
		String propertyName = accessNode.getProperty();
		int start = finish - propertyName.length();
		addChildToParent(new JSIdentifierNode(identifierSymbol(start, finish, propertyName)));
		popNode();
		return super.leaveAccessNode(accessNode);
	}

	@Override
	public boolean enterIndexNode(IndexNode indexNode)
	{
		// FIXME Use findChar to get more accurate positions!
		int leftBracket = indexNode.getBase().getFinish();
		int rightBracket = indexNode.getFinish() - 1;
		addToParentAndPushNodeToStack(new JSGetElementNode(indexNode.getStart(), indexNode.getFinish() - 1,
				toSymbol(JSTokenType.LBRACKET, leftBracket), toSymbol(JSTokenType.RBRACKET, rightBracket)));
		return super.enterIndexNode(indexNode);
	}

	@Override
	public Node leaveIndexNode(IndexNode indexNode)
	{
		popNode();
		return super.leaveIndexNode(indexNode);
	}

	@Override
	public boolean enterTernaryNode(TernaryNode ternaryNode)
	{
		// colon is between these two...
		int possibleColonStart = ternaryNode.getTrueExpression().getFinish(); // inclusive
		int possibleColonEnd = ternaryNode.getFalseExpression().getStart(); // not inclusive
		int colon = findChar(':', possibleColonStart, possibleColonEnd);

		// question is between these two
		int possibleQuestionStart = ternaryNode.getTest().getFinish(); // inclusive
		int possibleQuestionEnd = ternaryNode.getTrueExpression().getStart(); // not inclusive
		int question = findChar('?', possibleQuestionStart, possibleQuestionEnd);

		addToParentAndPushNodeToStack(new JSConditionalNode(ternaryNode.getStart(), ternaryNode.getFinish() - 1,
				toSymbol(JSTokenType.QUESTION, question), toSymbol(JSTokenType.COLON, colon)));
		return super.enterTernaryNode(ternaryNode);
	}

	/**
	 * Searches the source code for a specific characters index in a range. Returns -1 if not found. Returns the
	 * absolute index if found.
	 * 
	 * @param c
	 *            character to find
	 * @param startInclusive
	 *            start index
	 * @param endNonInclusive
	 *            end index (non-inclusive, we look up to the position, but not at this position)
	 * @return
	 */
	private int findChar(char c, int startInclusive, int endNonInclusive)
	{
		// FIXME Be smarter about possible comments! If we're inside a comment, don't return the position, ignore and
		// move on!
		int index = source.substring(startInclusive, endNonInclusive).indexOf(c);
		if (index == -1)
		{
			return -1;
		}
		return index + startInclusive;
	}

	private int findChar(char c, int from)
	{
		return source.indexOf(c, from);
	}

	private int findLastChar(char c, int from)
	{
		return source.lastIndexOf(c, from);
	}

	@Override
	public Node leaveTernaryNode(TernaryNode ternaryNode)
	{
		popNode();
		return super.leaveTernaryNode(ternaryNode);
	}

	@Override
	public boolean enterContinueNode(ContinueNode continueNode)
	{
		JSContinueNode cn = new JSContinueNode(continueNode.getStart(), continueNode.getFinish() - 1);
		String label = continueNode.getLabelName();
		if (label != null)
		{
			int start = continueNode.getFinish();
			int finish = start + label.length();
			cn = new JSContinueNode(continueNode.getStart(), continueNode.getFinish() - 1,
					toSymbol(JSTokenType.IDENTIFIER, start, finish, label));
		}
		cn.setSemicolonIncluded(true);
		addToParentAndPushNodeToStack(cn);
		return super.enterContinueNode(continueNode);
	}

	@Override
	public Node leaveContinueNode(ContinueNode continueNode)
	{
		popNode();
		return super.leaveContinueNode(continueNode);
	}

	@Override
	public boolean enterThrowNode(ThrowNode throwNode)
	{
		JSNode tn = new JSThrowNode(throwNode.getStart(), throwNode.getFinish() - 1);
		tn.setSemicolonIncluded(true);
		addToParentAndPushNodeToStack(tn);
		return super.enterThrowNode(throwNode);
	}

	@Override
	public Node leaveThrowNode(ThrowNode throwNode)
	{
		popNode();
		return super.leaveThrowNode(throwNode);
	}

	@Override
	public boolean enterLabelNode(LabelNode labelNode)
	{
		// TODO: Find colon!
		addToParentAndPushNodeToStack(
				new JSLabelledNode(new JSIdentifierNode(identifierSymbol(labelNode, labelNode.getLabelName())), null));
		return super.enterLabelNode(labelNode);
	}

	@Override
	public Node leaveLabelNode(LabelNode labelNode)
	{
		popNode();
		return super.leaveLabelNode(labelNode);
	}

	// @Override
	// protected boolean enterDefault(Node node)
	// {
	// System.out.println("Entering node: " + node.getClass().getName() + ": " + node);
	// return super.enterDefault(node);
	// }

	@Override
	public boolean enterWithNode(WithNode withNode)
	{
		// lParen, rParen
		// FIXME Use findChar to get more accurate positions!
		int lParen = withNode.getExpression().getStart() - 1;
		int rParen = withNode.getExpression().getFinish();
		addToParentAndPushNodeToStack(new JSWithNode(withNode.getStart(), withNode.getFinish() - 1,
				toSymbol(JSTokenType.LPAREN, lParen), toSymbol(JSTokenType.RPAREN, rParen)));
		return super.enterWithNode(withNode);
	}

	@Override
	public Node leaveWithNode(WithNode withNode)
	{
		popNode();
		return super.leaveWithNode(withNode);
	}

	public IParseRootNode getRootNode()
	{
		return fRootNode;
	}

	private void addChildToParent(JSNode node)
	{
		IParseNode parent = getCurrentNode();
		if (parent != null)
		{
			parent.addChild(node);
		}
	}

	private IParseNode popNode()
	{
		return fNodeStack.pop();
	}

	private IParseNode getLastNode()
	{
		IParseNode currentNode = getCurrentNode();
		if (currentNode == null)
		{
			return null;
		}
		return currentNode.getLastChild();
	}

	private IParseNode getCurrentNode()
	{
		if (fNodeStack.isEmpty())
		{
			return null;
		}
		return fNodeStack.peek();
	}

	private void addToParentAndPushNodeToStack(JSNode node)
	{
		addChildToParent(node);
		fNodeStack.push(node);
	}

	private ExportedStatus getExportStatus(IdentNode ident)
	{
		if (module != null)
		{
			List<ExportEntry> exports = module.getLocalExportEntries();
			for (ExportEntry entry : exports)
			{
				if (entry.getLocalName().equals(ident.getName()))
				{
					return new ExportedStatus(true, entry.getExportName().equals(Module.DEFAULT_NAME));
				}
			}
		}
		return new ExportedStatus(false, false);
	}

	private static class ExportedStatus
	{
		final boolean isExported;
		final boolean isDefault;

		public ExportedStatus(boolean isExported, boolean isDefault)
		{
			this.isExported = isExported;
			this.isDefault = isDefault;
		}
	}
}
