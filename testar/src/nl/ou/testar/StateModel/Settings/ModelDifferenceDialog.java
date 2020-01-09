package nl.ou.testar.StateModel.Settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Config;

public class ModelDifferenceDialog extends JDialog {
	
	private static final long serialVersionUID = 7890181945543399039L;

	// orient db instance that will create database sessions
	private transient OrientDB orientDB;

	// orient db configuration object
	private transient Config dbConfig;

	public ModelDifferenceDialog(String storeType, String storeServer) {
		initialize(storeType, storeServer);
	}

	private void calculateModelDifference() {
		String appNameOne, appVerOne, appNameTwo, appVerTwo = "";
		if((appNameOne = textApplicationNameOne.getText())==null || (appVerOne = textApplicationVersionOne.getText())==null
				|| (appNameTwo = textApplicationNameTwo.getText())==null || (appVerTwo = textApplicationVersionTwo.getText())==null)
			return;

		String dbConnection = connectionStuff();

		try (ODatabaseSession sessionDB = orientDB.open(dbConnection, dbConfig.getUser(), dbConfig.getPassword())){

			String modelIdOne = AbstractStateModelInfo(sessionDB, appNameOne, appVerOne);
			Set<String> abstractStateOne = new HashSet<>(abstractState(sessionDB, modelIdOne));
			
			String modelIdTwo = AbstractStateModelInfo(sessionDB, appNameTwo, appVerTwo);
			Set<String> abstractStateTwo = new HashSet<>(abstractState(sessionDB, modelIdTwo));
			
			System.out.println("\n ---- DISSAPEARED ABSTRACT STATES ----");
			for(String s : abstractStateOne)
				if(!abstractStateTwo.contains(s))
					System.out.println(s);
			
			System.out.println("\n ---- NEW ABSTRACT STATES ----");
			for(String s : abstractStateTwo)
				if(!abstractStateOne.contains(s))
					System.out.println(s);
			
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			orientDB.close();
		}

	}
	
	private String AbstractStateModelInfo(ODatabaseSession sessionDB, String AppName, String AppVer) {
		OResultSet resultSet = sessionDB.query("SELECT FROM AbstractStateModel where applicationName=\"" + AppName
				+ "\" and applicationVersion=\"" + AppVer + "\"");

		String modelIdentifier = "";
		
		while (resultSet.hasNext()) {
			OResult result = resultSet.next();
			// we're expecting a vertex
			if (result.isVertex()) {
				Optional<OVertex> op = result.getVertex();
				if (!op.isPresent()) continue;
				OVertex modelVertex = op.get();

				System.out.println("StateModel: " + AppName + " " + AppVer);
				System.out.println("Collecting DB State Model data...");

				System.out.println("JSON: " + result.toJSON());
				System.out.println("Edges: " + modelVertex.getEdges(ODirection.BOTH));
				
				modelIdentifier = modelVertex.getProperty("modelIdentifier");

				break;
			}
		}
		resultSet.close();
		
		return modelIdentifier;
	}
	
