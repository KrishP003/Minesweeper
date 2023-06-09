import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Minesweeper extends JFrame implements ActionListener, MouseListener
{
	JToggleButton[][] board;
	JFrame frame;
	JPanel boardPanel;
	boolean firstClick = true, gameOn = true;
	int numMines, timePassed;
	ImageIcon mineIcon, flag, lose, smile, wait, win;
	ImageIcon[] numbers;
	GraphicsEnvironment ge;
	Font mineFont, timerFont;
	JMenu difficultyMenu;
	JMenuBar menuBar;
	JMenuItem easy, medium, hard;
	JButton reset;
	Timer timer;
	JTextField timeField;

	public Minesweeper()
	{
		numMines = 10;
		try
		{
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			mineFont = Font.createFont(Font.TRUETYPE_FONT, new File("mine-sweeper.ttf"));
			ge.registerFont(mineFont);
		}catch(IOException | FontFormatException e){}
		System.out.println(mineFont);

		mineIcon = new ImageIcon("mine.png");
		mineIcon = new ImageIcon(mineIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));

		numbers = new ImageIcon[8];
		for(int i = 0; i < numbers.length; i++)
		{
			numbers[i] = new ImageIcon((i+1)+".png");
			numbers[i] = new ImageIcon(numbers[i].getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		}

		flag = new ImageIcon("flag.png");
		flag = new ImageIcon(flag.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

		lose = new ImageIcon("lose1.png");
		lose = new ImageIcon(lose.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

		smile = new ImageIcon("smile1.png");
		smile = new ImageIcon(smile.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

		wait = new ImageIcon("wait1.png");
		wait = new ImageIcon(wait.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

		win = new ImageIcon("win1.png");
		win = new ImageIcon(win.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        frame = new JFrame("Minesweeper");
		difficultyMenu = new JMenu("Difficulty");
		timeField = new JTextField(5);
		menuBar = new JMenuBar();

		try
		{
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			timerFont = Font.createFont(Font.TRUETYPE_FONT, new File("digital-7.ttf"));
			ge.registerFont(timerFont);
		}catch(IOException | FontFormatException e){}
		System.out.println(timerFont);
		timeField.setFont(timerFont.deriveFont(20f));
		timeField.setForeground(Color.BLUE);
		timeField.setBackground(Color.YELLOW);
		timeField.addActionListener(this);

		reset = new JButton(smile);
		reset.addActionListener(this);

		easy = new JMenuItem("Easy");
		easy.addActionListener(this);

		medium = new JMenuItem("Medium");
		medium.addActionListener(this);

		hard = new JMenuItem("Hard");
		hard.addActionListener(this);

		difficultyMenu.add(easy);
		difficultyMenu.add(medium);
		difficultyMenu.add(hard);
		menuBar.setLayout(new GridLayout(1, 3));
		menuBar.add(difficultyMenu);
		menuBar.add(reset);
		menuBar.add(timeField);

		createBoard(10,10);
		this.add(menuBar, BorderLayout.NORTH);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void createBoard(int row, int col)
	{
		if(boardPanel != null)
			this.remove(boardPanel);
		boardPanel = new JPanel();
		board = new JToggleButton[row][col];
		boardPanel.setLayout(new GridLayout(row,col));
		for(int r = 0; r < board.length; r++)
		{
			for(int c = 0; c < board[0].length; c++)
			{
				board[r][c] = new JToggleButton();
				board[r][c].putClientProperty("row",r);
				board[r][c].putClientProperty("column",c);
				board[r][c].putClientProperty("state",0);
				board[r][c].setBorder(BorderFactory.createBevelBorder(0));
				//board[r][c].setFont(mineFont.deriveFont(16f));
				board[r][c].setFocusPainted(false);
				board[r][c].addMouseListener(this);
				boardPanel.add(board[r][c]);
			}
		}
		this.add(boardPanel);
		this.setSize(col*40, row*40);
		this.revalidate();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == easy)
		{
			numMines = 10;
			createBoard(9,9);
		}

		if(e.getSource() == medium)
		{
			numMines = 40;
			createBoard(16,16);
		}

		if(e.getSource() == hard)
		{
			numMines = 99;
			createBoard(16,30);
		}

		if(e.getSource() == reset)
		{
			reset.setIcon(smile);
			gameOn = true;
			firstClick = true;
			timePassed = 0;
			timeField.setText(" "+timePassed);
			createBoard(board.length,board[0].length);
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		if(gameOn)
		{
			int row = (int)((JToggleButton)e.getComponent()).getClientProperty("row");
			int col = (int)((JToggleButton)e.getComponent()).getClientProperty("column");
			reset.setIcon(smile);

			if(e.getButton() == MouseEvent.BUTTON1 && board[row][col].isEnabled() )
			{
				if(firstClick)
				{
					setMinesAndCount(row, col);
					timer = new Timer();
					timer.schedule(new UpdateTimer(),0,1000);
					firstClick = false;
				}
				int state = (int)((JToggleButton)board[row][col]).getClientProperty("state");
				if(state == -1)
				{
					reset.setIcon(lose);
					gameOn = false;
					board[row][col].setIcon(mineIcon);
					board[row][col].setContentAreaFilled(false);
					board[row][col].setOpaque(true);
					board[row][col].setBackground(Color.RED);
					revealMines();
					timer.cancel();
					//JOptionPane.showMessageDialog(null, "You are a loser!");
				}
				else
				{
					expand(row,col);
					checkWin();
				}
			}

			if(e.getButton() == MouseEvent.BUTTON3)
			{
				if(!firstClick)
				{
					if(!board[row][col].isSelected())
					{
						if(board[row][col].getIcon() == null)
						{
							board[row][col].setIcon(flag);
							board[row][col].setDisabledIcon(flag);
							board[row][col].setEnabled(false);
						}
						else if(board[row][col].getIcon() == flag)
						{
							board[row][col].setIcon(null);
							board[row][col].setEnabled(true);
						}
						else
						{
							board[row][col].setIcon(flag);
							board[row][col].setEnabled(false);
						}
					}
				}
			}
		}
	}

	public void revealMines()
	{
		for(int r = 0; r < board.length; r++)
		{
			for(int c = 0; c < board[0].length; c++)
			{
				int state = (int)((JToggleButton)board[r][c]).getClientProperty("state");
				if(state == -1)
				{
					board[r][c].setIcon(mineIcon);
					board[r][c].setDisabledIcon(mineIcon);
				}
				board[r][c].setEnabled(false);
			}
		}
	}

	public void setMinesAndCount(int currRow, int currCol)
	{
		int count = numMines;
		int dimR = board.length;
		int dimC = board[0].length;
		while(count > 0)
		{
			int randR = (int)(Math.random()*dimR);
			int randC = (int)(Math.random()*dimC);
			int state = (int)((JToggleButton)board[randR][randC]).getClientProperty("state");

			if(state != -1 && (Math.abs(randR - currRow) > 1 || Math.abs(randC - currCol) > 1))
			{
				board[randR][randC].putClientProperty("state",-1);
				count--;
			}
		}


		for(int r = 0; r < dimR; r++)
		{
			for(int c = 0; c < dimC; c++)
			{
				count = 0;
				int state = (int)((JToggleButton)board[r][c]).getClientProperty("state");
				//System.out.println(r+","+c+"\t"+state);
				if(state != -1)
				{
					for(int r3x3 = r - 1; r3x3 <= r + 1; r3x3++)
					{
						for(int c3x3 = c - 1; c3x3 <= c + 1; c3x3++)
						{
							try
							{
								state = (int)((JToggleButton)board[r3x3][c3x3]).getClientProperty("state");
								if(state == -1)
									count++;
							}catch(ArrayIndexOutOfBoundsException e){}
						}
					}
					//System.out.println(r+","+c+"\t"+count);
					board[r][c].putClientProperty("state",count);
				}
			}
		}

		/*for(int r = 0; r < dimR; r++)
		{
			for(int c = 0; c < dimC; c++)
			{
				int state = (int)((JToggleButton)board[r][c]).getClientProperty("state");
				board[r][c].setText(""+state);
			}
		}*/
	}

	public void expand(int row, int col)
	{
		if(!board[row][col].isSelected())
			board[row][col].setSelected(true);

		int state = (int)((JToggleButton)board[row][col]).getClientProperty("state");
		if(state > 0)
			write(row, col, state);
		else
		{
			for(int r3x3 = row - 1; r3x3 <= row + 1; r3x3++)
			{
				for(int c3x3 = col - 1; c3x3 <= col + 1; c3x3++)
				{
					try
					{
						if(!board[r3x3][c3x3].isSelected())
							expand(r3x3,c3x3);
					}catch(ArrayIndexOutOfBoundsException e){}

				}
			}
		}
	}

	public void checkWin()
	{
		int dimR = board.length;
		int dimC = board[0].length;
		int totalSpaces = dimR * dimC;
		int count = 0;
		for(int r = 0; r < dimR; r++)
		{
			for(int c = 0; c < dimC; c++)
			{
				int state = (int)((JToggleButton)board[r][c]).getClientProperty("state");
				if(board[r][c].isSelected() && state != -1)
					count++;
			}
		}
		System.out.println(count);
		if(numMines == totalSpaces - count)
		{
			timer.cancel();
			gameOn = false;
			reset.setIcon(win);
			for(int r = 0; r < board.length; r++)
				for(int c = 0; c < board[0].length; c++)
					board[r][c].setEnabled(false);
			//JOptionPane.showMessageDialog(null, "You Win!");
		}
	}

	public void write(int row, int col, int state)
	{
		switch(state)
		{
			case 1: board[row][col].setForeground(Color.BLUE); break;
			case 2: board[row][col].setForeground(Color.GREEN); break;
			case 3: board[row][col].setForeground(Color.RED); break;
			case 4: board[row][col].setForeground(new Color(128,0,128)); break;
			case 5: board[row][col].setForeground(new Color(128,0,0)); break;
			case 6: board[row][col].setForeground(Color.CYAN); break;
			case 7: board[row][col].setForeground(Color.BLACK); break;
			case 8: board[row][col].setForeground(Color.GRAY); break;
			default: board[row][col].setForeground(Color.ORANGE); break;
		}

		if(state > 0)
		{
			board[row][col].setIcon(numbers[state-1]);
			board[row][col].setDisabledIcon(numbers[state-1]);
		}
	}

	public void mousePressed(MouseEvent e)
	{
			for(int r = 0; r < board.length; r++)
			{
				for(int c = 0; c < board[0].length; c++)
				{
					if(board[r][c] == e.getSource())
					{
						reset.setIcon(wait);
						break;
					}
				}
			}
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}


	public static void main(String[] args)
	{
		Minesweeper app = new Minesweeper();
	}

	public class UpdateTimer extends TimerTask
	{
		public void run()
		{
			if(gameOn)
			{
				timePassed++;
				timeField.setText(" "+timePassed);
			}
		}
	}
}