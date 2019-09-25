// Jessie Shen and Richa Gupta, js9fr rg4wd
// March 20 2018


import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class GUILLOfFortune extends JFrame {
	JLabel instructions;
	JButton submit;
	JTextArea enterArea;
	
	JLabel livesRemaining;
	JLabel lettersEntered;
	JLabel wordOutput;
	
	
	boolean[] lettersCorrect = {false, false, false, false, false};
	int lives;
	String password;
	ArrayList<String> letters;
	
	public static void main(String[] args) {
		new GUILLOfFortune();
	}
	
	public GUILLOfFortune()
	{
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		this.setSize(width, height);
		
		password = "super";
		lives = 5;
		letters = new ArrayList<String>();
		
		instructions = new JLabel("<html>Enter a letter to see if it's in the word!</html>" );
		submit = new JButton("ENTER");
		enterArea = new JTextArea();
		livesRemaining = new JLabel("Lives: " + lives);
		lettersEntered = new JLabel("<html>Letters Entered: " + letters + "</html>");
		wordOutput = new JLabel("_ _ _ _ _");
		wordOutput.setFont(new Font("Times", Font.BOLD, 20));
		
		instructions.setSize(23*width/60, height/6);
		submit.setSize(width/3, 50);
		enterArea.setSize(width/3, 50);
		livesRemaining.setSize(width/4, height/12);
		lettersEntered.setSize(width/3, height/3);
		wordOutput.setSize(width/2, height/6);
		
		instructions.setLocation(width/15, 1);
		submit.setLocation(width/2, height/2);
		enterArea.setLocation(width/6, height/2);
		livesRemaining.setLocation(11*width/15, 1);
		lettersEntered.setLocation(17*width/30, height/20);
		wordOutput.setLocation(width/3, height/3);
		
		submit.addActionListener(new passwordButtonListener());
		
		this.add(instructions);
		this.add(submit);
		this.add(enterArea);
		this.add(livesRemaining);
		this.add(lettersEntered);
		this.add(wordOutput);
		
		this.add(new JLabel());
		this.setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private class passwordButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0) {
            // TODO: Implement the action taken by the button.
			
			int sizeOfArray = password.length();
			String currentWord = wordOutput.getText();
			boolean inPassword = false;
			char[] passwordLetters = new char[sizeOfArray];

			for(int k = 0; k < passwordLetters.length; k++) {
				passwordLetters[k] = currentWord.charAt(k);
				System.out.println(passwordLetters[k] + "");
			}
			
			String input = enterArea.getText().charAt(0) + "";
			if(letters.contains(input))
				instructions.setText("Letter already in array");
			else {
				letters.add(input);
				char inputchar = input.charAt(0);
				ArrayList<Integer> appearsAt = new ArrayList<Integer>();

				if(password.indexOf(inputchar) != -1) {
					inPassword = true;
					appearsAt.add(password.indexOf(inputchar));
				}
				 
				if(inPassword == true) {
					for(int o = 0; o < appearsAt.size(); o ++) {
						passwordLetters[appearsAt.get(o)] = input.charAt(0);
					}
					
					String newWord = "";
					for(int m = 0; m < passwordLetters.length; m++) {
						newWord = newWord + passwordLetters[m];
						//System.out.println(newWord);
					
					}
					wordOutput.setText(newWord);
					lettersEntered.setText("<html>Letters Entered: " + letters + "</html>");

				}
				else {

					lives--;
					if(lives == 0) {
						instructions.setText("You have no more lives! You lose.");
					}
					livesRemaining.setText("Lives: " + lives);
					lettersEntered.setText("<html>Letters Entered: " + letters + "</html>");
				}
				
				if(wordOutput.getText().equals(password)) {
					instructions.setText("You win!");
				}
			}
		}
	}
}