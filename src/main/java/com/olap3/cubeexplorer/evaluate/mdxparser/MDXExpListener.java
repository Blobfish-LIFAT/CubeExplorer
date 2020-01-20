// Generated from /home/alex/IdeaProjects/CubeExplorer/src/main/antlr4/MDXExp.g4 by ANTLR 4.7.2
package com.olap3.cubeexplorer.evaluate.mdxparser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MDXExpParser}.
 */
public interface MDXExpListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MDXExpParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(MDXExpParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link MDXExpParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(MDXExpParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link MDXExpParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(MDXExpParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MDXExpParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(MDXExpParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MDXExpParser#measure}.
	 * @param ctx the parse tree
	 */
	void enterMeasure(MDXExpParser.MeasureContext ctx);
	/**
	 * Exit a parse tree produced by {@link MDXExpParser#measure}.
	 * @param ctx the parse tree
	 */
	void exitMeasure(MDXExpParser.MeasureContext ctx);
}