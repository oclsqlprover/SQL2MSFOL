package visitor;

import java.util.ArrayList;
import java.util.List;

import org.uni.dm2schema.dm.Association;
import org.uni.dm2schema.dm.Attribute;
import org.uni.dm2schema.dm.End;
import org.uni.dm2schema.dm.Entity;

import constant.Constant;
import constant.ConstantMapping;
import datamodel.DataModelUtils;
import index.AssociationIndex;
import index.EntityIndex;
import index.Index;
import index.JoinIndex;
import index.PlainSelectIndex;
import mappings.IndexMapping;
import mappings.ValueMapping;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import type.TypeUtils;
import value.ExpressionValue;

public class ExpressionValueVisitor implements ExpressionVisitor {

	private Index source;
	private Index parent;
	private List<String> definitions = new ArrayList<String>();
	// TODO: Clean this quick dirty implementation
	// As an expression, a SubSelect implementation sometimes need to be a
	// singled-value expression otherwise the statement will not be well-typed.
	// Here, in the implementation, whenever it is the case (that is a Subselect
	// must return a singled-value, we need to prove that it is indeed).
	// In our supported syntax, in most of the case the Subselect must be singled
	// value, the only case where it doesn't matter is the case
	// EXISTS subselect
	// to simplify the prototype, the dirty trick is implemented: adding a flag in
	// this visitor that is true whenever the proof for singled value is not needed.
	private boolean isSingleValued = true;

	public boolean isSingleValued() {
		return isSingleValued;
	}

	public void setSingleValued(boolean isSingleValued) {
		this.isSingleValued = isSingleValued;
	}

	@Override
	public void visit(BitwiseRightShift aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseLeftShift aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO: Type check of null?
		ExpressionValue v = valueExpression(nullValue, true);
		String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
		definition = String.format(definition, parent.getFuncName(), "%s", "NULL");
		definitions.add(definition);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub

	}

	private ExpressionValue valueExpression(Expression expr, Boolean forNow) {
		ExpressionValue ev = new ExpressionValue();
		ev.setExpr(expr);
		ev.setParentIndex(parent);
		ev.setSourceIndex(source);
		ev.setType(TypeUtils.get(expr, source));
		ev.setMeanings(definitions);
		ValueMapping.add(ev);
		return ev;
	}

	private ExpressionValueVisitor createVisitor() {
		ExpressionValueVisitor evv = new ExpressionValueVisitor();
		evv.setParent(this.parent);
		evv.setSource(this.source);
		return evv;
	}

