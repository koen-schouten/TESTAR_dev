package nl.ou.testar.SimpleGuiStateGraph.strategy.actions;

import nl.ou.testar.SimpleGuiStateGraph.strategy.StrategyNode;
import nl.ou.testar.SimpleGuiStateGraph.strategy.actionTypes.StrategyNodeAction;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import java.util.ArrayList;

public class SnRandomUnexecutedAction extends StrategyNodeAction {

    public SnRandomUnexecutedAction(ArrayList<StrategyNode> children) {
        super(children);
    }

    @Override
    public Action getAction(State state) {
        return state.getRandomAction("UNEX");
    }

}