/**
 * 
 */
package nl.ou.testar.tgherkin;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.fruit.alayer.StdWidget;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.junit.Before;
import org.junit.Test;

import nl.ou.testar.tgherkin.WidgetConditionEvaluator;
import nl.ou.testar.tgherkin.gen.TgherkinLexer;
import nl.ou.testar.tgherkin.gen.WidgetConditionParser;
import nl.ou.testar.tgherkin.model.DataTable;
import nl.ou.testar.tgherkin.model.TableCell;
import nl.ou.testar.tgherkin.model.TableRow;

/**
 * Test WidgetConditionEvaluator class.
 *
 */
public class WidgetConditionEvaluatorTest {

	private Map<String, Boolean> testMap = new HashMap<String, Boolean>();
	private Widget widget;
	private DataTable dataTable;
	
	@Before
	public void setUp() throws Exception {
		// Create test widget
		widget = new StdWidget();
		widget.set(Tags.ConcreteID, "ConcreteID1");
		widget.set(Tags.Desc, "Desc1");
		widget.set(Tags.Title, "Title1");
		widget.set(Tags.Blocked, false);
		widget.set(Tags.Enabled, true);
		widget.set(Tags.Abstract_R_ID, "Abstract_R_ID1");
		widget.set(Tags.Abstract_R_T_ID, "Abstract_R_T_ID1");
		widget.set(Tags.Abstract_R_T_P_ID, "Abstract_R_T_P_ID1");
		// Create test data table
		List<TableRow> tableRows = new ArrayList<TableRow>();
		List<TableCell> tableCells = new ArrayList<TableCell>();
		tableCells.add(new TableCell("header1"));
		tableCells.add(new TableCell("header2"));
		tableCells.add(new TableCell("header3"));
		tableRows.add(new TableRow(tableCells));
		tableCells = new ArrayList<TableCell>();
		tableCells.add(new TableCell("Desc1"));
		tableCells.add(new TableCell("Title1"));
		tableCells.add(new TableCell("true"));
		tableRows.add(new TableRow(tableCells));		
		dataTable = new DataTable(tableRows);
		dataTable.beginSequence();
		// Create map with expression and expected result
		testMap.put("1 = 1", true);
		testMap.put("1 + 2 = 3", true);
		testMap.put("1 > 2", false);
		testMap.put("1 < 2", true);
		testMap.put("(1 < 2)", true);
		testMap.put("(1 + 4 < 2)", false);
		testMap.put("(1 + 4 < 2 * 4)", true);
		testMap.put("(1 + 4 * 5 > 22)", false);
		testMap.put("(1 + 8 / 4 > 2.99)", true);
		testMap.put("(2 / 4 * 8 > 3.99)", true);
		testMap.put("((2 / 4) * 8 > 3.99)", true);
		testMap.put("(2 > 1) or (4 > 15)", true);
		testMap.put("(2 > 1) and (4 > 15)", false);
		testMap.put("true", true);
		testMap.put("(true) and (false)", false);
		testMap.put("(true) and (false or true)", true);
		testMap.put("true and false or true", true);
		testMap.put("not true", false);
		testMap.put("not false", true);
		testMap.put("- 2 + 8 > 5", true);
		testMap.put("-2 + 8 > 5", true);
		testMap.put("\"test\" =  \"proef\"", false);
		testMap.put("\"test\" =  \"test\"", true);
		testMap.put("1 = 1.0", true);
		testMap.put("1 = 1.00", true);
		testMap.put("$Title =  \"Title1\"", true);
		testMap.put("matches($Title,\"Tit.*\")", true);
		testMap.put("$Title =  <header2>", true);
		testMap.put("$Title =  \"Title1\" and $Desc =  \"Desc1\"", true);
		testMap.put("$Title =  \"Title1\" and $Desc <>  \"Desc1\"", false);
	}

	@Test
	public void test() {
		Iterator<Entry<String,Boolean>> iterator = testMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String,Boolean> entry = iterator.next();
			String expression = entry.getKey();
			Boolean expectedResult = entry.getValue();
			ANTLRInputStream inputStream = new ANTLRInputStream(expression);
			TgherkinLexer lexer = new TgherkinLexer(inputStream);
			WidgetConditionParser parser = new WidgetConditionParser(new CommonTokenStream(lexer));
			Boolean result = false;
			result = (Boolean)new WidgetConditionEvaluator(widget, dataTable).visit(parser.widget_condition());
			assertEquals(expectedResult, result);
		}
		
	}

}
