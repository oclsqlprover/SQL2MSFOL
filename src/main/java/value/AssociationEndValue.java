package value;

import org.uni.dm2schema.dm.End;

public class AssociationEndValue extends Value {
	private Boolean isLeft;
	private End source;

	public End getSource() {
		return source;
	}

	public void setSource(End source) {
		this.source = source;
	}

	@Override
	public void define() {
		String def = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (id (%3$s x))))))";
		System.out.println(String.format(def, getSourceIndex().getFuncName(), getFuncName(), isLeft ? "left" : "right"));
	}

	public Boolean getIsLeft() {
		return isLeft;
	}

	public void setIsLeft(Boolean isLeft) {
		this.isLeft = isLeft;
	}
	
	@Override
	public String toString() {
		return source.getName();
	}

	@Override
	public String getFuncName() {
		return String.format("val-%1$s-%2$s", parentIndex.getFuncName(), getName());
	}
}
