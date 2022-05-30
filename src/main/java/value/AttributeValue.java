package value;


import org.uni.dm2schema.dm.Attribute;
import org.uni.dm2schema.dm.Entity;

import datamodel.DataModelUtils;

public class AttributeValue extends Value {
	private Attribute source;

	public Attribute getSource() {
		return source;
	}

	public void setSource(Attribute source) {
		this.source = source;
	}

	@Override
	public void define() {
		Entity e = DataModelUtils.getEntity(source);
		if (source.getName().contains("_id")) {
			String def = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (id x)))))";
			System.out.println(String.format(def, getSourceIndex().getFuncName(), getFuncName()));
		} else {
			String def = "(assert (forall ((x Int)) (=> (%1$s x) (= (%2$s x) (%3$s (id x))))))";
			System.out.println(String.format(def, getSourceIndex().getFuncName(), getFuncName(), source.getName()+"_"+e.getName()));
		}
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
