package constant;

import java.util.HashMap;

import net.sf.jsqlparser.statement.select.SubSelect;
import type.Type;

public class ConstantMapping {
	private static Integer counter;
	private static HashMap<SubSelect, Constant> constants;
	
	public static void reset() {
		ConstantMapping.counter = 0;
		ConstantMapping.constants = new HashMap<SubSelect, Constant>();
	}

	public static Integer getCounter() {
		return counter;
	}

	public static void setCounter(Integer counter) {
		ConstantMapping.counter = counter;
	}
	
	public static Constant addConstant(SubSelect ss, Type type) {
		Constant c = new Constant();
		c.setName(getConstrantName());
		c.setType(type);
		constants.put(ss, c);
		return c;
	}
	
	private static String getConstrantName() {
		return String.format("w%1$s", String.valueOf(counter++));
	}

	public static void define() {
		ConstantMapping.constants.values().forEach(c -> {
			c.define();
		});
	}
}
