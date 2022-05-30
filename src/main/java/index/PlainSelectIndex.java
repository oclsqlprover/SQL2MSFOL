package index;

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
			if (StatementUtils.noFromClause(s)) {
				// ONLY SELECT
				String def = "(assert (exists ((x Int)) (and (%1$s x) (forall ((y Int)) (=> (not (= x y)) (not (%1$s y)))))))";
				System.out.println(String.format(def, getFuncName()));
			} else {
				if (StatementUtils.noWhereClause(s)) {
					if (StatementUtils.noJoinClause(s)) {
						// SELECT FROM
						FromItem fi = source.getFromItem();
						Index i = IndexMapping.find(fi);
						String def = "(assert (forall ((x Int)) (= (%1$s x) (%2$s x))))";
						System.out.println(String.format(def, getFuncName(), i.getFuncName()));
					} else {
						if (StatementUtils.noOnClause(s)) {
							// SELECT FROM JOIN
							FromItem left = source.getFromItem();
							Index iLeft = IndexMapping.find(left);
							FromItem right = source.getJoins().get(0).getRightItem();
							Index iRight = IndexMapping.find(right);
							Index i = IndexMapping.getJoinIndex(iLeft, iRight);
							String def = "(assert (forall ((x Int)) (= (%1$s x) (%2$s x))))";
							System.out.println(String.format(def, getFuncName(), i.getFuncName()));
						} else {
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
					}
				} else {
					if (StatementUtils.noJoinClause(s)) {
						// SELECT FROM WHERE
						FromItem fi = source.getFromItem();
						Index i = IndexMapping.find(fi);
						Expression where = source.getWhere();
						Value v = ValueMapping.getValue(where);
						String def = "(assert (forall ((x Int)) (= (%1$s x) (and (%2$s x) (= (%3$s x) TRUE)))))";
						System.out.println(String.format(def, getFuncName(), i.getFuncName(), v.getFuncName()));
					} else {
						if (StatementUtils.noOnClause(s)) {
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
						} else {
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
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
