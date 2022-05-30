package value;

import java.util.ArrayList;
import java.util.List;

import index.JoinIndex;
import net.sf.jsqlparser.expression.Expression;

public class ExpressionValue extends Value {
	private Expression expr;
	private List<String> meanings;
	private boolean isLeft = false;

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
			System.out.println(s);
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
		return String.format("val-%1$s-expr%2$s", parentIndex.getFuncName(), getName());
	}
}
