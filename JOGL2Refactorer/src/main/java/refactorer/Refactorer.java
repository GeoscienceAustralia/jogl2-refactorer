/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package refactorer;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import refactorer.LocalVariables.LocalVariable;

/**
 * Class used to refactor the World Wind Java SDK from the obsolete JOGL1 to
 * JOGAMP's JOGL2.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Refactorer
{
	private final String[] classpath;
	private final String[] sources;

	private CompilationUnit unit;
	private LocalVariables variables;

	public Refactorer(String[] classpath, String[] sources)
	{
		this.classpath = classpath;
		this.sources = sources;
	}

	public String refactor(String source, String unitName)
	{
		try
		{
			Document document = new Document(source);
			ASTParser parser = ASTParser.newParser(AST.JLS4);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setStatementsRecovery(true);

			@SuppressWarnings("unchecked")
			Hashtable<String, String> options = JavaCore.getDefaultOptions();
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
			parser.setCompilerOptions(options);

			parser.setEnvironment(classpath, sources, null, true);

			parser.setSource(document.get().toCharArray());
			parser.setUnitName(unitName);

			unit = (CompilationUnit) parser.createAST(null);
			unit.recordModifications();
			variables = new LocalVariables();


			/*IProblem[] problems = unit.getProblems();
			if (problems != null && problems.length > 0)
			{
				System.out.println("Got " + problems.length + " problems compiling the source file: ");
				for (IProblem problem : problems)
				{
					System.out.println(problem);
				}
			}*/

			fixImports();

			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> types = unit.types();
			for (AbstractTypeDeclaration type : types)
			{
				processTypeDeclaration(type);
			}

			//apply changes to document
			TextEdit edits = unit.rewrite(document, null);
			edits.apply(document);

			return document.get();
		}
		catch (Exception e)
		{
			System.err.println("Error processing unit: " + unitName);
			e.printStackTrace();
			return null;
		}
	}

	protected void fixImports()
	{


		@SuppressWarnings("unchecked")
		List<ImportDeclaration> imports = unit.imports();
		AST ast = unit.getAST();
		for (ImportDeclaration importDeclaration : imports)
		{
			String name = importDeclaration.getName().getFullyQualifiedName();
			if (name.startsWith("com.sun.opengl.util.texture"))
			{
				name = "com.jogamp.opengl.util.texture" + name.substring("com.sun.opengl.util.texture".length());
				importDeclaration.setName(ast.newName(name));
			}
			else if ("javax.media.opengl.GL".equals(name))
			{
				importDeclaration.setName(ast.newName("javax.media.opengl.GL2"));
			}
			else if ("com.sun.opengl.util.j2d.TextRenderer".equals(name))
			{
				importDeclaration.setName(ast.newName("com.jogamp.opengl.util.awt.TextRenderer"));
			}
			else if ("com.sun.opengl.util.j2d".equals(name) && importDeclaration.isOnDemand())
			{
				importDeclaration.setName(ast.newName("com.jogamp.opengl.util.awt"));
			}
			else if ("com.sun.opengl.util.BufferUtil".equals(name))
			{
				importDeclaration.setName(ast.newName("com.jogamp.common.nio.Buffers"));
			}
			else if ("com.sun.opengl.util".equals(name) && importDeclaration.isOnDemand())
			{
				//TODO don't like this, could hide other imports
				importDeclaration.setOnDemand(false);
				importDeclaration.setName(ast.newName("com.jogamp.common.nio.Buffers"));
			}
			else if ("com.sun.opengl.util.FPSAnimator".equals(name))
			{
				importDeclaration.setName(ast.newName("com.jogamp.opengl.util.FPSAnimator"));
			}
			else if ("javax.media.opengl.GLCanvas".equals(name))
			{
				importDeclaration.setName(ast.newName("javax.media.opengl.awt.GLCanvas"));
			}
			else if ("javax.media.opengl.GLJPanel".equals(name))
			{
				importDeclaration.setName(ast.newName("javax.media.opengl.awt.GLJPanel"));
			}
			else if ("com.sun.opengl.util.Screenshot".equals(name))
			{
				importDeclaration.setName(ast.newName("com.jogamp.opengl.util.awt.Screenshot"));
			}
			else if ("com.sun.opengl.impl.packrect".equals(name))
			{
				importDeclaration.setName(ast.newName("com.jogamp.opengl.util.packrect"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void addImportIfRequired(String qualifiedName)
	{
		int lastIndexOfDot = qualifiedName.lastIndexOf('.');
		String packageName = qualifiedName.substring(0, lastIndexOfDot);

		List<ImportDeclaration> imports = unit.imports();
		for (ImportDeclaration importDeclaration : imports)
		{
			String name = importDeclaration.getName().getFullyQualifiedName();
			if (qualifiedName.equals(name) || (packageName.equals(name) && importDeclaration.isOnDemand()))
			{
				return;
			}
		}

		ImportDeclaration newImport = unit.getAST().newImportDeclaration();
		newImport.setName(unit.getAST().newName(qualifiedName));
		unit.imports().add(newImport);
	}

	@SuppressWarnings("unchecked")
	protected void processTypeDeclaration(AbstractTypeDeclaration type)
	{
		if (type == null)
			return;

		List<BodyDeclaration> bodies = type.bodyDeclarations();
		for (BodyDeclaration body : bodies)
		{
			processBodyDeclaration(body);
		}

		if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
		{
			TypeDeclaration typeDeclaration = (TypeDeclaration) type;

			if (typeDeclaration.getSuperclassType() != null)
			{
				ITypeBinding superclass = typeDeclaration.getSuperclassType().resolveBinding();
				if (superclass != null)
				{
					if ("javax.media.opengl.GLCanvas".equals(superclass.getQualifiedName()))
					{
						addImportIfRequired("javax.media.opengl.awt.GLCanvas");
					}
					else if ("javax.media.opengl.GLJPanel".equals(superclass.getQualifiedName()))
					{
						addImportIfRequired("javax.media.opengl.awt.GLJPanel");
					}
				}
			}

			for (Type interfaceType : (List<Type>) typeDeclaration.superInterfaceTypes())
			{
				ITypeBinding binding = interfaceType.resolveBinding();
				if (binding != null)
				{
					String interfaceName = binding.getQualifiedName();
					if ("javax.media.opengl.GLEventListener".equals(interfaceName))
					{
						for (int i = bodies.size() - 1; i >= 0; i--)
						{
							BodyDeclaration body = bodies.get(i);
							if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
							{
								MethodDeclaration methodDeclaration = (MethodDeclaration) body;
								if ("displayChanged".equals(methodDeclaration.getName().getFullyQualifiedName()))
								{
									bodies.remove(i);
								}
							}
						}

						AST ast = typeDeclaration.getAST();
						MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
						methodDeclaration.setName(ast.newSimpleName("dispose"));
						methodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
						methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
						Block block = ast.newBlock();
						methodDeclaration.setBody(block);
						SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
						parameter.setName(ast.newSimpleName("glDrawable"));
						parameter.setType(ast.newSimpleType(ast.newName("GLAutoDrawable")));
						methodDeclaration.parameters().add(parameter);
						bodies.add(methodDeclaration);

						if ("performance.VBORenderer.GLDisplay.MyGLEventListener".equals(type.resolveBinding()
								.getQualifiedName()))
						{
							ForStatement forStatement = ast.newForStatement();

							VariableDeclarationFragment forInitializerFragment = ast.newVariableDeclarationFragment();
							forInitializerFragment.setName(ast.newSimpleName("i"));
							forInitializerFragment.setInitializer(ast.newNumberLiteral("0"));
							VariableDeclarationExpression forInitializer =
									ast.newVariableDeclarationExpression(forInitializerFragment);
							forInitializer.setType(ast.newPrimitiveType(PrimitiveType.INT));
							forStatement.initializers().add(forInitializer);

							InfixExpression forExpression = ast.newInfixExpression();
							forExpression.setLeftOperand(ast.newSimpleName("i"));
							forExpression.setOperator(InfixExpression.Operator.LESS);
							MethodInvocation rightOperand = ast.newMethodInvocation();
							rightOperand.setExpression(ast.newSimpleName("eventListeners"));
							rightOperand.setName(ast.newSimpleName("size"));
							forExpression.setRightOperand(rightOperand);
							forStatement.setExpression(forExpression);

							PostfixExpression forUpdater = ast.newPostfixExpression();
							forUpdater.setOperand(ast.newSimpleName("i"));
							forUpdater.setOperator(PostfixExpression.Operator.INCREMENT);
							forStatement.updaters().add(forUpdater);

							Block forBlock = ast.newBlock();
							MethodInvocation mi1 = ast.newMethodInvocation();
							mi1.setName(ast.newSimpleName("dispose"));
							mi1.arguments().add(ast.newName("glDrawable"));
							CastExpression castExpression = ast.newCastExpression();
							castExpression.setType(ast.newSimpleType(ast.newName("GLEventListener")));
							MethodInvocation mi2 = ast.newMethodInvocation();
							mi2.setExpression(ast.newName("eventListeners"));
							mi2.setName(ast.newSimpleName("get"));
							mi2.arguments().add(ast.newName("i"));
							castExpression.setExpression(mi2);
							ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
							parenthesizedExpression.setExpression(castExpression);
							mi1.setExpression(parenthesizedExpression);
							forBlock.statements().add(ast.newExpressionStatement(mi1));
							forStatement.setBody(forBlock);

							block.statements().add(forStatement);
						}
					}
					else if ("com.sun.opengl.impl.packrect.BackingStoreManager".equals(interfaceName))
					{
						AST ast = typeDeclaration.getAST();
						MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();
						newMethodDeclaration.setName(ast.newSimpleName("canCompact"));
						newMethodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
						newMethodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
						Block block = ast.newBlock();
						ReturnStatement returnStatement = ast.newReturnStatement();
						returnStatement.setExpression(ast.newBooleanLiteral(false));
						block.statements().add(returnStatement);
						newMethodDeclaration.setBody(block);
						bodies.add(newMethodDeclaration);

						if ("gov.nasa.worldwind.util.TextureAtlas.AtlasBackingStore".equals(type.resolveBinding()
								.getQualifiedName()))
						{
							for (BodyDeclaration body : (List<BodyDeclaration>) typeDeclaration.bodyDeclarations())
							{
								if (body.getNodeType() == ASTNode.METHOD_DECLARATION)
								{
									MethodDeclaration methodDeclaration = (MethodDeclaration) body;
									if ("additionFailed".equals(methodDeclaration.getName().getFullyQualifiedName()))
									{
										methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
										Block body2 = methodDeclaration.getBody();
										if (!body2.statements().isEmpty())
										{
											Statement firstStatement = (Statement) body2.statements().get(0);
											if (firstStatement.getNodeType() == ASTNode.IF_STATEMENT)
											{
												IfStatement ifStatement = (IfStatement) firstStatement;
												Expression expression = ifStatement.getExpression();
												ifStatement.setExpression(ast.newBooleanLiteral(true));
												ReturnStatement newReturnStatement = ast.newReturnStatement();
												newReturnStatement.setExpression(expression);
												body2.statements().clear();
												body2.statements().add(newReturnStatement);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected void processBodyDeclaration(BodyDeclaration body)
	{
		variables.push();
		switch (body.getNodeType())
		{
		//case ASTNode.Constructor_Declaration:
		case ASTNode.ENUM_CONSTANT_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
		case ASTNode.INITIALIZER:
			//ignore
			break;
		case ASTNode.FIELD_DECLARATION:
			FieldDeclaration fieldDeclaration = (FieldDeclaration) body;
			processFieldDeclaration(fieldDeclaration);
			break;
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration methodDeclaration = (MethodDeclaration) body;
			processMethodDeclaration(methodDeclaration);
			break;
		case ASTNode.TYPE_DECLARATION:
		case ASTNode.ENUM_DECLARATION:
			AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) body;
			processTypeDeclaration(abstractTypeDeclaration);
			break;
		}
		variables.pop();
	}

	@SuppressWarnings("unchecked")
	protected void processMethodDeclaration(MethodDeclaration methodDeclaration)
	{
		Type returnType = methodDeclaration.getReturnType2();
		if (returnType != null)
		{
			ITypeBinding binding = returnType.resolveBinding();
			if (binding != null && binding.getQualifiedName().equals("javax.media.opengl.GL"))
			{
				AST ast = methodDeclaration.getAST();
				SimpleType newType = ast.newSimpleType(ast.newName("GL2"));
				methodDeclaration.setReturnType2(newType);
				addImportIfRequired("javax.media.opengl.GL2");
			}
		}
		for (SingleVariableDeclaration singleVariableDeclaration : (List<SingleVariableDeclaration>) methodDeclaration
				.parameters())
		{
			processSingleVariableDeclaration(singleVariableDeclaration);
		}
		processStatement(methodDeclaration.getBody());
	}

	@SuppressWarnings("unchecked")
	protected void processStatement(Statement statement)
	{
		if (statement == null)
			return;

		switch (statement.getNodeType())
		{
		case ASTNode.ASSERT_STATEMENT:
		case ASTNode.BREAK_STATEMENT:
		case ASTNode.CONTINUE_STATEMENT:
		case ASTNode.EMPTY_STATEMENT:
		case ASTNode.LABELED_STATEMENT:
			//ignore
			break;
		case ASTNode.BLOCK:
			variables.push();
			Block block = (Block) statement;
			for (Statement s : (List<Statement>) block.statements())
			{
				processStatement(s);
			}
			variables.pop();
			break;
		case ASTNode.CONSTRUCTOR_INVOCATION:
			ConstructorInvocation constructorInvocation = (ConstructorInvocation) statement;
			for (Expression e : (List<Expression>) constructorInvocation.arguments())
			{
				processExpression(e);
			}
			break;
		case ASTNode.DO_STATEMENT:
			DoStatement doStatement = (DoStatement) statement;
			processExpression(doStatement.getExpression());
			processStatement(doStatement.getBody());
			break;
		case ASTNode.ENHANCED_FOR_STATEMENT:
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
			processSingleVariableDeclaration(enhancedForStatement.getParameter());
			processExpression(enhancedForStatement.getExpression());
			processStatement(enhancedForStatement.getBody());
			break;
		case ASTNode.EXPRESSION_STATEMENT:
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			processExpression(expressionStatement.getExpression());
			break;
		case ASTNode.FOR_STATEMENT:
			ForStatement forStatement = (ForStatement) statement;
			for (Expression e : (List<Expression>) forStatement.initializers())
			{
				processExpression(e);
			}
			processExpression(forStatement.getExpression());
			for (Expression e : (List<Expression>) forStatement.updaters())
			{
				processExpression(e);
			}
			processStatement(forStatement.getBody());
			break;
		case ASTNode.IF_STATEMENT:
			IfStatement ifStatement = (IfStatement) statement;
			processExpression(ifStatement.getExpression());
			processStatement(ifStatement.getThenStatement());
			processStatement(ifStatement.getElseStatement());
			break;
		case ASTNode.RETURN_STATEMENT:
			ReturnStatement returnStatement = (ReturnStatement) statement;
			processExpression(returnStatement.getExpression());
			break;
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) statement;
			processExpression(superConstructorInvocation.getExpression());
			for (Expression e : (List<Expression>) superConstructorInvocation.arguments())
			{
				processExpression(e);
			}
			break;
		case ASTNode.SWITCH_CASE:
			SwitchCase switchCase = (SwitchCase) statement;
			processExpression(switchCase.getExpression());
			break;
		case ASTNode.SWITCH_STATEMENT:
			SwitchStatement switchStatement = (SwitchStatement) statement;
			processExpression(switchStatement.getExpression());
			for (Statement s : (List<Statement>) switchStatement.statements())
			{
				processStatement(s);
			}
			break;
		case ASTNode.SYNCHRONIZED_STATEMENT:
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement) statement;
			processExpression(synchronizedStatement.getExpression());
			processStatement(synchronizedStatement.getBody());
			break;
		case ASTNode.THROW_STATEMENT:
			ThrowStatement throwStatement = (ThrowStatement) statement;
			processExpression(throwStatement.getExpression());
			break;
		case ASTNode.TRY_STATEMENT:
			TryStatement tryStatement = (TryStatement) statement;
			processStatement(tryStatement.getBody());
			for (CatchClause cc : (List<CatchClause>) tryStatement.catchClauses())
			{
				processStatement(cc.getBody());
			}
			processStatement(tryStatement.getFinally());
			break;
		case ASTNode.TYPE_DECLARATION_STATEMENT:
			TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) statement;
			processTypeDeclaration(typeDeclarationStatement.getDeclaration());
			break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			processVariableDeclarationStatement(variableDeclarationStatement);
			break;
		case ASTNode.WHILE_STATEMENT:
			WhileStatement whileStatement = (WhileStatement) statement;
			processExpression(whileStatement.getExpression());
			processStatement(whileStatement.getBody());
			break;
		}
	}

	@SuppressWarnings("unchecked")
	protected void processExpression(Expression expression)
	{
		if (expression == null)
			return;

		switch (expression.getNodeType())
		{
		case ASTNode.ARRAY_ACCESS:
		case ASTNode.BOOLEAN_LITERAL:
		case ASTNode.CAST_EXPRESSION:
		case ASTNode.CHARACTER_LITERAL:
		case ASTNode.FIELD_ACCESS:
		case ASTNode.INSTANCEOF_EXPRESSION:
		case ASTNode.NULL_LITERAL:
		case ASTNode.NUMBER_LITERAL:
		case ASTNode.STRING_LITERAL:
		case ASTNode.SUPER_FIELD_ACCESS:
		case ASTNode.SUPER_METHOD_INVOCATION:
		case ASTNode.THIS_EXPRESSION:
		case ASTNode.TYPE_LITERAL:
		case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
			//ignore
			break;
		case ASTNode.ARRAY_CREATION:
			ArrayCreation arrayCreation = (ArrayCreation) expression;
			processExpression(arrayCreation.getInitializer());
			break;
		case ASTNode.ARRAY_INITIALIZER:
			ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
			for (Expression e : (List<Expression>) arrayInitializer.expressions())
			{
				processExpression(e);
			}
			break;
		case ASTNode.ASSIGNMENT:
			Assignment assignment = (Assignment) expression;
			processExpression(assignment.getLeftHandSide());
			processExpression(assignment.getRightHandSide());
			break;
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression infixExpression = (InfixExpression) expression;
			processExpression(infixExpression.getLeftOperand());
			processExpression(infixExpression.getRightOperand());
			for (Expression e : (List<Expression>) infixExpression.extendedOperands())
			{
				processExpression(e);
			}
			break;
		case ASTNode.PREFIX_EXPRESSION:
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			processExpression(prefixExpression.getOperand());
			break;
		case ASTNode.POSTFIX_EXPRESSION:
			PostfixExpression postfixExpression = (PostfixExpression) expression;
			processExpression(postfixExpression.getOperand());
			break;
		case ASTNode.PARENTHESIZED_EXPRESSION:
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			processExpression(parenthesizedExpression.getExpression());
			break;
		case ASTNode.CLASS_INSTANCE_CREATION:
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
			processClassInstanceCreation(classInstanceCreation);
			break;
		case ASTNode.CONDITIONAL_EXPRESSION:
			ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
			processExpression(conditionalExpression.getExpression());
			processExpression(conditionalExpression.getThenExpression());
			processExpression(conditionalExpression.getElseExpression());
			break;
		case ASTNode.METHOD_INVOCATION:
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			processMethodInvocation(methodInvocation);
			break;
		case ASTNode.QUALIFIED_NAME:
		case ASTNode.SIMPLE_NAME:
			Name name = (Name) expression;
			processName(name);
			break;
		}

		//System.out.println(expression.getNodeType() + " = " + expression);
	}

	@SuppressWarnings("unchecked")
	protected void processClassInstanceCreation(ClassInstanceCreation classInstanceCreation)
	{
		ITypeBinding type = classInstanceCreation.resolveTypeBinding();
		while (type != null)
		{
			if (type.getQualifiedName().equals("javax.media.opengl.GLCapabilities"))
			{
				AST ast = classInstanceCreation.getAST();
				MethodInvocation methodInvocation = ast.newMethodInvocation();
				methodInvocation.setExpression(ast.newSimpleName("GLProfile"));
				methodInvocation.setName(ast.newSimpleName("get"));
				methodInvocation.arguments().add(
						ast.newQualifiedName(ast.newName("GLProfile"), ast.newSimpleName("GL2")));
				classInstanceCreation.arguments().add(methodInvocation);
				addImportIfRequired("javax.media.opengl.GLProfile");
			}
			else if (type.getQualifiedName().equals("com.sun.opengl.util.texture.TextureData"))
			{
				AST ast = classInstanceCreation.getAST();
				MethodInvocation methodInvocation = ast.newMethodInvocation();
				methodInvocation.setExpression(ast.newSimpleName("GLProfile"));
				methodInvocation.setName(ast.newSimpleName("get"));
				methodInvocation.arguments().add(
						ast.newQualifiedName(ast.newName("GLProfile"), ast.newSimpleName("GL2")));
				classInstanceCreation.arguments().add(0, methodInvocation);
				addImportIfRequired("javax.media.opengl.GLProfile");
			}
			type = type.getSuperclass();
		}

		for (Expression e : (List<Expression>) classInstanceCreation.arguments())
		{
			processExpression(e);
		}

		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		if (anonymousClassDeclaration != null)
		{
			processAnonymousClassDeclaration(anonymousClassDeclaration);
		}
	}

	@SuppressWarnings("unchecked")
	protected void processAnonymousClassDeclaration(AnonymousClassDeclaration anonymousClassDeclaration)
	{
		for (BodyDeclaration body : (List<BodyDeclaration>) anonymousClassDeclaration.bodyDeclarations())
		{
			processBodyDeclaration(body);
		}
	}

	protected void processName(Name name)
	{
		switch (name.getNodeType())
		{
		case ASTNode.SIMPLE_NAME:
			break;
		case ASTNode.QUALIFIED_NAME:
			QualifiedName qualifiedName = (QualifiedName) name;
			ITypeBinding typeBinding = qualifiedName.getQualifier().resolveTypeBinding();
			if (typeBinding != null)
			{
				AST ast = qualifiedName.getAST();
				String typeName = typeBinding.getQualifiedName();
				if ("javax.media.opengl.GL".equals(typeName))
				{
					qualifiedName.setQualifier(ast.newSimpleName("GL2"));
					addImportIfRequired("javax.media.opengl.GL2");

					String constantName = qualifiedName.getName().getFullyQualifiedName();
					if (constantName.endsWith("_EXT") || constantName.endsWith("_ARB"))
					{
						//GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT no longer exists, replace with int literal
						if ("GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT".equals(constantName))
						{
							if (name.getParent().getNodeType() == ASTNode.SWITCH_CASE)
							{
								SwitchCase switchCase = (SwitchCase) name.getParent();
								switchCase.setExpression(ast.newNumberLiteral("0x8CD8"));
							}
						}
						else if (!"GL_TEXTURE_MAX_ANISOTROPY_EXT".equals(constantName)
								&& !"GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT".equals(constantName)
								&& !"GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_EXT".equals(constantName)
								&& !"GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT".equals(constantName))
						{
							qualifiedName
									.setName(ast.newSimpleName(constantName.substring(0, constantName.length() - 4)));
						}
					}
				}
				else if ("com.sun.opengl.util.BufferUtil".equals(typeName))
				{
					qualifiedName.setQualifier(ast.newSimpleName("Buffers"));
				}
			}
			break;
		}
	}

	@SuppressWarnings("unchecked")
	protected void processMethodInvocation(MethodInvocation methodInvocation)
	{
		if (methodInvocation == null)
			return;

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression != null)
		{
			ITypeBinding type = methodExpression.resolveTypeBinding();
			String methodName = methodInvocation.getName().getFullyQualifiedName();
			if (type != null && methodName != null)
			{
				AST ast = methodExpression.getAST();
				String typeName = type.getQualifiedName();
				if (("javax.media.opengl.GLContext".equals(typeName) || "javax.media.opengl.GLAutoDrawable".equals(type
						.getQualifiedName())) && "getGL".equals(methodName))
				{
					Expression newExpression = ast.newName("IGNORED");
					newExpression =
							(Expression) ASTNode.copySubtree(newExpression.getAST(), methodInvocation.getExpression());
					MethodInvocation newInvocation = ast.newMethodInvocation();
					newInvocation.setExpression(newExpression);
					newInvocation.setName(ast.newSimpleName("getGL"));
					methodInvocation.setExpression(newInvocation);
					methodInvocation.setName(ast.newSimpleName("getGL2"));
				}
				else if ("javax.media.opengl.GL".equals(typeName))
				{
					if (methodName.endsWith("EXT") || methodName.endsWith("ARB"))
					{
						methodInvocation.setName(ast.newSimpleName(methodName.substring(0, methodName.length() - 3)));
					}
				}
				else if ("com.sun.opengl.util.BufferUtil".equals(typeName))
				{
					methodInvocation.setExpression(ast.newSimpleName("Buffers"));
					if ("newByteBuffer".equals(methodName))
					{
						methodInvocation.setName(ast.newSimpleName("newDirectByteBuffer"));
					}
					else if ("newIntBuffer".equals(methodName))
					{
						methodInvocation.setName(ast.newSimpleName("newDirectIntBuffer"));
					}
					else if ("newFloatBuffer".equals(methodName))
					{
						methodInvocation.setName(ast.newSimpleName("newDirectFloatBuffer"));
					}
					else if ("newDoubleBuffer".equals(methodName))
					{
						methodInvocation.setName(ast.newSimpleName("newDirectDoubleBuffer"));
					}
				}
				else if ("com.sun.opengl.util.texture.TextureIO".equals(typeName))
				{
					boolean newTexture = "newTexture".equals(methodName);
					boolean newTextureData = "newTextureData".equals(methodName);
					if (newTexture || newTextureData)
					{
						boolean addGLProfile = newTextureData;
						if (methodInvocation.arguments().size() == 2)
						{
							Expression secondArgument = (Expression) methodInvocation.arguments().get(0);
							if ("java.awt.image.BufferedImage".equals(secondArgument.resolveTypeBinding()
									.getQualifiedName()))
							{
								methodInvocation.setExpression(ast.newSimpleName("AWTTextureIO"));
								addImportIfRequired("com.jogamp.opengl.util.texture.awt.AWTTextureIO");
								addGLProfile = true;
							}
						}

						if (addGLProfile)
						{
							MethodInvocation newMethodInvocation = ast.newMethodInvocation();
							newMethodInvocation.setExpression(ast.newSimpleName("GLProfile"));
							newMethodInvocation.setName(ast.newSimpleName("get"));
							newMethodInvocation.arguments().add(
									ast.newQualifiedName(ast.newName("GLProfile"), ast.newSimpleName("GL2")));
							methodInvocation.arguments().add(0, newMethodInvocation);
							addImportIfRequired("javax.media.opengl.GLProfile");
						}
					}
				}
				else if ("com.sun.opengl.util.texture.Texture".equals(typeName)
						&& ("bind".equals(methodName) || "getTextureObject".equals(methodName)
								|| "setTexParameteri".equals(methodName) || "dispose".equals(methodName)
								|| "enable".equals(methodName) || "disable".equals(methodName)
								|| "updateSubImage".equals(methodName) || "updateImage".equals(methodName)))
				{
					List<LocalVariable> locals = variables.getLocalVariablesMatchingType("javax.media.opengl.GL");
					if (!locals.isEmpty())
					{
						Name name = ast.newName(locals.get(0).name);
						methodInvocation.arguments().add(name);
					}
					else
					{
						locals = variables.getLocalVariablesMatchingType("gov.nasa.worldwind.render.DrawContext");
						if (!locals.isEmpty())
						{
							MethodInvocation newMethodInvocation = ast.newMethodInvocation();
							newMethodInvocation.setExpression(ast.newSimpleName(locals.get(0).name));
							newMethodInvocation.setName(ast.newSimpleName("getGL"));
							methodInvocation.arguments().add(0, newMethodInvocation);
						}
						else
						{
							//GLContext.getCurrent().getGL().getGL2()
							MethodInvocation mi1 = ast.newMethodInvocation();
							mi1.setExpression(ast.newName("GLContext"));
							mi1.setName(ast.newSimpleName("getCurrent"));

							MethodInvocation mi2 = ast.newMethodInvocation();
							mi2.setExpression(mi1);
							mi2.setName(ast.newSimpleName("getGL"));

							MethodInvocation mi3 = ast.newMethodInvocation();
							mi3.setExpression(mi2);
							mi3.setName(ast.newSimpleName("getGL2"));

							methodInvocation.arguments().add(0, mi3);

							addImportIfRequired("javax.media.opengl.GLContext");
						}
					}

					if ("dispose".equals(methodName))
					{
						methodInvocation.setName(ast.newSimpleName("destroy"));
					}
				}
				else if ("javax.media.opengl.GLAutoDrawable".equals(typeName) && "repaint".equals(methodName))
				{
					ASTNode parent = methodInvocation.getParent().getParent();
					if (parent.getNodeType() == ASTNode.BLOCK || parent.getNodeType() == ASTNode.IF_STATEMENT)
					{
						IfStatement ifStatement = ast.newIfStatement();
						InstanceofExpression instanceofExpression = ast.newInstanceofExpression();
						ifStatement.setExpression(instanceofExpression);

						Expression leftOperation = ast.newName("IGNORED");
						leftOperation =
								(Expression) ASTNode.copySubtree(leftOperation.getAST(),
										methodInvocation.getExpression());
						instanceofExpression.setLeftOperand(leftOperation);
						instanceofExpression.setRightOperand(ast.newSimpleType(ast.newName("Component")));

						Expression castExpressionExpression = ast.newName("IGNORED");
						castExpressionExpression =
								(Expression) ASTNode.copySubtree(castExpressionExpression.getAST(),
										methodInvocation.getExpression());

						CastExpression castExpression = ast.newCastExpression();
						castExpression.setExpression(castExpressionExpression);
						castExpression.setType(ast.newSimpleType(ast.newName("Component")));

						ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
						parenthesizedExpression.setExpression(castExpression);
						methodInvocation.setExpression(parenthesizedExpression);

						if (parent.getNodeType() == ASTNode.BLOCK)
						{
							Block parentBlock = (Block) parent;
							parentBlock.statements().remove(methodInvocation.getParent());
							parentBlock.statements().add(ifStatement);
						}
						else if (parent.getNodeType() == ASTNode.IF_STATEMENT)
						{
							IfStatement parentIfStatement = (IfStatement) parent;
							if (parentIfStatement.getThenStatement() == methodInvocation.getParent())
							{
								parentIfStatement.setThenStatement(ifStatement);
							}
							else if (parentIfStatement.getElseStatement() == methodInvocation.getParent())
							{
								parentIfStatement.setElseStatement(ifStatement);
							}
						}
						ifStatement.setThenStatement((Statement) methodInvocation.getParent());

						addImportIfRequired("java.awt.Component");
					}
				}
				else if ("com.sun.opengl.util.Screenshot".equals(typeName))
				{
					methodInvocation.setExpression(ast.newSimpleName("Screenshot"));
					addImportIfRequired("com.jogamp.opengl.util.awt.Screenshot");
				}
			}
		}

		processExpression(methodInvocation.getExpression());
		for (Expression e : (List<Expression>) methodInvocation.arguments())
		{
			processExpression(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void processFieldDeclaration(FieldDeclaration fieldDeclaration)
	{
		for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>) fieldDeclaration.fragments())
		{
			processExpression(fragment.getInitializer());

			ITypeBinding binding = fieldDeclaration.getType().resolveBinding();
			if (binding != null)
			{
				variables.add(binding, fragment.getName().getFullyQualifiedName());
			}
		}

		ITypeBinding type = fieldDeclaration.getType().resolveBinding();
		if (type != null)
		{
			String typeName = type.getQualifiedName();
			AST ast = fieldDeclaration.getAST();
			if ("javax.media.opengl.GL".equals(typeName))
			{
				Name name = ast.newName("GL2");
				fieldDeclaration.setType(ast.newSimpleType(name));
				addImportIfRequired("javax.media.opengl.GL2");
			}
			else if ("javax.media.opengl.GLCapabilities".equals(typeName))
			{
				Name name = ast.newName("GLCapabilitiesImmutable");
				fieldDeclaration.setType(ast.newSimpleType(name));
				addImportIfRequired("javax.media.opengl.GLCapabilitiesImmutable");
			}
			else if ("javax.media.opengl.GLCanvas".equals(typeName))
			{
				addImportIfRequired("javax.media.opengl.awt.GLCanvas");
			}
		}

	}

	@SuppressWarnings("unchecked")
	protected void processVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement)
	{
		ITypeBinding binding = variableDeclarationStatement.getType().resolveBinding();

		for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>) variableDeclarationStatement
				.fragments())
		{
			processExpression(fragment.getInitializer());

			if (binding != null)
			{
				variables.add(binding, fragment.getName().getFullyQualifiedName());
			}
		}

		if (binding != null)
		{
			String typeName = binding.getQualifiedName();
			if ("javax.media.opengl.GL".equals(typeName))
			{
				AST ast = variableDeclarationStatement.getAST();
				Name name = ast.newName("GL2");
				Type type = ast.newSimpleType(name);
				variableDeclarationStatement.setType(type);
				addImportIfRequired("javax.media.opengl.GL2");
			}
			else if ("javax.media.opengl.GLCanvas".equals(typeName))
			{
				addImportIfRequired("javax.media.opengl.awt.GLCanvas");
			}
		}
	}

	protected void processSingleVariableDeclaration(SingleVariableDeclaration singleVariableDeclaration)
	{
		ITypeBinding binding = singleVariableDeclaration.getType().resolveBinding();
		if (binding != null)
		{
			variables.add(binding, singleVariableDeclaration.getName().getFullyQualifiedName());

			if (binding.getQualifiedName().equals("javax.media.opengl.GL"))
			{
				AST ast = singleVariableDeclaration.getAST();
				Name name = ast.newName("GL2");
				singleVariableDeclaration.setType(ast.newSimpleType(name));
				addImportIfRequired("javax.media.opengl.GL2");
			}
		}

		processExpression(singleVariableDeclaration.getInitializer());
	}
}
