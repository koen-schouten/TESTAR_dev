package nl.ou.testar.tgherkin.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.Widget;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;


/**
 * Tgherkin TripleClickGesture.
 *
 */
public class TripleClickGesture extends Gesture {

    /**
     * TripleClickGesture constructor.
     * @param arguments list of arguments
     */
    public TripleClickGesture(List<Argument> arguments) {
    	super(arguments);
    }
	
    
    @Override
    public boolean gesturePossible(Widget widget, ProtocolProxy proxy, DataTable dataTable) {
    	if (getArguments().size() > 0 && getBooleanArgument(0, dataTable)) {    		 
    		// unchecked argument contains value true
    		return super.gesturePossible(widget, proxy, dataTable);
    	}    	
   		return proxy.isClickable(widget);
    }
    
    @Override
    public Set<Action> getActions(Widget widget, ProtocolProxy proxy, DataTable dataTable) {
		Set<Action> actions = new HashSet<Action>();	
    	StdActionCompiler ac = new AnnotatingActionCompiler();
    	actions.add(ac.leftTripleClickAt(widget));
    	return actions;
    }
    
    @Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
   		result.append("tripleClick");
   		result.append(argumentsToString());
    	return result.toString();    	
    }
}
