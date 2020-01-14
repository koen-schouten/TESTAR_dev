package nl.ou.testar.StateModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComboBox;

import org.fruit.monkey.Main;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ORecordBytes;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Config;
import nl.ou.testar.a11y.reporting.HTMLReporter;

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

			//TODO: instead of (for) prepare a better Set difference comparison or,
			//TODO: prepare OrientDB queries to obtain the difference at DB level

			Set<String> dissapearedImages = new HashSet<>();
			Set<String> newImages = new HashSet<>();

			System.out.println("\n ---- DISSAPEARED ABSTRACT STATES ----");
			for(String s : abstractStateOne)
				if(!abstractStateTwo.contains(s)) {
					System.out.println(s);
					dissapearedImages.add(screenshotConcreteState(sessionDB, concreteStateId(sessionDB, s), "DissapearedState"));
				}

			System.out.println("\n ---- NEW ABSTRACT STATES ----");
			for(String s : abstractStateTwo)
				if(!abstractStateOne.contains(s)) {
					System.out.println(s);
					newImages.add(screenshotConcreteState(sessionDB, concreteStateId(sessionDB, s), "NewState"));
				}

			System.out.println("\n ---- DISSAPEARED ABSTRACT ACTIONS ----");
			for(String s : abstractActionOne)
				if(!abstractActionTwo.contains(s))
					System.out.println(s);

			System.out.println("\n ---- NEW ABSTRACT ACTIONS ----");
			for(String s : abstractActionTwo)
				if(!abstractActionOne.contains(s))
					System.out.println(s);
			
			createHTMLreport(dissapearedImages, newImages);

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

	private static String concreteStateId(ODatabaseSession sessionDB, String abstractId) {
		OResultSet resultSet = sessionDB.query("SELECT FROM AbstractState WHERE stateId = \"" 
				+ abstractId + "\" LIMIT 1");

		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				try {
					for(String concreteStateId : (Set<String>) modelVertex.getProperty("concreteStateIds"))
						if(!concreteStateId.isEmpty())
							return concreteStateId;
				}catch (Exception e) {System.out.println("ERROR: ModelDifferenceManager concreteStateId() ");}

			}
		}
		resultSet.close();

		return "";
	}

	private static String screenshotConcreteState(ODatabaseSession sessionDB, String concreteId, String folderName) {
		OResultSet resultSet = sessionDB.query("SELECT FROM ConcreteState WHERE ConcreteIDCustom = \"" 
				+ concreteId + "\" LIMIT 1");

		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				String sourceScreenshot = "n" + formatId(modelVertex.getIdentity().toString());
				return processScreenShot(modelVertex.getProperty("screenshot"), sourceScreenshot, folderName);
			}
		}
		resultSet.close();
		return "";
	}

	// this helper method formats the @RID property into something that can be used in a web frontend
	private static String formatId(String id) {
		if (id.indexOf("#") != 0) return id; // not an orientdb id
		id = id.replaceAll("[#]", "");
		return id.replaceAll("[:]", "_");
	}

	/**
	 * This method saves screenshots to disk.
	 * @param recordBytes
	 * @param identifier
	 */
	private static String processScreenShot(ORecordBytes recordBytes, String identifier, String folderName) {
		if (!Main.outputDir.substring(Main.outputDir.length() - 1).equals(File.separator)) {
			Main.outputDir += File.separator;
		}

		// see if we have a directory for the screenshots yet
		File screenshotDir = new File(Main.outputDir + "ModelDiff" + /*File.separator + folderName +*/ File.separator);

		if (!screenshotDir.exists()) {
			screenshotDir.mkdir();
		}

		// save the file to disk
		File screenshotFile = new File( screenshotDir, identifier + ".png");
		if (screenshotFile.exists()) {
			try {
				return screenshotFile.getCanonicalPath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream outputStream = new FileOutputStream(screenshotFile.getCanonicalPath());
			outputStream.write(recordBytes.toStream());
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return screenshotFile.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	private static void createHTMLreport(Set<String> dissapearedImages, Set<String> newImages) {
		try {
			String[] HEADER = new String[] {
					"<!DOCTYPE html>",
					"<html>",
					"<head>",
					"<title>TESTAR State Model difference report</title>",
					"</head>",
					"<body>"
			};

			String htmlReportName = Main.outputDir + "ModelDiff" + File.separator + "DifferenceReport.html";

			PrintWriter out = new PrintWriter(new File(htmlReportName).getCanonicalPath(), HTMLReporter.CHARSET);

			for(String s:HEADER){
				out.println(s);
				out.flush();
			}

			out.println("<h4> Dissapeared Abstract States </h4>");
			out.flush();

			for(String path : dissapearedImages) {
				out.println("<p><img src=\""+path+"\"></p>");
				out.flush();
			}
			
			out.println("<h4> New Abstract States </h4>");
			out.flush();

			for(String path : newImages) {
				out.println("<p><img src=\""+path+"\"></p>");
				out.flush();
			}
			
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
