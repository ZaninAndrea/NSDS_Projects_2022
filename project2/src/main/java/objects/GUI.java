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
	private ArrayList<JLabel> labList;
	private JTextField targetTemp, roomId;
	private JLabel labTemp, labRoom;
	private JButton onOff, targTemp;

	public GUI(ArrayList<Room> roomList, ClientActor cl) {
		client = cl;
		// Instantiating GUI Objects
		frame = new JFrame();
		targetTemp = new JTextField();
		roomId = new JTextField();
		targTemp = new JButton("Set Target Temperature");
		onOff = new JButton("Set ON/OFF");
		labTemp = new JLabel("Target Temperature: ");
		labRoom = new JLabel("Room Number: ");
		labList = new ArrayList<JLabel>();
		mainPanel = new JPanel();
		textPanel = new JPanel();
		buttonPanel = new JPanel();

		// set fonts
		Font fp = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
		Font fb = new Font(Font.SANS_SERIF, Font.BOLD, 15);
		targetTemp.setFont(fp);
		roomId.setFont(fp);
		labRoom.setFont(fp);
		labTemp.setFont(fp);
		onOff.setFont(fb);
		targTemp.setFont(fb);

		// set layouts and sizes
		frame.setSize(800, 400);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		roomId.setMaximumSize(new Dimension(new Integer(500), new Integer(150)));
		roomId.setBorder(new LineBorder(Color.BLACK, 1));
		targetTemp.setMaximumSize(new Dimension(new Integer(500), new Integer(150)));
		targetTemp.setBorder(new LineBorder(Color.BLACK, 1));
		labTemp.setBorder(new EmptyBorder(0, 5, 0, 0));
		labRoom.setBorder(new EmptyBorder(0, 5, 0, 0));

		// add one panel for each room
		for (Room c : roomList) {
			JPanel pan = new JPanel();
			JLabel lab = new JLabel(c.toString());
			lab.setFont(fp);
			pan.add(lab);
			labList.add(lab);
			mainPanel.add(pan);
		}

		// add texts area
		textPanel.add(labRoom);
		textPanel.add(roomId);
		textPanel.add(labTemp);
		textPanel.add(targetTemp);
		mainPanel.add(textPanel);
		textPanel.setBounds(0, 500, textPanel.getWidth(), textPanel.getHeight());

		// add button listeners
		onOff.addMouseListener(new MouseListener() {

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
				if (roomId.getText().equals("") || roomId.getText() == null)
					JOptionPane.showMessageDialog(frame, "Invalid Room ID");
				else {
					int x = Integer.parseInt(roomId.getText());

					if (x < 0 || x > client.getRoomList().size())
						JOptionPane.showMessageDialog(frame, "Invalid Room ID");
					else {
						if (client.getRoomList().get(x).getHeaterStatus() != Status.MANUALLY_ON)
							client.getServer().tell(
									new ClientMsg(x, Status.MANUALLY_ON, MessageType.MANUALLY_SET_HEATER),
									client.self());
						else
							client.getServer().tell(new ClientMsg(x, Status.OFF, MessageType.MANUALLY_SET_HEATER),
									client.self());
					}
				}
			}
		});

		targTemp.addMouseListener(new MouseListener() {

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
				if (roomId.getText().equals("") || roomId.getText() == null)
					JOptionPane.showMessageDialog(frame, "Invalid Room ID");
				else {
					if (targetTemp.getText().equals("") || targetTemp.getText() == null)
						JOptionPane.showMessageDialog(frame, "Invalid Temperature");
					else {
						int x = Integer.parseInt(roomId.getText());
						int y = Integer.parseInt(targetTemp.getText());

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
		buttonPanel.add(onOff);
		buttonPanel.add(targTemp);
		mainPanel.add(buttonPanel);

		// add the main panel to the frame
		frame.add(mainPanel);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	//
	public void update(Room r) {
		labList.get(r.getId()).setText(r.toString());
	}
}
