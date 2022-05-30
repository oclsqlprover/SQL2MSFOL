package visitor;

import java.util.List;

import index.Index;
import mappings.IndexMapping;
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

public class StatementValueVisitor implements StatementVisitor {
	
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
		try {
			if (StatementUtils.noFromClause(select)) {
				// ONLY SELECT
				List<SelectItem> sis = ps.getSelectItems();
				for (SelectItem si : sis) {
					SelectExpressionItem sei = (SelectExpressionItem) si;
					Expression expr = sei.getExpression();
					Index i = IndexMapping.getPlainSelectIndex(ps);
					valExpression(expr, null, i);
				}
			}
			else {
				FromItem fi = ps.getFromItem();
				valFromItem(fi);
				if (StatementUtils.noJoinClause(select)) {
					if (StatementUtils.noWhereClause(select)) {
						// SELECT FROM
						List<SelectItem> sis = ps.getSelectItems();
						for (SelectItem si : sis) {
							SelectExpressionItem sei = (SelectExpressionItem) si;
							Expression expr = sei.getExpression();
							Index parent = IndexMapping.getPlainSelectIndex(ps);
							Index source = IndexMapping.find(fi);
							valExpression(expr, source, parent);
						}
					}
					else {
						// SELECT FROM WHERE
						Expression where = ps.getWhere();
						Index source = IndexMapping.find(fi);
						valExpression(where, source, source);
						List<SelectItem> sis = ps.getSelectItems();
						for (SelectItem si : sis) {
							SelectExpressionItem sei = (SelectExpressionItem) si;
							Expression expr = sei.getExpression();
							Index parent = IndexMapping.getPlainSelectIndex(ps);
							valExpression(expr, source, parent);
						}
					}
				} 
				else {
					FromItem left = ps.getFromItem();
					FromItem right = ps.getJoins().get(0).getRightItem();
					valFromItem(right);
					if (StatementUtils.noWhereClause(select)) {
					}
					else {
						// SELECT FROM JOIN + WHERE
						Expression where = ps.getWhere();
						Index i = IndexMapping.getJoinIndex(IndexMapping.find(left), IndexMapping.find(right));
						valExpression(where, i, i);
					}
					if (StatementUtils.noOnClause(select)) {}
					else {
						Expression on = ps.getJoins().get(0).getOnExpression();
						Index i = IndexMapping.getJoinIndex(IndexMapping.find(left), IndexMapping.find(right));
						valExpression(on, i, i);
					}
					List<SelectItem> sis = ps.getSelectItems();
					for (SelectItem si : sis) {
						SelectExpressionItem sei = (SelectExpressionItem) si;
						Expression expr = sei.getExpression();
						Index parent = IndexMapping.getPlainSelectIndex(ps);
						Index source = IndexMapping.getJoinIndex(IndexMapping.find(left), IndexMapping.find(right));
						valExpression(expr, source, parent);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void valFromItem(FromItem fi) {
		if (fi instanceof SubSelect) {
			SubSelect ss = (SubSelect) fi;
			SelectBody sb = ss.getSelectBody();
			Select s_ = new Select();
			s_.setSelectBody(sb);
			s_.accept(this);
		} else {
			// this is just a table, we already had value for it
		}
	}

	private void valExpression(Expression expr, Index source, Index parent) {
		ExpressionValueVisitor vev = new ExpressionValueVisitor();
		vev.setSource(source);
		vev.setParent(parent);
		expr.accept(vev);
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
