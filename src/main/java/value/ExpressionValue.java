package value;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;

public class ExpressionValue extends Value {
	private Expression expr;
	private List<String> meanings;
	private boolean isLeft = false;
	private boolean isRes = false;
	private boolean isGrouped = false;
	private String groupbyFuncname = null;

	public ExpressionValue() {
		meanings = new ArrayList<String>();
	}

	public Expression getExpr() {
		return expr;
	}

	public List<String> getMeanings() {
		return meanings;
	}

	public void setMeanings(List<String> meanings) {
		this.meanings = meanings;
	}

	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		return expr.toString();
	}

	@Override
	public void define() {
		for (String s : meanings) {
			System.out.println(String.format(s, getFuncName()));
		}
	}

	public boolean isLeft() {
		return isLeft;
	}

	public void setLeft(boolean isLeft) {
		this.isLeft = isLeft;
	}

	@Override
	public String getFuncName() {
		if (isRes)
			return "val-index0-res";
		return String.format("val-%1$s-expr%2$s", parentIndex.getFuncName(), getName());
	}

	public boolean isRes() {
		return isRes;
	}

	public void setRes(boolean isRes) {
		this.isRes = isRes;
	}

	public boolean isGrouped() {
		return isGrouped;
	}

	public void setGrouped(boolean isGrouped) {
		this.isGrouped = isGrouped;
	}

	public String getGroupbyFuncname() {
		return groupbyFuncname;
	}

	public void setGroupbyFuncname(String groupbyFuncname) {
		this.groupbyFuncname = groupbyFuncname;
	}
}
