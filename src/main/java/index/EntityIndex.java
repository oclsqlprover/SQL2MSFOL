package index;

import java.util.Arrays;
import java.util.List;

import org.uni.dm2schema.dm.Entity;

public class EntityIndex extends Index {
	private Entity source;
	final List<String> defs = Arrays.asList(
			"(assert (forall ((x Int)) (=> (%1$s x) (exists ((c Classifier)) (and (%2$s c) (= c (id x)))))))",
			"(assert (forall ((c Classifier)) (=> (%2$s c) (exists ((x Int)) (and (%1$s x) (= c (id x)))))))",
			"(assert (forall ((x Int) (y Int)) (=> (and (%1$s x) (%1$s y) (not (= x y))) (not (= (id x) (id y))))))",
			"(assert (forall ((x Int)) (=> (%1$s x) (= (val-%1$s-%2$s_id x) (id x)))))"
	);

	public Entity getSource() {
		return source;
	}

	public void setSource(Entity source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return String.format("[Entity]: %1$s", source.getName());
	}

	@Override
	public void define() {
		for (String def : defs) {
			System.out.println(String.format(def, super.getFuncName(), source.getName()));
		}
	}
}
