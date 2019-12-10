package it.unibo.csr.big.cubeload;

import it.unibo.csr.big.cubeload.generator.OlapGenerator;
import it.unibo.csr.big.cubeload.generator.Profile;
import it.unibo.csr.big.cubeload.io.XMLReader;
import it.unibo.csr.big.cubeload.io.XMLWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class displays the user interface and handles 
 * all interactions with it. 
 * @author Luca Spadazzi
 */
@SuppressWarnings("serial")
public class Main extends javax.swing.JFrame
{

    public Main()
    {
    	setResizable(false);
        initComponents();
    }

    private void initComponents()
    {
        globalParametersPanel = new javax.swing.JPanel();
        maxMeasuresLabel = new javax.swing.JLabel();
        minReportSizeLabel = new javax.swing.JLabel();
        maxReportSizeLabel = new javax.swing.JLabel();
        surprisingQueriesLabel = new javax.swing.JLabel();
        maxMeasuresTextField = new javax.swing.JTextField();
        minReportSizeTextField = new javax.swing.JTextField();
        maxReportSizeTextField = new javax.swing.JTextField();
        surprisingQueriesTextField = new javax.swing.JTextField();
        profileParametersPanel = new javax.swing.JPanel();
        
        profileNameLabel = new javax.swing.JLabel();
        seedQueriesLabel = new javax.swing.JLabel();
        minLengthLabel = new javax.swing.JLabel();
        maxLengthLabel = new javax.swing.JLabel();
        numSessionLabel = new javax.swing.JLabel();
        yearPromptLabel = new javax.swing.JLabel();
        segregationLabel = new javax.swing.JLabel();

        profileNameTextField = new javax.swing.JTextField();
        seedQueriesTextField = new javax.swing.JTextField();
        minLengthTextField = new javax.swing.JTextField();
        maxLengthTextField = new javax.swing.JTextField();
        numSessionTextField = new javax.swing.JTextField();
        yearPromptTextField = new javax.swing.JTextField();
        segregationCheckBox = new javax.swing.JCheckBox();
        
        saveProfileButton = new javax.swing.JButton();
        loadProfilesButton = new javax.swing.JButton();
        profilesScrollPane = new javax.swing.JScrollPane();
        profilesTextArea = new javax.swing.JTextArea();
        profilesTextArea.setEditable(false);
        inputFilesPanel = new javax.swing.JPanel();
        chooseCSVButton = new javax.swing.JButton();
        chooseCSVTextField = new javax.swing.JTextField();
        chooseSchemaButton = new javax.swing.JButton();
        chooseSchemaTextField = new javax.swing.JTextField();
        cubesScrollPane = new javax.swing.JScrollPane();
        cubesTextArea = new javax.swing.JTextArea();
        selectCubeLabel = new javax.swing.JLabel();
        selectCubeTextField = new javax.swing.JTextField();
        generateWorkloadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        globalParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Global parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));

        maxMeasuresLabel.setText("Max. num. of measures per query:");

        minReportSizeLabel.setText("Min. report size of seed queries:");

        maxReportSizeLabel.setText("Max. report size of seed queries:");

        surprisingQueriesLabel.setText("Num. of \"surprising\" queries:");
        
        javax.swing.GroupLayout globalParametersPanelLayout = new javax.swing.GroupLayout(globalParametersPanel);
        globalParametersPanel.setLayout(globalParametersPanelLayout);
        globalParametersPanelLayout.setHorizontalGroup(
            globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, globalParametersPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxMeasuresLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(minReportSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(maxReportSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(surprisingQueriesLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxReportSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(surprisingQueriesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minReportSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxMeasuresTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        globalParametersPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxReportSizeTextField, maxMeasuresTextField, minReportSizeTextField, surprisingQueriesTextField});
        
        globalParametersPanelLayout.setVerticalGroup(
                globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(globalParametersPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(maxMeasuresLabel)
                        .addComponent(maxMeasuresTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(minReportSizeLabel)
                        .addComponent(minReportSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(maxReportSizeLabel)
                        .addComponent(maxReportSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(globalParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(surprisingQueriesLabel)
                        .addComponent(surprisingQueriesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

        profileParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Profile parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));

        profileNameLabel.setText("Profile name:");

        seedQueriesLabel.setText("Number of session-seed queries:");

        minLengthLabel.setText("Minimum session length:");

        maxLengthLabel.setText("Maximum session length");

        numSessionLabel.setText("Number of sessions:");

        yearPromptLabel.setText("Fraction of seed queries with year prompt [0 ÷ 1]:");

        segregationLabel.setText("Segregation predicate:");

        segregationCheckBox.setText("Present");

        saveProfileButton.setText("Save profile");
        saveProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProfileButtonActionPerformed(evt);
            }
        });

        loadProfilesButton.setText("Load profiles");
        loadProfilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProfilesButtonActionPerformed(evt);
            }
        });
        
        profilesTextArea.setColumns(20);
        profilesTextArea.setRows(5);
        profilesScrollPane.setViewportView(profilesTextArea);

        javax.swing.GroupLayout profileParametersPanelLayout = new javax.swing.GroupLayout(profileParametersPanel);
        profileParametersPanel.setLayout(profileParametersPanelLayout);
        profileParametersPanelLayout.setHorizontalGroup(
            profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profileParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(profilesScrollPane)
                    .addGroup(profileParametersPanelLayout.createSequentialGroup()
                        .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(profileNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(seedQueriesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(minLengthLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(maxLengthLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(numSessionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(yearPromptLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(segregationLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(seedQueriesTextField)
                            .addComponent(minLengthTextField)
                            .addComponent(maxLengthTextField)
                            .addComponent(numSessionTextField)
                            .addComponent(yearPromptTextField)
                            .addComponent(profileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .addComponent(segregationCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(profileParametersPanelLayout.createSequentialGroup()
                        .addComponent(saveProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(loadProfilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        profileParametersPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {seedQueriesTextField, maxLengthTextField, minLengthTextField, numSessionTextField, profileNameTextField, yearPromptTextField});

        profileParametersPanelLayout.setVerticalGroup(
            profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profileParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileNameLabel)
                    .addComponent(profileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(seedQueriesLabel)
                    .addComponent(seedQueriesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minLengthLabel)
                    .addComponent(minLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxLengthLabel)
                    .addComponent(maxLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numSessionLabel)
                    .addComponent(numSessionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yearPromptLabel)
                    .addComponent(yearPromptTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(segregationLabel)
                    .addComponent(segregationCheckBox))
                .addGap(18, 18, 18)
                .addGroup(profileParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveProfileButton)
                    .addComponent(loadProfilesButton))
                .addGap(18, 18, 18)
                .addComponent(profilesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addContainerGap())
        );

        inputFilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));

        chooseCSVButton.setText("Choose CSV path");
        chooseCSVButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseCSVButtonActionPerformed(evt);
            }
        });

        chooseCSVTextField.setEditable(false);

        chooseSchemaButton.setText("Choose schema");
        chooseSchemaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseSchemaButtonActionPerformed(evt);
            }
        });
        
        chooseSchemaTextField.setEditable(false);

        cubesTextArea.setColumns(20);
        cubesTextArea.setRows(5);
        cubesTextArea.setEditable(false);
        cubesScrollPane.setViewportView(cubesTextArea);

        selectCubeLabel.setText("Select cube:");

        javax.swing.GroupLayout inputFilesPanelLayout = new javax.swing.GroupLayout(inputFilesPanel);
        inputFilesPanel.setLayout(inputFilesPanelLayout);
        inputFilesPanelLayout.setHorizontalGroup(
            inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputFilesPanelLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(selectCubeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectCubeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(inputFilesPanelLayout.createSequentialGroup()
                        .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cubesScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputFilesPanelLayout.createSequentialGroup()
                                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(chooseCSVButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(chooseSchemaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chooseSchemaTextField)
                                    .addComponent(chooseCSVTextField))))
                        .addContainerGap())))
        );
        inputFilesPanelLayout.setVerticalGroup(
            inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseCSVButton)
                    .addComponent(chooseCSVTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseSchemaButton)
                    .addComponent(chooseSchemaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cubesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectCubeLabel)
                    .addComponent(selectCubeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        generateWorkloadButton.setText("Generate Workload");
        generateWorkloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	generateWorkloadButtonActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(inputFilesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(globalParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(profileParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(generateWorkloadButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputFilesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(globalParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(profileParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateWorkloadButton, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }

    /**
     * When che "Choose CSV path" button is clicked, the user must select
     * the directory containing the CSV files with the hierarchies data.
     * @param evt
     */
    private void chooseCSVButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	JFileChooser fc = new JFileChooser();
        File f = new File("./");
        
        fc.setCurrentDirectory(f);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.showOpenDialog(null);
        
        csvPath = fc.getSelectedFile().getAbsolutePath();
        chooseCSVTextField.setText(csvPath);
    }

    /**
     * When the "Choose schema" button is clicked, the user must select
     * an XML file containing the multidimensional schema to be used.
     * @param evt
     */
    private void chooseSchemaButtonActionPerformed(java.awt.event.ActionEvent evt)
    {                                             
        JFileChooser fc = new JFileChooser();
        File f = new File("./");
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML file", "xml");
        fc.addChoosableFileFilter(filter);
        
        fc.setCurrentDirectory(f);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showOpenDialog(null);
        
        schemaPath = fc.getSelectedFile().getAbsolutePath();
        chooseSchemaTextField.setText(schemaPath);
        
        XMLReader reader = new XMLReader();
        List<String> cubes = new ArrayList<String>();
        
        cubesTextArea.setText("");
        
		try
		{
			cubes = reader.getCubeNames(schemaPath);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this, "The selected file could not be read.", "Error message", JOptionPane.ERROR_MESSAGE);
			maxCube = 0;
			return;
		}
        
		maxCube = cubes.size();
		
        for (int i = 1; i <= maxCube; ++i)
        {
        	cubesTextArea.append(i + ". " + cubes.get(i - 1) + "\n");
        	cubeNames.add(cubes.get(i - 1));
        }
        if (cubes.size()==0)
        {
        	JOptionPane.showMessageDialog(this, "No cubes could be detected in the selected file.", "Error message", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * When the "Save profile" button is clicked, a syntactic check is performed
     * on the profile parameters chosen: if all parameters are correct, the profile
     * is saved; otherwise, incorrect fields are deleted.
     * @param evt
     */
    private void saveProfileButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	try
    	{
	    	boolean correct = true;
	    	String profileName = profileNameTextField.getText();
	    	int seedQueries = Integer.valueOf(seedQueriesTextField.getText());
	    	int minLength = Integer.valueOf(minLengthTextField.getText());
	    	int maxLength = Integer.valueOf(maxLengthTextField.getText());
	    	int numSessions = Integer.valueOf(numSessionTextField.getText());
	    	double yearPrompt = Double.valueOf(yearPromptTextField.getText().replace(',', '.'));
	    	
	    	String errors = "";
	    	
	    	for (char c : ILLEGAL_CHARACTERS)
	    	{
	    		String s = "" + c;
	    		
	    		if (profileName.contains(s))
	    		{
	    			errors += "The profile nam econtains illegal characters.\n";
	    			correct = false;
	    			profileName = profileName.replace(s, "");
	    			profileNameTextField.setText(profileName);
	    		}
	    	}
	    	
	    	if (seedQueries <= 0)
	    	{
	    		seedQueriesTextField.setText("");
	    		errors += "The number of session-seed queries must be higher than 0.\n";
	    		correct = false;
	    	}
	    	
	    	if (minLength <= 0)
	    	{
	    		minLengthTextField.setText("");
	    		errors += "The minimum length of sessions must be higher than 0.\n";
	    		correct = false;
	    	}
	    	
	    	if (maxLength <= 0 || maxLength < minLength)
	    	{
	    		maxLengthTextField.setText("");
	    		errors += "The maximum length of sessions must be higher (or equal) than the minimum length.\n";
	    		correct = false;
	    	}
	    	
	    	if (numSessions <= 0)
	    	{
	    		numSessionTextField.setText("");
	    		errors += "The number of sessions must be higher than 0.\n";
	    		correct = false;
	    	}
	    	
	    	if (yearPrompt < 0 || yearPrompt > 1)
	    	{
	    		yearPromptTextField.setText("");
	    		errors += yearPrompt + "The fraction for the year prompt parameter must range between 0 and 1.\n";
	    		correct = false;
	    	}
	    	
	    	if (correct)
	    	{
		    	XMLWriter.saveProfile(profileName,
		    					   seedQueries,
		    					   minLength,
		    					   maxLength,
		    					   numSessions,
		    					   yearPrompt,
		    					   segregationCheckBox.isSelected());
				JOptionPane.showMessageDialog(this, "The profile has been saved!", "Success message", JOptionPane.INFORMATION_MESSAGE);
	    	}
	    	else
	    	{
	    		JOptionPane.showMessageDialog(this, errors, "Warning message", JOptionPane.WARNING_MESSAGE);
	    	}
    	}
    	catch (Exception e)
    	{
    		JOptionPane.showMessageDialog(this, "The parameters you entered are not valid. Please check them and try again.", "Warning message", JOptionPane.WARNING_MESSAGE);
    	}
    }
    
    /**
     * When the "Load profiles" button is clicked, the user must select the XML files
     * corresponding to the profiles to be simulated by the generator. The list of profiles
     * selected is then shown in the text area below.
     * @param evt
     */
    private void loadProfilesButtonActionPerformed(java.awt.event.ActionEvent evt)
    {                                                   
    	JFileChooser fc = new JFileChooser();
    	File f = new File("./");
    	
    	FileNameExtensionFilter filter = new FileNameExtensionFilter("XML file", "xml");
        fc.addChoosableFileFilter(filter);
    	fc.setCurrentDirectory(f);
    	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	fc.setMultiSelectionEnabled(true);
    	fc.showOpenDialog(null);
    	
    	File[] fileList = fc.getSelectedFiles();
    	
    	profiles = new ArrayList<Profile>();
    	
    	for (int i = 0; i < fileList.length; ++i)
    	{
    		try
    		{
				profiles.add(new XMLReader().getProfile(fileList[i].getCanonicalPath()));
			}
     		catch (Exception e)
    		{
     			JOptionPane.showMessageDialog(this, "Could not read profile in file " + fileList[i].getName(), "Error message", JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
			}
    	}
    	
    	profilesTextArea.setText("");
    	
    	for (Profile p : profiles)
    	{
    		String profileName = p.getName();
    		profilesTextArea.append(profileName + "\n");
    	}
    }    
    
    /**
     * When the "Generate Workload" button is clicked, syntactic checks are performed
     * to avoid the generation of an incorrect workload. Checks include correct paths,
     * a positive number of profiles and correct global parameters.
     * @param evt
     */
    private void generateWorkloadButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	
    	boolean correct = true;
    	int maxMeasures, minReportSize, maxReportSize, surprisingQueries, cubeNumber;
    	String cubeName = "";
    	String errors = "";
    	
    	try
    	{
    		maxMeasures = Integer.valueOf(maxMeasuresTextField.getText());
    	   	minReportSize = Integer.valueOf(minReportSizeTextField.getText());
	    	maxReportSize = Integer.valueOf(maxReportSizeTextField.getText());
	    	surprisingQueries = Integer.valueOf(surprisingQueriesTextField.getText());
	    	cubeNumber = Integer.valueOf(selectCubeTextField.getText()) - 1;

	    	if (csvPath == null || csvPath.isEmpty())
	    	{
	    		errors += "The CSV path has not been selected.\n";
	    		correct = false;
	    	}
	    	
	    	if (schemaPath == null || schemaPath.isEmpty())
	    	{
	    		errors += "The Schema path has not been selected.\n";
	    		correct = false;
	    	}
	    	
	    	if (cubeNumber < 0 || cubeNumber >= maxCube)
	    	{
	    		correct = false;
	    		errors += "The cube number is not correct.\n";
	    	}
	    	else
	    	{
		    	cubeName = cubeNames.get(cubeNumber);
	    	}
	    	
	    	if (profiles.size() == 0)
	    	{
	    		errors += "No profiles have been loaded.\n";
	    		correct = false;
	    	}
	    	
	    	if (maxMeasures <= 0)
	    	{
	    		correct = false;
	    		errors += "The number of measures must be higher than 0.\n";
	    		maxMeasuresTextField.setText("");
	    	}
	    	
	    	if (minReportSize <= 0)
	    	{
	    		correct = false;
	    		errors += "The minimum size of session-seed queries reports must be higher than 0.\n";
	    		minReportSizeTextField.setText("");
	    	}
	    	
	    	if (maxReportSize <= 0 || maxReportSize < minReportSize)
	    	{
	    		correct = false;
	    		errors += "The maximum size of session-seed queries reports must be higher (or equal) than the set minimum.\n";
	    		maxReportSizeTextField.setText("");
	    	}
	    	
	    	if (surprisingQueries <= 0)
	    	{
	    		correct = false;
	    		errors += "The number of surprising queries must be higher than 0.";
	    		surprisingQueriesTextField.setText("");
	    	}
	    	
	    	if (correct)
	    	{
	    		OlapGenerator og = new OlapGenerator(profiles.size(),
	    											 maxMeasures,
	    											 minReportSize,
	    											 maxReportSize,
	    											 surprisingQueries,
	    											 cubeName,
	    											 schemaPath,
	    											 csvPath,
	    											 profiles);
	    		
	    		try
	    		{
					og.generateWorkload();
					og.saveWorkload("Workload_" + cubeName + ".xml");
					JOptionPane.showMessageDialog(this, "Workload created!", "Success message", JOptionPane.INFORMATION_MESSAGE);
				}
	    		catch (Exception e)
	    		{
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Sorry, an error halted the creation of the workload.", "Error message", JOptionPane.ERROR_MESSAGE);
				}
	    	}
	    	else {
	    		JOptionPane.showMessageDialog(this, errors, "Warning message", JOptionPane.WARNING_MESSAGE);
	    	}
    	}
    	catch (Exception e)
		{
    		correct = false;
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this, "The parameters you entered are not valid. Please check them and try again.", "Warning message", JOptionPane.WARNING_MESSAGE);
		}

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    
    private List<Profile> profiles = new ArrayList<Profile>();
    private String csvPath;
    private String schemaPath;
    private List<String> cubeNames = new ArrayList<String>();
    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    private int maxCube = 0;
    
    private javax.swing.JButton chooseCSVButton;
    private javax.swing.JTextField chooseCSVTextField;
    private javax.swing.JButton chooseSchemaButton;
    private javax.swing.JTextField chooseSchemaTextField;
    private javax.swing.JScrollPane cubesScrollPane;
    private javax.swing.JTextArea cubesTextArea;
    private javax.swing.JButton generateWorkloadButton;
    private javax.swing.JPanel globalParametersPanel;
    private javax.swing.JLabel seedQueriesLabel;
    private javax.swing.JTextField seedQueriesTextField;
    private javax.swing.JPanel inputFilesPanel;
    private javax.swing.JButton loadProfilesButton;
    private javax.swing.JPanel profileParametersPanel;
    private javax.swing.JLabel maxReportSizeLabel;
    private javax.swing.JTextField maxReportSizeTextField;
    private javax.swing.JLabel maxLengthLabel;
    private javax.swing.JTextField maxLengthTextField;
    private javax.swing.JLabel maxMeasuresLabel;
    private javax.swing.JTextField maxMeasuresTextField;
    private javax.swing.JLabel minReportSizeLabel;
    private javax.swing.JTextField minReportSizeTextField;
    private javax.swing.JLabel minLengthLabel;
    private javax.swing.JTextField minLengthTextField;
    private javax.swing.JLabel numSessionLabel;
    private javax.swing.JTextField numSessionTextField;
    private javax.swing.JLabel profileNameLabel;
    private javax.swing.JTextField profileNameTextField;
    private javax.swing.JScrollPane profilesScrollPane;
    private javax.swing.JTextArea profilesTextArea;
    private javax.swing.JButton saveProfileButton;
    private javax.swing.JCheckBox segregationCheckBox;
    private javax.swing.JLabel segregationLabel;
    private javax.swing.JLabel selectCubeLabel;
    private javax.swing.JTextField selectCubeTextField;
    private javax.swing.JLabel surprisingQueriesLabel;
    private javax.swing.JTextField surprisingQueriesTextField;
    private javax.swing.JLabel yearPromptLabel;
    private javax.swing.JTextField yearPromptTextField;

}