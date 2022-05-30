package value;

import index.Index;
import type.Type;

public abstract class Value {

	private String name;
	protected Index parentIndex; // parent is where the expression stands
	private Index sourceIndex; // source is where the expresison value can be found
	private Type type;

	final String declaration = "(declare-fun %1$s (Int) %2$s)";
	final String comment = "%1$s = %2$s";

	public String getName() {
		return name;
	}
	
	public abstract String getFuncName();

	public void setName(String name) {
		this.name = name;
	}

	public String comment() {
		return String.format(comment, getFuncName(), toString());
	}

	public void declare() {
		String dec = String.format(declaration, getFuncName(), type.getName());
		System.out.println(String.format("%s ; %s", dec, comment()));
	}

	public Index getParentIndex() {
		return parentIndex;
	}

	public void setParentIndex(Index source) {
		this.parentIndex = source;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Index getSourceIndex() {
		return sourceIndex;
	}

	public void setSourceIndex(Index sourceIndex) {
		this.sourceIndex = sourceIndex;
	}

	public abstract void define();

}
