package sql2msfol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.uni.dm2schema.dm.DataModel;

import constant.ConstantMapping;
import mappings.IndexMapping;
import mappings.ValueMapping;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import visitor.StatementIndexVisitor;
import visitor.StatementValueVisitor;

public class SQL2MSFOL {
	public DataModel getDataModel() {
		return dataModel;
	}

	private DataModel dataModel;

	public void setUpDataModelFromURL(String url) throws FileNotFoundException, IOException, ParseException, Exception {
		File dataModelFile = new File(url);
		JSONArray dataModelJSONArray = (JSONArray) new JSONParser().parse(new FileReader(dataModelFile));
		DataModel context = new DataModel(dataModelJSONArray);
		this.dataModel = context;
	}

	public void map(String sql) throws JSQLParserException {
		Statement statementSql = CCJSqlParserUtil.parse(sql);
		init();
		generateConstant();
		generateIndex(statementSql);
		generateValue(statementSql);
		IndexMapping.declare();
		ValueMapping.declare();
		ConstantMapping.define();
		IndexMapping.define();
		ValueMapping.define();
//		SQL2MSFOLStatementVisitor visitor = new SQL2MSFOLStatementVisitor();
//		statementSql.accept(visitor);
//		visitor.formalize();
	}

	private void generateConstant() {
		ConstantMapping.reset();
	}

	private void generateValue(Statement sql) {
		ValueMapping.reset();
		StatementValueVisitor vsv = new StatementValueVisitor();
		sql.accept(vsv);
		renameResValue();
	}
	
	private void renameResValue() {
		// This function rename the final Value object (i.e., the single selectitem on the top of our SQL query)
		ValueMapping.renameResValue();
	}

	private void generateIndex(Statement sql) {
		IndexMapping.reset();
		StatementIndexVisitor isv = new StatementIndexVisitor();
		sql.accept(isv);
	}

	private static void init() {
		defineSort_BOOL();
		declareFunction_id();
		declareFunction_left();
		declareFunction_right();
	}

	private static void declareFunction_right() {
		System.out.println("(declare-fun right (Int) Int)");
	}

	private static void declareFunction_left() {
		System.out.println("(declare-fun left (Int) Int)");
	}

	private static void declareFunction_id() {
		System.out.println("(declare-fun id (Int) Classifier)");
	}

	private static void defineSort_BOOL() {
		System.out.println("(declare-sort BOOL 0)");
		System.out.println("(declare-const TRUE BOOL)");
		System.out.println("(declare-const FALSE BOOL)");
		System.out.println("(declare-const NULL BOOL)");
		System.out.println("(assert (not (= TRUE FALSE)))");
		System.out.println("(assert (not (= TRUE NULL)))");
		System.out.println("(assert (not (= FALSE NULL)))");
		System.out.println("(assert (forall ((x BOOL)) (or (= x TRUE) (or (= x FALSE) (= x NULL)))))");
	}

}
