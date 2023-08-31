package objects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import actors.ClientActor;
import messages.ClientMsg;

public class GUI {

	private ClientActor client;
	private JFrame frame;
	private JPanel mainPanel, textPanel, buttonPanel;
	private ArrayList<JLabel> labelList;
	private JTextField targetTemp_text, roomId_text;
	private JLabel labTemp, labRoom, totalPowerUsage, applianceLabel;
	private JButton switchStatus, sendTargetTemp;

	public GUI(ArrayList<Room> roomList, String appliances, int usage, ClientActor cl) {
		client = cl;
		// Instantiating GUI Objects
		frame = new JFrame();
		targetTemp_text = new JTextField();
		roomId_text = new JTextField();
		sendTargetTemp = new JButton("Set Target Temperature");
		switchStatus = new JButton("Set ON/OFF");
		labTemp = new JLabel("Target Temperature: ");
		labRoom = new JLabel("Room Number: ");
		applianceLabel = new JLabel(appliances);
		totalPowerUsage = new JLabel("Total Power Usage: "+ usage + " W");
		labelList = new ArrayList<JLabel>();
		mainPanel = new JPanel();
		textPanel = new JPanel();
		buttonPanel = new JPanel();

		// set fonts
		Font fp = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
		Font fb = new Font(Font.SANS_SERIF, Font.BOLD, 15);
		targetTemp_text.setFont(fp);
		roomId_text.setFont(fp);
		labRoom.setFont(fp);
		labTemp.setFont(fp);
		applianceLabel.setFont(fp);
		totalPowerUsage.setFont(fb);
		switchStatus.setFont(fb);
		sendTargetTemp.setFont(fb);

		// set layouts and sizes
		frame.setSize(1000, 500);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		roomId_text.setMaximumSize(new Dimension(new Integer(500), new Integer(150)));
		roomId_text.setBorder(new LineBorder(Color.BLACK, 1));
		targetTemp_text.setMaximumSize(new Dimension(new Integer(500), new Integer(150)));
		targetTemp_text.setBorder(new LineBorder(Color.BLACK, 1));
		labTemp.setBorder(new EmptyBorder(0, 5, 0, 0));
		labRoom.setBorder(new EmptyBorder(0, 5, 0, 0));
		applianceLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		totalPowerUsage.setBorder(new EmptyBorder(0, 5, 0, 0));

		// add one panel for each room
		for (Room c : roomList) {
			JPanel pan = new JPanel();
			JLabel lab = new JLabel(c.toString());
			lab.setFont(fp);
			pan.add(lab);
			labelList.add(lab);
			mainPanel.add(pan);
		}
		mainPanel.add(applianceLabel);
		
		// add texts area
		textPanel.add(labRoom);
		textPanel.add(roomId_text);
		textPanel.add(labTemp);
		textPanel.add(targetTemp_text);
		mainPanel.add(textPanel);
		textPanel.setBounds(0, 500, textPanel.getWidth(), textPanel.getHeight());

		// add button listeners
		switchStatus.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (roomId_text.getText().equals("") || roomId_text.getText() == null)
					JOptionPane.showMessageDialog(frame, "Invalid Room ID");
				else {
					int x = Integer.parseInt(roomId_text.getText());

					if (x < 0 || x > client.getRoomList().size())
						JOptionPane.showMessageDialog(frame, "Invalid Room ID");
					else {
						if (client.getRoomList().get(x).getHeaterStatus() != Status.MANUALLY_OFF)
							client.getServer().tell(
									new ClientMsg(x, Status.MANUALLY_OFF, MessageType.MANUALLY_SET_HEATER),
									client.self());
						else
							client.getServer().tell(new ClientMsg(x, Status.AUTO, MessageType.MANUALLY_SET_HEATER),
									client.self());
					}
				}
			}
		});

		sendTargetTemp.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (roomId_text.getText().equals("") || roomId_text.getText() == null)
					JOptionPane.showMessageDialog(frame, "Invalid Room ID");
				else {
					if (targetTemp_text.getText().equals("") || targetTemp_text.getText() == null)
						JOptionPane.showMessageDialog(frame, "Invalid Temperature");
					else {
						int x = Integer.parseInt(roomId_text.getText());
						double y = Double.parseDouble(targetTemp_text.getText());

						if (x < 0 || x > client.getRoomList().size())
							JOptionPane.showMessageDialog(frame, "Invalid Room ID");
						else
							client.getServer().tell(new ClientMsg(x, y, MessageType.SET_TARGET_TEMPERATURE),
									client.self());
					}
				}
			}
		});

		// add buttons
		buttonPanel.add(switchStatus);
		buttonPanel.add(sendTargetTemp);
		mainPanel.add(buttonPanel);
		
		mainPanel.add(totalPowerUsage);

		// add the main panel to the frame
		frame.add(mainPanel);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void update(Room r, int usage) {
		labelList.get(r.getId()).setText(r.toString());
		totalPowerUsage.setText("Total Power Usage: "+ usage + " W");
	}
	
	public void update(String app, int usage) {
		applianceLabel.setText(app);
		totalPowerUsage.setText("Total Power Usage: "+ usage + " W");
	}
}
