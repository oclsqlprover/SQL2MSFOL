package index;

import java.util.Arrays;
import java.util.List;

import org.uni.dm2schema.dm.Association;


public class AssociationIndex extends Index {
	private Association source;
	final List<String> defs = Arrays.asList(
			"(assert (forall ((x Int) (y Int)) (=> (and (%1$s x) (%1$s y) (not (= x y))) (not (and (= (left x) (left y)) (= (right x) (right y)))))))",
			"(assert (forall ((x Classifier) (y Classifier)) (=> (%2$s x y) (exists ((z Int)) (and (%1$s z) (= x (id (left z))) (= y (id (right z))))))))",
			"(assert (forall ((z Int)) (=> (%1$s z) (exists ((x Classifier) (y Classifier)) (and (%2$s x y) (= x (id (left z))) (= y (id (right z))))))))"
	);

	public Association getSource() {
		return source;
	}

	public void setSource(Association source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return String.format("[Association]: %1$s", source.getName());
	}

	@Override
	public void define() {
		for (String def : defs) {
			System.out.println(String.format(def, super.getFuncName(), source.getName()));
		}
	}
}
