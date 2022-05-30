package constant;

import type.Type;

public class Constant {
	private String name;
	private Type type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void define() {
		String definition = "(declare-const %1$s %2$s)";
		System.out.println(String.format(definition, name, type.getName()));
	}
}
