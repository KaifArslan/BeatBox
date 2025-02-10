import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static javax.sound.midi.ShortMessage.*;

public class BeatBox {
// instance variable for sharing and networking
	private JList<String> incomingList;
	private JTextArea userMessage;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private String userName;
	private int nextNum;
	private Vector<String> listVector = new Vector<>();

// Midi and Sound instances 
	private Sequencer sequencer;
	private Sequence sequence;
	private Track track;
	private int[] instrumentsKeys =  {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	private boolean directPlayer = false; 

// Main Gui instances
HashMap<String, boolean[]> seqMap = new HashMap<>();
ArrayList<JCheckBox> checkboxList;
String[] instrumentsNames =  {"Bass Drum", "Closed Hi-Hat",
 "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
 "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
 "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
 "Open Hi Conga"};

JFrame frame = new JFrame("Beat Box");

// ------------Methods and Inner Class-----------------
public static void main(String[] args){
	String name;
	try{
		name = args[0];
	}catch(RuntimeException re) {
		name = JOptionPane.showInputDialog(
			 "Please Enter your name for Networking or else you can use the following alias. ", 
			 ("User" + new Random().nextInt(100)));
	} 
	new BeatBox().startUp(name);
}
	public void startUp(String name){
		userName = name;
		try{
			Socket socket  = new Socket("localhost", 5000);
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			ExecutorService executor  = Executors.newSingleThreadExecutor();
			executor.submit(new RemoteReader());
		}catch (Exception ex){
			System.out.println("Couldn't connect-you'll have to play alone");
		}
		
		setUpMidi();
		buildGui();
	}
	class RemoteReader implements Runnable{
		public void run(){
			Object obj;
		try{
			while((obj = input.readObject()) != null){
			System.out.println("got an object from server");
			System.out.println(obj.getClass());

			String message = (String) obj;
			boolean[] instrumentBooleanKeys = (boolean[])input.readObject();
			seqMap.put(message,instrumentBooleanKeys);

			listVector.add(message);
			incomingList.setListData(listVector);
			}
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		}
	}
	private void setUpMidi(){
		//Setting up the midi
		try{
				sequencer = MidiSystem.getSequencer();
				sequencer.open();
				sequence = new Sequence(Sequence.PPQ, 4);
				track = sequence.createTrack();
				sequencer.setTempoInBPM(120);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void buildGui(){

		// swing code to make gui 
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout borderLayout = new BorderLayout();
		JPanel background  = new JPanel(borderLayout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("Options");
		JMenuItem newFile = new JMenuItem("Clear the Current Pattern");
		newFile.addActionListener(event -> clearCheckBox());

		JMenuItem loadFile = new JMenuItem("Load Beat");
		loadFile.addActionListener(event -> loadFileFunction(frame) );
		
		JMenuItem saveFile = new JMenuItem("Save Beat");
		saveFile.addActionListener(event -> saveFileFunction() );

		file.add(newFile);
		file.add(loadFile);
		file.add(saveFile);

		JMenuItem help = new JMenuItem("Help");
		help.addActionListener(event -> helpBox() );

		file.add(help);
		menuBar.add(file);
		frame.setJMenuBar(menuBar);
		Box buttonBox = new Box(BoxLayout.Y_AXIS);

		JButton start = new JButton("Start");
		start.addActionListener(event -> BuildTrackAndStart());
		buttonBox.add(start);
		
		JButton directStart  = new JButton("Direct Play");
		ActionListener checkboxListener = event -> BuildTrackAndStart();
		directStart.addActionListener(event -> directPlay(directStart, checkboxListener ));
		buttonBox.add(start);
		buttonBox.add(directStart);

		JButton stop = new JButton("Stop");
		stop.addActionListener(event -> sequencer.stop());
		buttonBox.add(stop);

		JButton tempoUp = new JButton("Tempo Up");
		tempoUp.addActionListener(event -> changeTempo(1.03f) );
		buttonBox.add(tempoUp);

		JButton tempoDown = new JButton("Tempo Down");
		tempoDown.addActionListener(event -> changeTempo(0.97f));
		buttonBox.add(tempoDown);

		JButton sendIt = new JButton("Send It");
		sendIt.addActionListener(event -> sendMessageAndTrack() );
		buttonBox.add(sendIt);

		JLabel textLabel = new JLabel("Your Message");
		buttonBox.add(textLabel);

		userMessage = new JTextArea();
		userMessage.setLineWrap(true);
		userMessage.setWrapStyleWord(true);
		JScrollPane messageScroller = new JScrollPane(userMessage);
		buttonBox.add(messageScroller);

		JLabel textLabel2 = new JLabel("Network");
		buttonBox.add(textLabel2);
		incomingList = new JList<>();
		incomingList.addListSelectionListener(new MyListSelectionListener());
		incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane theList = new JScrollPane(incomingList);
		buttonBox.add(theList);
		incomingList.setListData(listVector);
		JPanel nameBox = new JPanel(new GridLayout(0, 1));

		//Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (String instrumentName : instrumentsNames){
			JLabel instrumentLabel = new JLabel(instrumentName);
			instrumentLabel.setBorder(BorderFactory.createEmptyBorder(4,1,4,1));
			nameBox.add(instrumentLabel);
		}

		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);

		frame.getContentPane().add(background);
		GridLayout grid = new GridLayout(16, 16);

		JPanel mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);

		checkboxList = new ArrayList<>();
	
		for (int i =0; i<256; i++){
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			c.setOpaque(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		Color color = new Color(248, 245, 233);
		background.setBackground(color);

		for(Component backgroundComponent :background.getComponents()){
			backgroundComponent.setBackground(color);
		}
	
		frame.setBounds(50,50,300,300);
		frame.pack();
		frame.setVisible(true);

	}
	public class MyListSelectionListener implements ListSelectionListener{

		public void valueChanged(ListSelectionEvent lse){
			
			if(!lse.getValueIsAdjusting()){

		String[] options = {"Load the Beat", "Save the Current Beat","Cancel" };
		int choice = JOptionPane.showOptionDialog(null, 
		"Do you want to save the current pattern before loading this shared one?",
		 "Save before Load", 
		 JOptionPane.YES_NO_CANCEL_OPTION, 
		 JOptionPane.QUESTION_MESSAGE, 
		 null, options, options[2]);

		 switch (choice) {
			case 0:
			String selected = incomingList.getSelectedValue();
			if (selected != null){
				boolean[] selectedState = seqMap.get(selected);
				changeSequence(selectedState);
				sequencer.stop();
				BuildTrackAndStart();
			}
			break;
			case 1:
		  saveFileFunction();
			default:
			break;
		 }
			}
		} 
												

	}
	private void changeSequence(boolean[] checkboxState){
		for(int i = 0; i< 256; i++){
			JCheckBox check = checkboxList.get(i);
			check.setSelected(checkboxState[i]);
		}
	}
	private void BuildTrackAndStart(){

		ArrayList<Integer> trackList;
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		for(int i = 0; i < 16; i++){
			trackList = new ArrayList<>();
			int key = instrumentsKeys[i];
			for (int j = 0; j < 16; j++){
				JCheckBox jc = checkboxList.get(j+ (16*i));
				if(jc.isSelected()){
					trackList.add(key);
				}else{
					trackList.add(null);
				}
			}
		makeTracks(trackList);
		track.add(makeEvent(CONTROL_CHANGE, 1,127,0,16));
		}
		track.add(makeEvent(PROGRAM_CHANGE, 9,1,0,15));
		// call to makeTracks() and makeEvent();
		try{
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.setTempoInBPM(120);
			sequencer.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void makeTracks(ArrayList<Integer> list){
		//call to makeEvent();
		for (int i = 0; i< 16; i++){
			Integer key = list.get(i);
			if(key != null){

				track.add(makeEvent(NOTE_ON, 9, key, 100, i));
				track.add(makeEvent(NOTE_OFF, 9, key, 100, i+1));
			}
		}
	}
	private MidiEvent makeEvent(int command, int channel, int one, int two, int tick){
		MidiEvent event = null;
		try{
			ShortMessage msg = new ShortMessage();
			msg.setMessage(command, channel, one, two);
			event = new MidiEvent(msg, tick);
		}catch(Exception e){
			e.printStackTrace();
		}
		return event;
	}
	private void changeTempo(float tempoMultiplier){
		float tempoFactor = sequencer.getTempoFactor();
		sequencer.setTempoFactor(tempoFactor * tempoMultiplier);
	}
	private void sendMessageAndTrack(){
		//connection stream to output stream (chain stream)
		boolean[] checkboxState = new boolean[256];
		for (int i =0; i<256; i++){
			JCheckBox checkBox =  checkboxList.get(i);
			if(checkBox.isSelected()){
				checkboxState[i] = true;
			}
		}
		
		try{
			output.writeObject(userName + (nextNum++) + ": "+ userMessage.getText() );
			output.writeObject(checkboxState);
		}catch(Exception e){
			System.out.println("Sorry, couldn't send the message to the server");
		}
		userMessage.setText("");
	}
	
	void clearCheckBox(){
		for(JCheckBox checkbox: checkboxList){
			checkbox.setSelected(false);
		}
	}
	void loadFileFunction(JFrame frame){
		FileDialog fileDialog = new FileDialog(frame, "Open", FileDialog.LOAD);
    fileDialog.setVisible(true);
		try{
		
		ObjectInputStream theSelectedFile = new ObjectInputStream( new FileInputStream(fileDialog.getFile()));
		boolean[] checkboxState = (boolean[]) theSelectedFile.readObject();
		changeSequence(checkboxState);

		theSelectedFile.close();
    }catch(IOException | ClassNotFoundException  e){
			e.printStackTrace();
		}
	}
	void saveFileFunction(){
		FileDialog fileDialog = new FileDialog(frame, "Save", FileDialog.SAVE);
    fileDialog.setVisible(true);
		
		try{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileDialog.getFile()));
		boolean[] checkboxState = new boolean[256];
		for (int i =0; i<256; i++){
			JCheckBox checkBox =  checkboxList.get(i);
			if(checkBox.isSelected()){
				checkboxState[i] = true;
			}
		}
		out.writeObject(checkboxState);
		out.close();
    }catch(IOException e){
			e.printStackTrace();
		}
	}
	
	void helpBox(){
		String helpBoxString = "Saving & Load: You can save your file or load an existing one, But be sure to give \".ser\" extention to file before saving, And load .ser files only.\n\t\t Other types of beat file are not supported yet.  \r\n" +
		 "Share: You can share your beat and message by clicking on the button \"Send It \". You can type your message in the given Textarea.\n"+
		 "\t\t You can select the beat from the scrolling area and by clicking on the message.\n "+
		 "\t\t Note: You should be connected to the Music Server for sharing, So first run Music Server then you'll be connected automatically."+
		 "Direct Play: Direct play button let's you build and play the pattern track instantly and simultanously.";
		JOptionPane.showMessageDialog(frame,helpBoxString,"Help",
		  JOptionPane.INFORMATION_MESSAGE); 
	}

	void directPlay(JButton directStart, ActionListener checkboxListener){
		
		directPlayer = !directPlayer;
		if (directPlayer){
			directStart.setText("Direct Play off");
		for (JCheckBox checkBox: checkboxList){
			checkBox.addActionListener(checkboxListener);
		}
		}else{
			for (JCheckBox checkBox: checkboxList){
				checkBox.removeActionListener(checkboxListener); 
			}
			directStart.setText("Direct Play On");
		}
	}
}
