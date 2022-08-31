package index;

import constant.ConstantMapping;
import mappings.IndexMapping;
import mappings.ValueMapping;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import sql2msfol.utils.StatementUtils;
import value.Value;

public class PlainSelectIndex extends Index {
	private PlainSelect source;

	public PlainSelect getSource() {
		return source;
	}

	public void setSource(PlainSelect source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return String.format("[Select]: %1$s", source.toString());
	}

	@Override
	public void define() {
		Select s = new Select();
		s.setSelectBody(source);
		try {
			if (StatementUtils.noGroupByClause(s)) {
				if (StatementUtils.noFromClause(s)) {
					onlySelect();
				} else {
					if (StatementUtils.noWhereClause(s)) {
						if (StatementUtils.noJoinClause(s)) {
							selectFrom();
						} else {
							selectFromJoin(s);
						}
					} else {
						if (StatementUtils.noJoinClause(s)) {
							selectFromWhere();
						} else {
							selectFromJoinWhere(s);
						}
					}
				}
			} else {
				groupby(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void groupby(Select s) {
		PlainSelect pl = (PlainSelect) s.getSelectBody();
		Index original = IndexMapping.getPlainSelectIndex(pl);
		Index appendum = IndexMapping.getLinkingIndex(original);
		final String groupByFunctionName = IndexMapping.getGroupByFuncName(original);
		String declaration = "(declare-fun %1$s (Int) Int) ; %2$s";
		String comment = "%1$s -> %2$s".formatted(original.getFuncName(), appendum.getFuncName());
		System.out.println(declaration.formatted(groupByFunctionName, comment));
		String def1 = "(assert (forall ((x Int)) (=> (%2$s x) (exists ((y Int)) (and (%3$s y) (= y (%1$s x)))))))";
		System.out.println(def1.formatted(groupByFunctionName, original.getFuncName(), appendum.getFuncName()));
		String def2 = "(assert (forall ((x Int)) (=> (%3$s x) (exists ((y Int) (z Int)) (and (%2$s y) (%3$s z) (= (%1$s y) z) %4$s)))))";
		String groupByDef = "";
		for (int i = 0; i < pl.getGroupBy().getGroupByExpressions().size(); i++) {
			Expression expr = pl.getGroupBy().getGroupByExpressions().get(i);
			Value v = ValueMapping.getValue(expr);
			if (i == pl.getGroupBy().getGroupByExpressions().size() - 1) {
				groupByDef = groupByDef + "(= (%1$s x) (%1$s z))".formatted(v.getFuncName());
			} else {
				groupByDef = groupByDef + "(= (%1$s x) (%1$s z))".formatted(v.getFuncName()) + " ";
			}
		}
		System.out.println(def2.formatted(groupByFunctionName, original.getFuncName(), appendum.getFuncName(), groupByDef));
		String def3 = "(assert (forall ((x Int) (y Int)) (=> (and (%2$s x) (%2$s y) (distinct x y)) (and (distinct (%1$s x) (%1$s y)) %3$s))))";
		String groupByDef2 = "";
		if (pl.getGroupBy().getGroupByExpressions().size() > 1) {
			groupByDef2 = "(or ";
			for (int i = 0; i < pl.getGroupBy().getGroupByExpressions().size(); i++) {
				Expression expr = pl.getGroupBy().getGroupByExpressions().get(i);
				Value v = ValueMapping.getValue(expr);
				if (i == pl.getGroupBy().getGroupByExpressions().size() - 1) {
					groupByDef = groupByDef + "(distinct (%2$s (%1$s x)) (%2$s (%1$s y))))".formatted(groupByFunctionName, v.getFuncName());
				} else {
					groupByDef = groupByDef + "(distinct (%2$s (%1$s x)) (%2$s (%1$s y))) ".formatted(groupByFunctionName, v.getFuncName());
				}
			}
		}
		else {
			Expression expr = pl.getGroupBy().getGroupByExpressions().get(0);
			Value v = ValueMapping.getValue(expr);
			groupByDef2 = "(distinct (%2$s (%1$s x)) (%2$s (%1$s y)))".formatted(groupByFunctionName, v.getFuncName());
		}
		System.out.println(def3.formatted(groupByFunctionName, original.getFuncName(), groupByDef2));
	}

	private void selectFromJoinWhere(Select s) throws Exception {
		if (StatementUtils.noOnClause(s)) {
			selectFromJoinWhere_noOn();
		} else {
			selectFromJoinOnWhere();
		}
	}

	private void selectFromJoinOnWhere() {
		// SELECT FROM JOIN ON WHERE
		FromItem left = source.getFromItem();
		Index iLeft = IndexMapping.find(left);
		FromItem right = source.getJoins().get(0).getRightItem();
		Index iRight = IndexMapping.find(right);
		Index i = IndexMapping.getJoinIndex(iLeft, iRight);
		Expression on = source.getJoins().get(0).getOnExpression();
		Value v = ValueMapping.getValue(on);
		Expression where = source.getWhere();
		Value v_ = ValueMapping.getValue(where);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (and (%2$s x) (= (%3$s x) TRUE) (= (%4$s x) TRUE)))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName(), v_.getFuncName(), v.getFuncName()));
	}

	private void selectFromJoinWhere_noOn() {
		// SELECT FROM JOIN WHERE
		FromItem left = source.getFromItem();
		Index iLeft = IndexMapping.find(left);
		FromItem right = source.getJoins().get(0).getRightItem();
		Index iRight = IndexMapping.find(right);
		Index i = IndexMapping.getJoinIndex(iLeft, iRight);
		Expression where = source.getWhere();
		Value v = ValueMapping.getValue(where);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (and (%2$s x) (= (%3$s x) TRUE)))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName(), v.getFuncName()));
	}

	private void selectFromWhere() {
		// SELECT FROM WHERE
		FromItem fi = source.getFromItem();
		Index i = IndexMapping.find(fi);
		Expression where = source.getWhere();
		Value v = ValueMapping.getValue(where);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (and (%2$s x) (= (%3$s x) TRUE)))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName(), v.getFuncName()));
	}

	private void selectFromJoin(Select s) throws Exception {
		if (StatementUtils.noOnClause(s)) {
			selectFromJoin_noOn();
		} else {
			selectFromJoinOn();
		}
	}

	private void selectFromJoinOn() {
		// SELECT FROM JOIN ON
		FromItem left = source.getFromItem();
		Index iLeft = IndexMapping.find(left);
		FromItem right = source.getJoins().get(0).getRightItem();
		Index iRight = IndexMapping.find(right);
		Index i = IndexMapping.getJoinIndex(iLeft, iRight);
		Expression on = source.getJoins().get(0).getOnExpression();
		Value v = ValueMapping.getValue(on);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (and (%2$s x) (= (%3$s x) TRUE)))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName(), v.getFuncName()));
	}

	private void selectFromJoin_noOn() {
		// SELECT FROM JOIN
		FromItem left = source.getFromItem();
		Index iLeft = IndexMapping.find(left);
		FromItem right = source.getJoins().get(0).getRightItem();
		Index iRight = IndexMapping.find(right);
		Index i = IndexMapping.getJoinIndex(iLeft, iRight);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (%2$s x))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName()));
	}

	private void selectFrom() {
		// SELECT FROM
		FromItem fi = source.getFromItem();
		Index i = IndexMapping.find(fi);
		String def = "(assert (forall ((x Int)) (= (%1$s x) (%2$s x))))";
		System.out.println(String.format(def, getFuncName(), i.getFuncName()));
	}

	private void onlySelect() {
		// ONLY SELECT
		String def = "(assert (exists ((x Int)) (and (%1$s x) (forall ((y Int)) (=> (not (= x y)) (not (%1$s y)))))))";
		System.out.println(String.format(def, getFuncName()));
	}
}
