package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

import entities.*;

/**
 * Graphical interface
 * @author Rui Henriques
 */
public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	static JTextField speed;
	static JPanel boardPanel;
	static JButton run, reset, step;
	private int nX, nY;
	static JLabel counterLabel = new JLabel("0",SwingConstants.LEFT);



	public class Cell extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public List<Entity> entities = new ArrayList<Entity>();
		
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for(Entity entity : entities) {
	            g.setColor(entity.color);
	            if(entity instanceof Ball) {
	            	g.fillOval(15, 15, 20, 20);
		            g.setColor(Color.white);
	            	g.drawOval(15, 15, 20, 20);
	            } else {
					MovingEntity mentity = (MovingEntity) entity;
	        		switch(mentity.direction) {
		    			case 0:  g.fillPolygon(new int[]{10, 25, 40}, new int[]{40, 10, 40}, 3); break;
		    			case 90: g.fillPolygon(new int[]{10, 40, 10}, new int[]{10, 25, 40}, 3); break;
		    			case 180:g.fillPolygon(new int[]{10, 40, 25}, new int[]{10, 10, 40}, 3); break;
		    			default: g.fillPolygon(new int[]{10, 40, 40}, new int[]{25, 10, 40}, 3); 
		    		}
	            }
            }
        }
	}

	public GUI() {
		setTitle("Dodge Ball");		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setSize(555, 625);
		add(createButtonPanel());
		
		Field.initialize();
		Field.associateGUI(this);


		boardPanel = new JPanel();
		boardPanel.setSize(new Dimension(500,500));
		boardPanel.setLocation(new Point(20,60));

		nX = Field.nX;
		nY = Field.nY;
		boardPanel.setLayout(new GridLayout(nX,nY));
		for(int i=0; i<nX; i++)
			for(int j=0; j<nY; j++)
				boardPanel.add(new Cell());
		
		displayBoard();
		Field.displayObjects();
		update();
		add(boardPanel);
		add(counterLabel);
	}

	public void displayBoard() {
		for(int i=0; i<nX; i++){
			for(int j=0; j<nY; j++){
				int row=nY-j-1, col=i;
				Block block = Field.getBlock(new Point(i,j));
				JPanel p = ((JPanel)boardPanel.getComponent(row*nX+col));
				p.setBackground(block.color);
				p.setBorder(BorderFactory.createLineBorder(Color.white));
			}
		}
		
	}
	
	public void removeObject(Entity object) {
		int row=nY-object.currentPosition.y-1, col=object.currentPosition.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));			
		p.entities.remove(object);
	}

	public void removeAgents(List<Agent> agents) {
		for(Agent agent: agents){	
			int row=nY-agent.currentPosition.y-1, col=agent.currentPosition.x;
			Cell p = (Cell)boardPanel.getComponent(row*nX+col);
			p.setBorder(BorderFactory.createLineBorder(Color.white));			
			p.entities.remove(agent);
		}
	}
	
	public void displayObject(Entity object) {
		int row=nY-object.currentPosition.y-1, col=object.currentPosition.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));			
		p.entities.add(object);
	}

	public void update() {
		boardPanel.invalidate();
	}

	private Component createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(600,50));
		panel.setLocation(new Point(0,0));
		
		step = new JButton("Step");
		panel.add(step);
		step.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")) Field.step();
				else Field.stop();

			}
		});


		reset = new JButton("Reset");
		panel.add(reset);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Field.reset();
			}
		});
		run = new JButton("Run");
		panel.add(run);
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")){
					int time = -1;
					try {
						time = Integer.valueOf(speed.getText());
					} catch(Exception e){
						JTextPane output = new JTextPane();
						output.setText("Please insert an integer value to set the time per step\nValue inserted = "+speed.getText());
						JOptionPane.showMessageDialog(null, output, "Error", JOptionPane.PLAIN_MESSAGE);
					}
					if(time>0){
						Field.run(time);
	 					run.setText("Stop");						
					}
 				} else {
					Field.stop();
 					run.setText("Run");
 				}
			}
		});
		speed = new JTextField("1");
		speed.setMargin(new Insets(5,5,5,5));
		panel.add(speed);
		
		return panel;
	}
}
