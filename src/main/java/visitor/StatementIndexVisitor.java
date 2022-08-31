package visitor;

import java.util.List;

import constant.ConstantMapping;
import index.Index;
import index.JoinIndex;
import index.PlainSelectIndex;
import mappings.IndexMapping;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import sql2msfol.utils.StatementUtils;

public class StatementIndexVisitor implements StatementVisitor {
	
	@Override
	public void visit(SavepointStatement savepointStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RollbackStatement rollbackStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Comment comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Commit commit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Delete delete) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Update update) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Insert insert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Replace replace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Drop drop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Truncate truncate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateIndex createIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateSchema aThis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateTable createTable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateView createView) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AlterView alterView) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Alter alter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Statements stmts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Execute execute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SetStatement set) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ResetStatement reset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ShowColumnsStatement set) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ShowTablesStatement showTables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Merge merge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Select select) {
		// The only Statement that we care is Select statement
		PlainSelect ps = (PlainSelect) select.getSelectBody();
		indexPlainSelect(ps);
		// If there is no join
		try {
			if (StatementUtils.noGroupByClause(select)) {
				noGroupByMapping(select, ps);
			} else {
				GroupByMapping(select,ps);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		selectItemsMapping(ps);
	}

	private void GroupByMapping(Select select, PlainSelect ps) {
		Select flat_b = StatementUtils.constructNewQueryWithoutGroupBy(select);
		flat_b.accept(this);
		IndexMapping.link(IndexMapping.getPlainSelectIndex(ps), flat_b);
		final String groupByFunctionName = "groupby%d".formatted(ConstantMapping.getCounter());
		IndexMapping.mapGroupByFuncName(IndexMapping.getPlainSelectIndex(ps), groupByFunctionName);
	}

	private void selectItemsMapping(PlainSelect ps) {
		List<SelectItem> sis = ps.getSelectItems();
		for (SelectItem si : sis) {
			SelectExpressionItem sei = (SelectExpressionItem) si;
			Expression expr = sei.getExpression();
			indexExpression(expr);
		}
	}

	private void noGroupByMapping(Select select, PlainSelect ps) throws Exception {
		if (StatementUtils.noFromClause(select)) {}
		else {
			fromMapping(select, ps);
		}
		if (StatementUtils.noJoinClause(select)) {} 
		else {
			joinMapping(select, ps);
		}
	}

	private void joinMapping(Select select, PlainSelect ps) throws Exception {
		FromItem left = ps.getFromItem();
		FromItem right = ps.getJoins().get(0).getRightItem();
		indexFromItem(right);
		indexJoin(left, right);
		if (StatementUtils.noOnClause(select)) {}
		else {
			Expression on = ps.getJoins().get(0).getOnExpression();
			indexExpression(on);
		}
	}

	private void fromMapping(Select select, PlainSelect ps) throws Exception {
		FromItem fi = ps.getFromItem();
		indexFromItem(fi);
		if (StatementUtils.noWhereClause(select)) {}
		else {
			Expression where = ps.getWhere();
			indexExpression(where);
		}
	}

	private void indexExpression(Expression expr) {
		ExpressionIndexVisitor iev = new ExpressionIndexVisitor();
		expr.accept(iev);
	}
	

	private void indexJoin(FromItem left, FromItem right) {
		JoinIndex ji = new JoinIndex();
		ji.setLeft(IndexMapping.find(left));
		ji.setRight(IndexMapping.find(right));
		if (left.getAlias() != null) {
			ji.setAliasLeft(left.getAlias().getName());
		} 
		if (right.getAlias() != null) {
			ji.setAliasRight(right.getAlias().getName());
		}
		IndexMapping.add(ji);
	}

	private void indexPlainSelect(PlainSelect ps) {
		PlainSelectIndex psi = new PlainSelectIndex();
		psi.setSource(ps);
		IndexMapping.add(psi);
	}

	private void indexFromItem(FromItem fi) {
		if (fi instanceof SubSelect) {
			SubSelect ss = (SubSelect) fi;
			SelectBody sb = ss.getSelectBody();
			Select s_ = new Select();
			s_.setSelectBody(sb);
			s_.accept(this);
		} else {
			// this is just a table, we already had index for it
		}
		Alias a = fi.getAlias();
		if (a != null) {
			String alias = a.getName();
			Index i = IndexMapping.find(fi);
			IndexMapping.link(alias, i);
		}
	}

	@Override
	public void visit(Upsert upsert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(UseStatement use) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Block block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ValuesStatement values) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DescribeStatement describe) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExplainStatement aThis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ShowStatement aThis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DeclareStatement aThis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Grant grant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateSequence createSequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AlterSequence alterSequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateFunctionalStatement createFunctionalStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateSynonym createSynonym) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AlterSession alterSession) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IfElseStatement aThis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RenameTableStatement renameTableStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PurgeStatement purgeStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AlterSystemStatement alterSystemStatement) {
		// TODO Auto-generated method stub
		
	}

}
