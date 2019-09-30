// Generated from /home/alex/IdeaProjects/CubeExplorer/src/main/antlr4/MDXExp.g4 by ANTLR 4.7.2
package com.olap3.cubeexplorer.mdxparser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MDXExpParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MDXExpVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MDXExpParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(MDXExpParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MDXExpParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(MDXExpParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MDXExpParser#measure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeasure(MDXExpParser.MeasureContext ctx);
}