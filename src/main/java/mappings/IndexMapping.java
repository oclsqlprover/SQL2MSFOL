package mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.uni.dm2schema.dm.Association;
import org.uni.dm2schema.dm.DataModel;
import org.uni.dm2schema.dm.Entity;

import datamodel.DataModelUtils;
import index.AssociationIndex;
import index.EntityIndex;
import index.Index;
import index.JoinIndex;
import index.PlainSelectIndex;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;

public class IndexMapping {
	
	private static Integer counter;
	private static List<Index> indices;
	private static HashMap<String, Index> aliases;

	public static void reset() {
		IndexMapping.counter = 0;
		IndexMapping.indices = new ArrayList<Index>();
		IndexMapping.aliases = new HashMap<String, Index>();
		indexDatamodel();
	}
	
	private static void indexDatamodel() {
		DataModel dataModel = DataModelUtils.getDataModel();
		dataModel.getEntities().values().forEach(e -> indexEntity(e));
		dataModel.getAssociations().forEach(a -> indexAssociation(a));
	}

	private static void indexEntity(Entity e) {
		EntityIndex ei = new EntityIndex();
		ei.setSource(e);
		add(e.getName(), ei);
	}
	
	private static void indexAssociation(Association a) {
		AssociationIndex ai = new AssociationIndex();
		ai.setSource(a);
		add(a.getName(), ai);
	}

	private static String generateName() {
		return String.valueOf(counter++);
	}

	public static void add(Index index) {
		index.setName(generateName());
		indices.add(index);
	}

	public static void add(String name, Index index) {
		index.setName(name);
		indices.add(index);
	}
	
	public static Index find(FromItem fi) {
		if (fi instanceof SubSelect) {
			SubSelect ss = (SubSelect) fi;
			PlainSelect ps = (PlainSelect) ss.getSelectBody();
			return getPlainSelectIndex(ps);
		} else {
			Table t = (Table) fi;
			String tableName = t.getName();
			if (DataModelUtils.isEntity(tableName)) {
				Entity e = DataModelUtils.getEntity(tableName);
				return getEntityIndex(e);
			} else {
				Association a = DataModelUtils.getAssociation(tableName);
				return getAssociationIndex(a);
			}
		}
		// fromItem cannot be our JoinIndex object!
		// so, no need to check that case
	}

	public static Index getPlainSelectIndex(PlainSelect ps) {
		for (Index i : indices) {
			if (i instanceof PlainSelectIndex) {
				PlainSelectIndex psi = (PlainSelectIndex) i;
				if (psi.getSource().equals(ps)) {
					return i;
				}
			}
		}
		return null;
	}
	
	public static Index getEntityIndex(Entity e) {
		for (Index i : indices) {
			if (i instanceof EntityIndex) {
				EntityIndex ei = (EntityIndex) i;
				if (ei.getSource().equals(e)) {
					return i;
				}
			}
		}
		return null;
	}
	
	public static Index getAssociationIndex(Association a) {
		for (Index i : indices) {
			if (i instanceof AssociationIndex) {
				AssociationIndex ai = (AssociationIndex) i;
				if (ai.getSource().equals(a)) {
					return i;
				}
			}
		}
		return null;
	}

	public static void declare() {
		IndexMapping.indices.forEach(i -> {
			i.declare();
		});
	}
	
	public static void define() {
		IndexMapping.indices.forEach(i -> {
			i.define();
		});
	}

	public static Index getJoinIndex(Index left, Index right) {
		for (Index i : indices) {
			if (i instanceof JoinIndex) {
				JoinIndex ji = (JoinIndex) i;
				if (ji.getLeft().equals(left) && ji.getRight().equals(right)) {
					return i;
				}
			}
		}
		return null;
	}

	public static void link(String alias, Index i) {
		aliases.put(alias, i);
	}

}
