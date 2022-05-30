package index;

import java.util.Arrays;
import java.util.List;

import mappings.IndexMapping;

public class JoinIndex extends Index {
	private Index left;
	private Index right;
	private String aliasLeft;
	private String aliasRight;
	
	final List<String> defs = Arrays.asList(
			"(assert (forall ((x Int) (y Int)) (=> (and (%1$s x) (%1$s y) (not (= x y))) (not (and (= (left x) (left y)) (= (right x) (right y)))))))",
			"(assert (forall ((x Int) (y Int)) (=> (%2$s x) (%3$s y) (exists ((z Int)) (and (%1$s z) (= x (left z)) (= y (right z)))))))",
			"(assert (forall ((z Int)) (=> (%1$s z) (exists ((x Int) (y Int)) (and (%2$s x) (%3$s y) (= x (left z)) (= y (right z)))))))"
	);

	public Index getLeft() {
		return left;
	}

	public void setLeft(Index left) {
		this.left = left;
	}

	public Index getRight() {
		return right;
	}

	public void setRight(Index right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return String.format("[Join]: %1$s <--> %2$s", left.getName(), right.getName());
	}

	@Override
	public void define() {
		Index join = IndexMapping.getJoinIndex(left, right);
		for (String def :defs) {
			System.out.println(String.format(def, join.getFuncName(), left.getFuncName(), right.getFuncName()));
		}
	}

	public String getAliasLeft() {
		return aliasLeft;
	}

	public void setAliasLeft(String aliasLeft) {
		this.aliasLeft = aliasLeft;
	}

	public String getAliasRight() {
		return aliasRight;
	}

	public void setAliasRight(String aliasRight) {
		this.aliasRight = aliasRight;
	}
}
