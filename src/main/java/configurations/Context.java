package configurations;

public class Context {
	private String var;
	private String type;

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Context(String var, String type) {
		super();
		this.var = var;
		this.type = type;
	}
}
