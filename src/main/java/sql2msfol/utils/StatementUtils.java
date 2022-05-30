package sql2msfol.utils;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

public class StatementUtils {

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

}
