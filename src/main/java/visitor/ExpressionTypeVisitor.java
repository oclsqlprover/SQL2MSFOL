package visitor;

import configurations.Context;
import datamodel.DataModelUtils;
import index.AssociationIndex;
import index.EntityIndex;
import index.Index;
import index.JoinIndex;
import index.PlainSelectIndex;
import mappings.IndexMapping;
import mappings.ValueMapping;
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
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import type.TypeUtils;
import value.Value;

public class ExpressionTypeVisitor implements ExpressionVisitor {

	private String type;
	private Index source;

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
		// TODO: Boolean for now!
		this.type = "BOOL";
	}

	@Override
	public void visit(Function function) {
		Expression expr = function.getParameters().getExpressions().get(0);
		expr.accept(this);
	}

	@Override
	public void visit(SignedExpression signedExpression) {
		Expression expr = signedExpression.getExpression();
		expr.accept(this);
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
		this.type = "Int";
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
		this.type = "String";
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
		this.type = "BOOL";
	}

	@Override
	public void visit(OrExpression orExpression) {
		this.type = "BOOL";
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
		this.type = "BOOL";
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		this.type = "BOOL";
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		this.type = "BOOL";
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
		this.type = "BOOL";
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
		this.type = "BOOL";
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		this.type = "BOOL";
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		this.type = "BOOL";
	}

	@Override
	public void visit(Column tableColumn) {
		String columnName = tableColumn.getColumnName();
		if ("TRUE".equalsIgnoreCase(columnName)) {
			this.type = "BOOL";
			return;
		}
		if ("FALSE".equalsIgnoreCase(columnName)) {
			this.type = "BOOL";
			return;
		}
		if (DataModelUtils.matchContext(tableColumn)) {
			Context ctx = DataModelUtils.get(tableColumn);
			this.type = TypeUtils.convert(ctx.getType());
			return;
		}
		{
			if (source instanceof EntityIndex) {
				EntityIndex ei = (EntityIndex) source;
				String type = DataModelUtils.getType(columnName, ei.getSource());
				this.type = TypeUtils.convert(type);
			} else if (source instanceof AssociationIndex) {
				this.type = "Classifier";
			} else if (source instanceof PlainSelectIndex) {
//				PlainSelectIndex psi = (PlainSelectIndex) source;
//				PlainSelect ps = psi.getSource();
//				FromItem fi = ps.getFromItem();
//				Index ifi = IndexMapping.find(fi);
//				this.type = findTypeFromSourceIndex(columnName, ifi);
				// Must be another subselect
				PlainSelectIndex psi_ = (PlainSelectIndex) source;
				Value referenceValue = ValueMapping.getValue(psi_, columnName);
				String type = referenceValue.getType().getName();
				this.type = TypeUtils.convert(type);
			} else {
				// Must be a JoinIndex
				JoinIndex ji = (JoinIndex) source;
				Index left = ji.getLeft();
				Index right = ji.getRight();
				if (findTypeFromSourceIndex(columnName, left) == null) {
					this.type = findTypeFromSourceIndex(columnName, right);
				} else {
					this.type = findTypeFromSourceIndex(columnName, left);
				}
			}
		}
	}

	private String findTypeFromSourceIndex(String columnName, Index ifi) {
		if (ifi instanceof EntityIndex) {
			EntityIndex ei = (EntityIndex) ifi;
			String type = DataModelUtils.getType(columnName, ei.getSource());
			return TypeUtils.convert(type);
		} else if (ifi instanceof AssociationIndex) {
			return "Classifier";
		} else {
			// Must be another subselect
			PlainSelectIndex psi_ = (PlainSelectIndex) ifi;
			Value referenceValue = ValueMapping.getValue(psi_, columnName);
			if (referenceValue == null) {
				return null;
			}
			String type = referenceValue.getType().getName();
			return TypeUtils.convert(type);
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		PlainSelect ps = (PlainSelect) subSelect.getSelectBody();
		SelectExpressionItem sei = (SelectExpressionItem) ps.getSelectItems().get(0);
		ExpressionTypeVisitor etv = new ExpressionTypeVisitor();
		etv.setSource(IndexMapping.getPlainSelectIndex(ps));
		sei.getExpression().accept(etv);
		this.type = etv.getType();
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		Expression elze = caseExpression.getElseExpression();
		Expression then = caseExpression.getWhenClauses().get(0).getThenExpression();
		elze.accept(this);
		String elzeType = this.getType();
		then.accept(this);
		String thenType = this.getType();
		if (elzeType != thenType) {
			System.out.println("Type mismatch in the Case expression");
		}
	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		this.type = "BOOL";
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
		this.type = "BOOL";
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		this.type = "BOOL";
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
		this.type = "BOOL";
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

	public String getType() {
		return type;
	}

	public Index getSource() {
		return source;
	}

	public void setSource(Index source) {
		this.source = source;
	}
}