	private Set<String> abstractState(ODatabaseSession sessionDB, String modelIdentifier) {
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
	
	private void obtainAvailableDatabases() {
		dbConfig = new Config();
		dbConfig.setConnectionType(textFieldStoreType.getText());
		dbConfig.setServer(textFieldStoreServer.getText());
		dbConfig.setUser(textFieldRoot.getText());
		dbConfig.setPassword(getPassword());

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
	
	private String connectionStuff() {
		dbConfig = new Config();
		dbConfig.setConnectionType(textFieldStoreType.getText());
		dbConfig.setServer(textFieldStoreServer.getText());
		dbConfig.setUser(textFieldRoot.getText());
		dbConfig.setPassword(getPassword());
		dbConfig.setDatabase(listDatabases.getSelectedItem().toString());

		orientDB = new OrientDB(dbConfig.getConnectionType() + ":" + dbConfig.getServer(), 
				dbConfig.getUser(), dbConfig.getPassword(), OrientDBConfig.defaultConfig());

		return dbConfig.getConnectionType() + ":" + dbConfig.getServer() + "/database/" + dbConfig.getDatabase();
	}

	/**
	 * Convert password field to string.
	 * @return password as String.
	 */
	private String getPassword() {
		StringBuilder result= new StringBuilder();
		for(char c : textFieldPassword.getPassword()) {
			result.append(c);
		}
		return  result.toString();
	}   

	private void closeOrientDB() {
		if(orientDB!=null && orientDB.isOpen())
			orientDB.close();
	}
	
	private JLabel labelStoreType = new JLabel("DataStoreType");
	private JLabel labelStoreServer = new JLabel("DataStoreServer");
	private JLabel labelRoot = new JLabel("RootUser");
	private JLabel labelPassword = new JLabel("RootPassword");
	private JLabel labelStoreDB = new JLabel("Existing DB");
	
	private JTextField textFieldStoreType = new JTextField();
	private JTextField textFieldStoreServer = new JTextField();
	private JTextField textFieldRoot = new JTextField();
	private JPasswordField textFieldPassword = new JPasswordField();
	
	private JButton buttonConnect = new JButton("Connect");
	private JButton buttonModelDiff = new JButton("Model-Diff");
	private JButton buttonCancel = new JButton("Cancel Artefact");
	
	private JComboBox<String> listDatabases = new JComboBox<>();
	
	private JLabel labelApplicationNameOne = new JLabel("1. Application Name");
	private JLabel labelApplicationVersionOne = new JLabel("1. Application Version");
	private JLabel labelApplicationNameTwo = new JLabel("2. Application Name");
	private JLabel labelApplicationVersionTwo = new JLabel("2. Application Version");
	private JTextField textApplicationNameOne = new JTextField();
	private JTextField textApplicationVersionOne = new JTextField();
	private JTextField textApplicationNameTwo = new JTextField();
	private JTextField textApplicationVersionTwo = new JTextField();
	
	private void initialize(String storeType, String storeServer) {

		setTitle("TESTAR State Model Difference");

		setSize(1000, 500);
		setLayout(null);
		setVisible(true);
		setLocationRelativeTo(null);

		labelStoreType.setBounds(10,14,150,27);
		add(labelStoreType);
		textFieldStoreType.setBounds(160,14,125,27);
		textFieldStoreType.setText(storeType);
		add(textFieldStoreType);

		labelStoreServer.setBounds(10,52,150,27);
		add(labelStoreServer);
		textFieldStoreServer.setBounds(160,52,125,27);
		textFieldStoreServer.setText(storeServer);
		add(textFieldStoreServer);

		labelRoot.setBounds(10,90,150,27);
		add(labelRoot);
		textFieldRoot.setBounds(160,90,125,27);
		textFieldRoot.setText("root");
		add(textFieldRoot);

		labelPassword.setBounds(10,128,150,27);
		add(labelPassword);
		textFieldPassword.setBounds(160,128,125,27);
		add(textFieldPassword);

		buttonConnect.setBounds(330, 166, 150, 27);
		buttonConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				obtainAvailableDatabases();
			}
		});
		add(buttonConnect);

		labelStoreDB.setBounds(10,166,150,27);
		add(labelStoreDB);
		listDatabases.setBounds(160,166,150,27);
		add(listDatabases);

		labelApplicationNameOne.setBounds(10,204,150,27);
		add(labelApplicationNameOne);
		textApplicationNameOne.setBounds(160,204,325,27);
		textApplicationNameOne.setText("NombreApp");
		add(textApplicationNameOne);

		labelApplicationVersionOne.setBounds(10,242,150,27);
		add(labelApplicationVersionOne);
		textApplicationVersionOne.setBounds(160,242,325,27);
		textApplicationVersionOne.setText("VersionApp");
		add(textApplicationVersionOne);
		
		labelApplicationNameTwo.setBounds(510,204,150,27);
		add(labelApplicationNameTwo);
		textApplicationNameTwo.setBounds(660,204,325,27);
		textApplicationNameTwo.setText("NombreApp");
		add(textApplicationNameTwo);

		labelApplicationVersionTwo.setBounds(510,242,150,27);
		add(labelApplicationVersionTwo);
		textApplicationVersionTwo.setBounds(660,242,325,27);
		textApplicationVersionTwo.setText("VersionApp");
		add(textApplicationVersionTwo);

		buttonModelDiff.setBounds(330, 356, 150, 27);
		buttonModelDiff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calculateModelDifference();
			}
		});
		add(buttonModelDiff);

		buttonCancel.setBounds(330, 408, 150, 27);
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeOrientDB();
				dispose();
			}
		});
		add(buttonCancel);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				closeOrientDB();
			}
		});
	}
	
}
