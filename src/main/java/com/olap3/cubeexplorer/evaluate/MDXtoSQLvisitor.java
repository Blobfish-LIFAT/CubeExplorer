package com.olap3.cubeexplorer.evaluate;

import com.olap3.cubeexplorer.evaluate.mdxparser.MDXExpBaseVisitor;
import com.olap3.cubeexplorer.evaluate.mdxparser.MDXExpParser;
import com.olap3.cubeexplorer.model.MeasureFragment;
import com.olap3.cubeexplorer.mondrian.CubeUtils;

/**
 * Does the MDX -&gt; SQL translation for calculated members
 */
class MDXtoSQLvisitor extends MDXExpBaseVisitor<String> {
    CubeUtils cube;
    String query;

    public MDXtoSQLvisitor(CubeUtils cube) {
        this.cube = cube;
    }

    @Override
    public String visitExpression(MDXExpParser.ExpressionContext ctx) {
        //System.out.println(ctx.children);
        return "(" + visit(ctx.getChild(0)) + ctx.OP() + visit(ctx.getChild(2)) + ")";
    }


    @Override
    public String visitMeasure(MDXExpParser.MeasureContext ctx) {
        var atoms = ctx.Atom();
        var measure = cube.getMeasure(atoms.get(1).getText().substring(1, atoms.get(1).getText().length() - 1));

        MeasureFragment mf = MeasureFragment.newInstance(measure);

        return SQLFactory.buildAggregate(mf);
    }

    @Override
    public String visitStart(MDXExpParser.StartContext ctx) {
        return visitExpression(ctx.expression());
    }
}