	@Override
	public void visit(SignedExpression signedExpression) {
		Expression expr = signedExpression.getExpression();
		expr.accept(createVisitor());
		ExpressionValue v = valueExpression(signedExpression, true);
		String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (* (-1) (%3$s x))))))";
		definition = String.format(definition, parent.getFuncName(), "%s",
				ValueMapping.getValue(signedExpression.getExpression()).getFuncName());
		definitions.add(definition);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LongValue longValue) {
		ExpressionValue v = valueExpression(longValue, true);
		String axiom = "(assert (not (= %1$s nullInt)))";
		String axiom1 = "(assert (not (= %1$s invalidInt)))";
		axiom = String.format(axiom, longValue.getValue());
		axiom1 = String.format(axiom1, longValue.getValue());
		String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
		definition = String.format(definition, parent.getFuncName(), "%s", longValue.getValue());
		definitions.add(definition);
		definitions.add(axiom);
		definitions.add(axiom1);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(HexValue hexValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StringValue stringValue) {
		ExpressionValue v = valueExpression(stringValue, true);
		String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
		String axiom = "(assert (not (= %1$s nullString)))";
		String axiom1 = "(assert (not (= %1$s invalString)))";
		axiom = String.format(axiom, stringValue.toString());
		axiom1 = String.format(axiom1, stringValue.toString());
		definition = String.format(definition, parent.getFuncName(), "%s", stringValue.toString());
		definitions.add(definition);
		definitions.add(axiom);
		definitions.add(axiom1);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerDivision division) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Subtraction subtraction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AndExpression andExpression) {
		Expression left = andExpression.getLeftExpression();
		left.accept(createVisitor());
		Expression right = andExpression.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(andExpression, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (= (%3$s x) TRUE) (= (%4$s x) TRUE))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (or (= (%3$s x) FALSE) (= (%4$s x) FALSE))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (and (= (%3$s x) NULL) (= (%4$s x) TRUE)) (and (= (%3$s x) TRUE) (= (%4$s x) NULL)) (and (= (%3$s x) NULL) (= (%4$s x) NULL)))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(OrExpression orExpression) {
		Expression left = orExpression.getLeftExpression();
		left.accept(createVisitor());
		Expression right = orExpression.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(orExpression, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (or (= (%3$s x) TRUE) (= (%4$s x) TRUE))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (= (%3$s x) FALSE) (= (%4$s x) FALSE))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (and (= (%3$s x) NULL) (= (%4$s x) FALSE)) (and (= (%3$s x) FALSE) (= (%4$s x) NULL)) (and (= (%3$s x) NULL) (= (%4$s x) NULL)))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(XorExpression orExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualsTo equalsTo) {
		Expression left = equalsTo.getLeftExpression();
		left.accept(createVisitor());
		Expression right = equalsTo.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(equalsTo, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (= (%3$s x) (%4$s x)))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (= (%3$s x) (%4$s x))))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		Expression left = greaterThan.getLeftExpression();
		left.accept(createVisitor());
		Expression right = greaterThan.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(greaterThan, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (> (%3$s x) (%4$s x)))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (> (%3$s x) (%4$s x))))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		Expression left = greaterThanEquals.getLeftExpression();
		left.accept(createVisitor());
		Expression right = greaterThanEquals.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(greaterThanEquals, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (>= (%3$s x) (%4$s x)))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (>= (%3$s x) (%4$s x))))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(InExpression inExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(FullTextSearch fullTextSearch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		Expression expr = isNullExpression.getLeftExpression();
		expr.accept(createVisitor());
		ExpressionValue v = valueExpression(isNullExpression, true);
		String definition1;
		String definition2;
		if (!isNullExpression.isNot()) {
			definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (= (%3$s x) %4$s)))))";
			definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (not (= (%3$s x) %4$s))))))";
		} else {
			definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (not (= (%3$s x) %4$s))))))";
			definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (= (%3$s x) %4$s)))))";
		}
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(expr).getFuncName(), TypeUtils.nullOf(expr, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(expr).getFuncName(), TypeUtils.nullOf(expr, source));
		definitions.add(definition1);
		definitions.add(definition2);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(IsBooleanExpression isBooleanExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LikeExpression likeExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan minorThan) {
		Expression left = minorThan.getLeftExpression();
		left.accept(createVisitor());
		Expression right = minorThan.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(minorThan, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (< (%3$s x) (%4$s x)))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (< (%3$s x) (%4$s x))))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		Expression left = minorThanEquals.getLeftExpression();
		left.accept(createVisitor());
		Expression right = minorThanEquals.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(minorThanEquals, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (<= (%3$s x) (%4$s x)))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (<= (%3$s x) (%4$s x))))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		Expression left = notEqualsTo.getLeftExpression();
		left.accept(createVisitor());
		Expression right = notEqualsTo.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(notEqualsTo, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (not (= (%3$s x) (%4$s x))))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (not (= (%3$s x) %5$s)) (not (= (%4$s x) %6$s)) (= (%3$s x) (%4$s x)))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (= (%3$s x) %5$s) (= (%4$s x) %6$s))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName(),
				TypeUtils.nullOf(left, source), TypeUtils.nullOf(right, source));
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(Column tableColumn) {
		String columnName = tableColumn.getColumnName();
		ExpressionValue v = valueExpression(tableColumn, true);
		if ("TRUE".equals(tableColumn.getColumnName()) || "FALSE".equals(tableColumn.getColumnName())) {
			String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
			definition = String.format(definition, parent.getFuncName(), "%s", columnName);
			definitions.add(definition);
		} else if (DataModelUtils.isContextVariables(columnName)) {
			String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
			definition = String.format(definition, parent.getFuncName(), "%s", columnName);
			definitions.add(definition);
		} else {
			// It must be the projection from Fromitem(s)
			mapFromItemColumn(tableColumn, columnName, v, source, null);
		} 
		v.setMeanings(definitions);
	}

	private void mapFromItemColumn(Column tableColumn, String columnName, ExpressionValue v, Index source, String nested) {
		if (source instanceof EntityIndex) {
			EntityIndex ei = (EntityIndex) source;
			Entity e = ei.getSource();
			Attribute at = DataModelUtils.getAttribute(e, columnName);
			if (nested == null) {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s x)))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(at).getFuncName());
				definitions.add(definition);
			} else {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s (%4$s x))))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(at).getFuncName(), nested);
				definitions.add(definition);
			}
		} else if (source instanceof AssociationIndex) {
			AssociationIndex ai = (AssociationIndex) source;
			Association as = ai.getSource();
			End end = DataModelUtils.getAssociationEnd(as, columnName);
			if (nested == null) {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s x)))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(end).getFuncName());
				definitions.add(definition);
			} else {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s (%4$s x))))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(end).getFuncName(), nested);
				definitions.add(definition);
			}
		} else if (source instanceof PlainSelectIndex){
			PlainSelectIndex psi = (PlainSelectIndex) source;
			Expression expr = findReferExpression(psi, columnName);
			if (nested == null) {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s x)))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(expr).getFuncName());
				definitions.add(definition);
			} else {
				String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s (%4$s x))))))";
				definition = String.format(definition, parent.getFuncName(), "%s",
						ValueMapping.getValue(expr).getFuncName(), nested);
				definitions.add(definition);
			}
		} else {
			// Then it must be the JoinIndex
			JoinIndex ji = (JoinIndex) source;
			String alias = tableColumn.getTable().getName();
			if (ji.getAliasLeft().equals(alias)) {
				Index left = ji.getLeft();
				mapFromItemColumn(tableColumn, columnName, v, left, "left");
			} else {
				Index right = ji.getRight();
				mapFromItemColumn(tableColumn, columnName, v, right, "right");
			}
		}
	}

	private Expression findReferExpression(PlainSelectIndex psi, String columnName) {
		List<SelectItem> sis = psi.getSource().getSelectItems();
		for (SelectItem si : sis) {
			if (si instanceof SelectExpressionItem) {
				SelectExpressionItem sei = (SelectExpressionItem) si;
				Expression e = sei.getExpression();
				Alias as = sei.getAlias();
				if (as != null && as.getName().equals(columnName)) {
					return e;
				} else if (e instanceof Column) {
					Column c = (Column) e;
					if (c.getColumnName().equals(columnName)) {
						return c;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void visit(SubSelect subSelect) {
		PlainSelect ps = (PlainSelect) subSelect.getSelectBody();
		Select s = prepareSelect(ps);
		StatementValueVisitor vsv = new StatementValueVisitor();
		s.accept(vsv);
		ExpressionValue v = valueExpression(subSelect, true);
		if (isSingleValued) {
			String comment = "; Here, one needs to prove that [%1$s] subselect returns exactly one row with one value, that is, a single-value";
			comment = String.format(comment, IndexMapping.getPlainSelectIndex(ps).getFuncName());
			String comment1 = "; Below provides the lemma that proves the above lemma";
			String comment2 = "; Please enable the lemma proof by removing the next 3 lines";
			String proof = "; (assert (not (exists ((x Int)) (and (%1$s x) (forall ((y Int)) (=> (not (= x y)) (not (%1$s y)))))))";
			String checksat = "; (check-sat)";
			String exit = "; (exit)";
			proof = String.format(proof, IndexMapping.getPlainSelectIndex(ps).getFuncName());
			String comment3 = "; ==== [Lemma ends here] ====";
			String comment4 = "; Assuming that the above proof holds, we append to the theorem the following \"facts\" about [%1$s] subselect";
			comment4 = String.format(comment4, IndexMapping.getPlainSelectIndex(ps).getFuncName());
			// TODO: We assume that the subslect here contains exactly one projection.
			// This can easily be checked but here we leave the responsibility for the
			// developer who implements the SQL query.
			Expression expr = ((SelectExpressionItem) ps.getSelectItems().get(0)).getExpression();
			Constant c = ConstantMapping.addConstant(subSelect,
					TypeUtils.get(expr, IndexMapping.getPlainSelectIndex(ps)));
			String definition = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) %3$s))))";
			String definition2 = "(assert (exists ((x Int)) (and (%1$s x) (= (%2$s x) %3$s))))";
			definition = String.format(definition, parent.getFuncName(), "%s", c.getName());
			definition2 = String.format(definition2, IndexMapping.getPlainSelectIndex(ps).getFuncName(),
					ValueMapping.getValue(expr).getFuncName(), c.getName());
			definitions.add(comment);
			definitions.add(comment1);
			definitions.add(comment2);
			definitions.add(proof);
			definitions.add(checksat);
			definitions.add(exit);
			definitions.add(comment3);
			definitions.add(comment4);
			definitions.add(definition);
			definitions.add(definition2);
		} else {
			String comment = "; In this case, the val of the subselect is irrelevant to the decidability of the theory";
			String comment2 = "; ergo, it is omitted here for the sake of simplicity";
			definitions.add(comment);
			definitions.add(comment2);
		}
		v.setMeanings(definitions);
	}

	private Select prepareSelect(PlainSelect ps) {
		Select s = new Select();
		s.setSelectBody(ps);
		return s;
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		Expression when = caseExpression.getWhenClauses().get(0).getWhenExpression();
		Expression then = caseExpression.getWhenClauses().get(0).getThenExpression();
		Expression elze = caseExpression.getElseExpression();
		when.accept(createVisitor());
		then.accept(createVisitor());
		elze.accept(createVisitor());
		ExpressionValue v = valueExpression(caseExpression, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) (%4$s x)) (= (%3$s x) TRUE)))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) (%5$s x)) (or (= (%3$s x) FALSE) (= (%3$s x) NULL))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(when).getFuncName(), ValueMapping.getValue(then).getFuncName(),
				ValueMapping.getValue(elze).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(when).getFuncName(), ValueMapping.getValue(then).getFuncName(),
				ValueMapping.getValue(elze).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		Expression expr = existsExpression.getRightExpression();
		ExpressionValueVisitor evv = createVisitor();
		evv.isSingleValued = false;
		expr.accept(evv);
		// Shortcut: We know for a fact that the subexpression is a subselect!
		PlainSelect subSelect = (PlainSelect) ((SubSelect) expr).getSelectBody();
		ExpressionValue v = valueExpression(existsExpression, true);
		String definition1;
		String definition2;
		if (!existsExpression.isNot()) {
			definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (exists ((y Int)) (%3$s y))))))";
			definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (not (exists ((y Int)) (%3$s y)))))))";
		} else {
			definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (not (exists ((y Int)) (%3$s y)))))))";
			definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (exists ((y Int)) (%3$s y))))))";
		}
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				IndexMapping.getPlainSelectIndex(subSelect).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				IndexMapping.getPlainSelectIndex(subSelect).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		Expression left = bitwiseAnd.getLeftExpression();
		left.accept(createVisitor());
		Expression right = bitwiseAnd.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(bitwiseAnd, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (and (= (%3$s x) TRUE) (= (%4$s x) TRUE))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (or (= (%3$s x) FALSE) (= (%4$s x) FALSE))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (and (= (%3$s x) NULL) (= (%4$s x) TRUE)) (and (= (%3$s x) TRUE) (= (%4$s x) NULL)) (and (= (%3$s x) NULL) (= (%4$s x) NULL)))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		Expression left = bitwiseOr.getLeftExpression();
		left.accept(createVisitor());
		Expression right = bitwiseOr.getRightExpression();
		right.accept(createVisitor());
		ExpressionValue v = valueExpression(bitwiseOr, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (or (= (%3$s x) TRUE) (= (%4$s x) TRUE))))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (and (= (%3$s x) FALSE) (= (%4$s x) FALSE))))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (or (and (= (%3$s x) NULL) (= (%4$s x) FALSE)) (and (= (%3$s x) FALSE) (= (%4$s x) NULL)) (and (= (%3$s x) NULL) (= (%4$s x) NULL)))))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(left).getFuncName(), ValueMapping.getValue(right).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CastExpression cast) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Modulo modulo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExtractExpression eexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntervalExpression iexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonExpression jsonExpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonOperator jsonExpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(UserVariable var) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NumericBind bind) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(KeepExpression aexpr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MySQLGroupConcat groupConcat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ValueListExpression valueList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RowConstructor rowConstructor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RowGetExpression rowGetExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OracleHint hint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateTimeLiteralExpression literal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NotExpression aThis) {
		Expression expr = aThis.getExpression();
		expr.accept(createVisitor());
		ExpressionValue v = valueExpression(aThis, true);
		String definition1 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) TRUE) (= (%3$s x) FALSE)))))";
		String definition2 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) FALSE) (= (%3$s x) TRUE)))))";
		String definition3 = "(assert (forall ((x Int)) (=> (%1$s x) (= (= (%2$s x) NULL) (= (%3$s x) NULL)))))";
		definition1 = String.format(definition1, parent.getFuncName(), "%s",
				ValueMapping.getValue(expr).getFuncName());
		definition2 = String.format(definition2, parent.getFuncName(), "%s",
				ValueMapping.getValue(expr).getFuncName());
		definition3 = String.format(definition3, parent.getFuncName(), "%s",
				ValueMapping.getValue(expr).getFuncName());
		definitions.add(definition1);
		definitions.add(definition2);
		definitions.add(definition3);
		v.setMeanings(definitions);
	}

	@Override
	public void visit(NextValExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CollateExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SimilarToExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ArrayExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ArrayConstructor aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(VariableAssignment aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(XMLSerializeExpr aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimezoneExpression aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonAggregateFunction aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JsonFunction aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ConnectByRootOperator aThis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OracleNamedFunctionParameter aThis) {
		// TODO Auto-generated method stub

	}

	public Index getSource() {
		return source;
	}

	public void setSource(Index source) {
		this.source = source;
	}

	public Index getParent() {
		return parent;
	}

	public void setParent(Index parent) {
		this.parent = parent;
	}

}
