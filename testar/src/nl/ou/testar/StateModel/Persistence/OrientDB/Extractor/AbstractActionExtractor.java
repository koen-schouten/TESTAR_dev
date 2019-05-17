package nl.ou.testar.StateModel.Persistence.OrientDB.Extractor;

import com.orientechnologies.orient.core.metadata.schema.OType;
import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.Exception.ExtractionException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.DocumentEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.EdgeEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.PropertyValue;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.VertexEntity;

import java.util.Set;

public class AbstractActionExtractor implements EntityExtractor<AbstractAction> {


    @Override
    public AbstractAction extract(DocumentEntity entity, AbstractStateModel abstractStateModel) throws ExtractionException {
        if (!(entity instanceof EdgeEntity)) {
            throw new ExtractionException("Abstract action extractor expects an edge entity. Instance of " + entity.getClass().toString() + " was given.");
        }
        if (!entity.getEntityClass().getClassName().equals("AbstractAction") && !entity.getEntityClass().getClassName().equals("UnvisitedAbstractAction")) {
            throw new ExtractionException("Entity of class AbstractAction or UnvisitedAbstractAction expected. Class " + entity.getEntityClass().getClassName() + " given.");
        }

        // get the action id
        PropertyValue propertyValue;
        propertyValue = entity.getPropertyValue("actionId");
        if (propertyValue.getType() != OType.STRING) {
            throw new ExtractionException("Expected string value for actionId attribute. Type " + propertyValue.getType().toString() + " given.");
        }
        String actionId = propertyValue.getValue().toString();
        AbstractAction action = new AbstractAction(actionId);

        // get the concrete action id's
        PropertyValue concreteActionIdValues = entity.getPropertyValue("concreteActionIds");
        if (concreteActionIdValues == null) {
            return null;
        }
        if (concreteActionIdValues.getType() != OType.EMBEDDEDSET) {
            throw new ExtractionException("Embedded set was expected for concrete action ids. " + concreteActionIdValues.getType().toString() + " was given.");
        }
        if (!Set.class.isAssignableFrom(concreteActionIdValues.getValue().getClass())) {
            throw new ExtractionException("Set expected for value of concrete action ids");
        }
        Set<String> concreteActionIds = (Set<String>)concreteActionIdValues.getValue();
        for (String concreteActionId : concreteActionIds) {
            action.addConcreteActionId(concreteActionId);
        }
        return action;
    }
}
