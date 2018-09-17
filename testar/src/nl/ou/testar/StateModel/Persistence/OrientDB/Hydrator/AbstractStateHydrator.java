package nl.ou.testar.StateModel.Persistence.OrientDB.Hydrator;

import com.orientechnologies.orient.core.metadata.schema.OType;
import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractState;
import nl.ou.testar.StateModel.Exception.HydrationException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Property;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.TypeConvertor;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.VertexEntity;
import nl.ou.testar.StateModel.Util.HydrationHelper;
import org.fruit.alayer.Tag;
import org.fruit.alayer.TaggableBase;

import java.util.Set;

public class AbstractStateHydrator implements EntityHydrator<VertexEntity> {

    @Override
    public void hydrate(VertexEntity target, Object source) throws HydrationException {
        if (!(source instanceof AbstractState)) {
            throw new HydrationException();
        }

        // first make sure the identity property is set
        Property identifier = target.getEntityClass().getIdentifier();
        if (identifier == null) {
            throw new HydrationException();
        }
        // make sure the java and orientdb property types are compatible
        OType identifierType = TypeConvertor.getInstance().getOrientDBType(((AbstractState) source).getStateId().getClass());
        if (identifierType != identifier.getPropertyType()) {
            throw new HydrationException();
        }
        target.addPropertyValue(identifier.getPropertyName(), identifier.getPropertyType(), ((AbstractState) source).getStateId());

        // we want to loop through the properties on the entity class
        for (Property property : target.getEntityClass().getProperties()) {
            //@todo for now we assume that the tag name and the property names match
            // ideally we want to add a mapper for this later
            // also, this way of looking for tags is terribly inefficient due to the use of taggablebase,
            // which makes it so we use a loop within a loop basically.
            // we should refactor this at some point in time
            if (property.isReadOnly()) {
                continue;
            }

            Tag tag = HydrationHelper.getTag(((AbstractState) source).getAttributes(), property.getPropertyName());
            if (tag == null && (property.isMandatory() || !property.isNullable())) {
                throw new HydrationException();
            }
            if (tag != null && ((AbstractState) source).getAttributes().get(tag) == null && !property.isNullable()) {
                throw new HydrationException();
            }

            if (!HydrationHelper.typesMatch(property, ((AbstractState) source).getAttributes().get(tag).getClass())) {
                throw new HydrationException();
            }

            target.addPropertyValue(property.getPropertyName(), property.getPropertyType(), ((AbstractState) source).getAttributes().get(tag));
        }

        // now the reverse for those state attributes that are not yet set on the entity, meaning they are not part of
        // the orientdb schema, which is perfectly fine
        TaggableBase attributes = ((AbstractState) source).getAttributes();
        for (Tag<?> tag :attributes.tags()) {
            if (HydrationHelper.getProperty(target.getEntityClass().getProperties(), tag.name()) != null) {
                // skip, we already processed this one
                continue;
            }

            // we simply add a property for each tag
            target.addPropertyValue(tag.name(), TypeConvertor.getInstance().getOrientDBType(attributes.get(tag).getClass()), attributes.get(tag));
        }

        // specifically for abstract states we need to add the ids of unvisited actions
        Property unvisitedActions = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "unvisitedActions");
        if (unvisitedActions == null) {
            throw new HydrationException();
        }
        if (!((AbstractState) source).getUnvisitedActionIds().isEmpty()) {
            target.addPropertyValue(unvisitedActions.getPropertyName(), unvisitedActions.getPropertyType(), ((AbstractState) source).getUnvisitedActionIds());
        }

    }


}
