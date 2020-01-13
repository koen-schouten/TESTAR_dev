package nl.ou.testar.StateModel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComboBox;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Config;

public class ModelDifferenceManager {
	
	private ModelDifferenceManager() {}
	
	// orient db instance that will create database sessions
	private static OrientDB orientDB;

	// orient db configuration object
	private static Config dbConfig;
	
	public static void obtainAvailableDatabases(String storeType, String storeServer, String root, String passField,
			JComboBox<String> listDatabases) {
		dbConfig = new Config();
		dbConfig.setConnectionType(storeType);
		dbConfig.setServer(storeServer);
		dbConfig.setUser(root);
		dbConfig.setPassword(passField);

		try{

			listDatabases.removeAllItems();

			orientDB = new OrientDB(dbConfig.getConnectionType() + ":" + dbConfig.getServer(), 
					dbConfig.getUser(), dbConfig.getPassword(), OrientDBConfig.defaultConfig());

			if(!orientDB.list().isEmpty())
				for(String database : orientDB.list())
					listDatabases.addItem(database);

		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			orientDB.close();
		}

	}
	
	public static String connectionStuff(String storeType, String storeServer, String root, String passField,
			String database) {
		dbConfig = new Config();
		dbConfig.setConnectionType(storeType);
		dbConfig.setServer(storeServer);
		dbConfig.setUser(root);
		dbConfig.setPassword(passField);
		dbConfig.setDatabase(database);

		orientDB = new OrientDB(dbConfig.getConnectionType() + ":" + dbConfig.getServer(), 
				dbConfig.getUser(), dbConfig.getPassword(), OrientDBConfig.defaultConfig());

		return dbConfig.getConnectionType() + ":" + dbConfig.getServer() + "/database/" + dbConfig.getDatabase();
	}
	
	public static void closeOrientDB() {
		if(orientDB!=null && orientDB.isOpen())
			orientDB.close();
	}
	
	public static void calculateModelDifference(String storeType, String storeServer, String root, String passField,
			String database, String appNameOne, String appVerOne, String appNameTwo, String appVerTwo) {
		if(appNameOne == null || appVerOne == null || appNameTwo == null || appVerTwo == null)
			return;

		String dbConnection = connectionStuff(storeType, storeServer, root, passField, database);

		try (ODatabaseSession sessionDB = orientDB.open(dbConnection, dbConfig.getUser(), dbConfig.getPassword())){

			String modelIdOne = abstractStateModelInfo(sessionDB, appNameOne, appVerOne);
			Set<String> abstractStateOne = new HashSet<>(abstractState(sessionDB, modelIdOne));
			Set<String> abstractActionOne = new HashSet<>(abstractAction(sessionDB, modelIdOne));
			
			String modelIdTwo = abstractStateModelInfo(sessionDB, appNameTwo, appVerTwo);
			Set<String> abstractStateTwo = new HashSet<>(abstractState(sessionDB, modelIdTwo));
			Set<String> abstractActionTwo = new HashSet<>(abstractAction(sessionDB, modelIdTwo));
			
			System.out.println("\n ---- DISSAPEARED ABSTRACT STATES ----");
			for(String s : abstractStateOne)
				if(!abstractStateTwo.contains(s))
					System.out.println(s);
			
			System.out.println("\n ---- NEW ABSTRACT STATES ----");
			for(String s : abstractStateTwo)
				if(!abstractStateOne.contains(s))
					System.out.println(s);
			
			System.out.println("\n ---- DISSAPEARED ABSTRACT ACTIONS ----");
			for(String s : abstractActionOne)
				if(!abstractActionTwo.contains(s))
					System.out.println(s);
			
			System.out.println("\n ---- NEW ABSTRACT ACTIONS ----");
			for(String s : abstractActionTwo)
				if(!abstractActionOne.contains(s))
					System.out.println(s);
			
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			orientDB.close();
		}

	}
	
	private static String abstractStateModelInfo(ODatabaseSession sessionDB, String appName, String appVer) {
		OResultSet resultSet = sessionDB.query("SELECT FROM AbstractStateModel where applicationName=\"" + appName
				+ "\" and applicationVersion=\"" + appVer + "\"");
		
		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				System.out.println("StateModel: " + appName + " " + appVer);
				System.out.println("Collecting DB State Model data...");

				System.out.println("JSON: " + result.toJSON());
				System.out.println("Edges: " + modelVertex.getEdges(ODirection.BOTH));
				
				return modelVertex.getProperty("modelIdentifier");
			}
		}
		resultSet.close();
		
		return "";
	}
	
	private static Set<String> abstractState(ODatabaseSession sessionDB, String modelIdentifier) {
		OResultSet resultSet = sessionDB.query("SELECT FROM AbstractState WHERE modelIdentifier = \"" 
				+ modelIdentifier + "\"");

		Set<String> abstractStates = new HashSet<>();
		
		System.out.println("**** Existing AbstractStates ****");
		
		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				System.out.println("JSON: " + result.toJSON());
				System.out.println("Edges: " + modelVertex.getEdges(ODirection.BOTH));
				abstractStates.add(modelVertex.getProperty("stateId"));
			}
		}
		resultSet.close();
		
		return abstractStates;
	}
	
	private static Set<String> abstractAction(ODatabaseSession sessionDB, String modelIdentifier) {
		OResultSet resultSet = sessionDB.query("SELECT FROM AbstractAction WHERE modelIdentifier = \"" 
				+ modelIdentifier + "\"");

		Set<String> abstractActions = new HashSet<>();
		
		System.out.println("**** Existing AbstractActions ****");
		
		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isEdge()) {
				Optional<OEdge> op = result.getEdge();
				if (!op.isPresent()) continue;
				OEdge modelEdge = op.get();

				System.out.println("JSON: " + result.toJSON());
				abstractActions.add(modelEdge.getProperty("actionId"));
			}
		}
		resultSet.close();
		
		return abstractActions;
	}
	
	/*private Set<String> concreteState(ODatabaseSession sessionDB, String abstractedBy) {
		OResultSet resultSet = sessionDB.query("SELECT FROM ConcreteState WHERE AbstractIDCustom = \"" 
				+ abstractedBy + "\"");

		Set<String> concreteStates = new HashSet<>();
		
		System.out.println("\n\n\n\n");
		
		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				System.out.println("JSON: " + result.toJSON());
				System.out.println("Edges: " + modelVertex.getEdges(ODirection.BOTH));
				concreteStates.add(modelVertex.getProperty("stateId"));
			}
		}
		resultSet.close();
		
		return concreteStates;
	}*/

}
