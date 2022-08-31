package sql2msfol.utils;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class StatementUtils {
	
	public static boolean noGroupByClause(Select select) throws Exception {
		if (noFromClause(select))
			return false;
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) sb;
			return ps.getGroupBy() == null;
		}
		throw new Exception("Unfamiliar SQL pattern");
	}

	public static boolean noFromClause(Select select) throws Exception {
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) sb;
			return ps.getFromItem() == null;
		}
		throw new Exception("Unfamiliar SQL pattern");
	}

	public static boolean noJoinClause(Select select) throws Exception {
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) sb;
			if (ps.getJoins() == null) {
				return true;
			}
			if (ps.getJoins().size() == 0) {
				return true;
			}
			if (ps.getJoins().size() == 1) {
				return false;
			}
		}
		throw new Exception("Unfamiliar SQL pattern");
	}

	public static boolean noWhereClause(Select select) throws Exception {
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) sb;
			return ps.getWhere() == null;
		}
		throw new Exception("Unfamiliar SQL pattern");
	}

	public static boolean noOnClause(Select select) throws Exception {
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect) sb;
			if (ps.getJoins().get(0).getOnExpressions() == null || 
					ps.getJoins().get(0).getOnExpressions().size() == 0) {
				return true;
			}
			return false;
		}
		throw new Exception("Unfamiliar SQL pattern");
	}

	public static Select constructNewQueryWithoutGroupBy(Select select) {
		Select newSelect = new Select();
		PlainSelect pl = (PlainSelect) select.getSelectBody();
		PlainSelect newPl = new PlainSelect();
		newPl.setFromItem(pl.getFromItem());
		newPl.setWhere(pl.getWhere());
		newPl.setJoins(pl.getJoins());
		List<SelectItem> sis = new ArrayList<SelectItem>();
		GroupByElement gbe = pl.getGroupBy();
		for (Expression e : gbe.getGroupByExpressions()) {
			SelectExpressionItem sei = new SelectExpressionItem(e);
			sis.add(sei);
		}
		newPl.setSelectItems(sis);
		newSelect.setSelectBody(newPl);
		return newSelect;
	}

}
