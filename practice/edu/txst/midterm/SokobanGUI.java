package edu.txst.midterm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Simple Swing based user interface for the 16-bit Sokoban game. This class
 * is responsible for loading levels, handling keyboard input, and displaying
 * the current board.
 */
public class SokobanGUI extends JFrame {
	private Board originalBoard;
	private Board currentBoard;
	private GameEngine engine;
	private GamePanel gamePanel;
	private JMenuItem resetItem;
	private int stepCounter;

	/**
	 * Creates the Sokoban graphical user interface and initializes menu and
	 * keyboard handling.
	 */
	public SokobanGUI() {
		setTitle("16-Bit Sokoban");
		setSize(640, 480); // Adjusted for 10x5 grid with scaling
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initMenu();

		gamePanel = new GamePanel();
		add(gamePanel);

		// Handle Keyboard Input
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (engine == null)
					return;

				boolean moved = false;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP -> moved = engine.movePlayer(-1, 0);
					case KeyEvent.VK_DOWN -> moved = engine.movePlayer(1, 0);
					case KeyEvent.VK_LEFT -> moved = engine.movePlayer(0, -1);
					case KeyEvent.VK_RIGHT -> moved = engine.movePlayer(0, 1);
				}
				if (moved) {
					stepCounter++;
				}
				gamePanel.repaint();

				// Check for victory
				if (engine.isGameOver()) {
					JOptionPane.showMessageDialog(SokobanGUI.this,
							"Congratulations! You solved the puzzle in " + stepCounter + " steps!",
							"Level Complete", JOptionPane.INFORMATION_MESSAGE);

					// Optional: Disable engine to prevent movement after win
					engine = null;
					resetItem.setEnabled(false);
				} else if (engine.isGameStuck()) {
					JOptionPane.showMessageDialog(SokobanGUI.this,
							"Level has no solution because boxes cannot be moved anymore.",
							"Level cannot be solved.", JOptionPane.INFORMATION_MESSAGE);

					// Optional: Disable engine to prevent movement after win
					engine = null;
				}
			}
		});
	}

	/**
	 * Initializes the menu bar and game menu actions.
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");

		JMenuItem openItem = new JMenuItem("Open");
		resetItem = new JMenuItem("Reset");
		resetItem.setEnabled(false); // Disabled by default

		openItem.addActionListener(e -> openFile());
		resetItem.addActionListener(e -> resetGame());

		gameMenu.add(openItem);
		gameMenu.add(resetItem);
		menuBar.add(gameMenu);
		setJMenuBar(menuBar);
	}

	/**
	 * Opens a CSV file selected by the user and loads it as a new level.
	 */
	private void openFile() {
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			CSVBoardLoader loader = new CSVBoardLoader();

			// Load and Store
			originalBoard = loader.load(selectedFile.getAbsolutePath());
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);
			stepCounter = 0;

			resetItem.setEnabled(true);
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	/**
	 * Resets the current level to its original state.
	 */
	private void resetGame() {
		if (originalBoard != null) {
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);
			stepCounter = 0;
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	// Inner class for custom rendering
	/**
	 * Inner panel responsible for drawing the current state of the board.
	 */
	private class GamePanel extends JPanel {
		private Board board;
		private final int TILE_SIZE = 64; // Scale up for visibility

		/**
		 * Sets the board that this panel should render.
		 * 
		 * @param board the board to render
		 */
		public void setBoard(Board board) {
			this.board = board;
		}

		/**
		 * Paints the current board state by drawing every tile in the 5x10 grid.
		 * 
		 * @param g graphics context used for rendering
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (board == null)
				return;

			for (int r = 0; r < 5; r++) {
				for (int c = 0; c < 10; c++) {
					int cell = board.getCell(r, c);
					drawTile(g, cell, c * TILE_SIZE, r * TILE_SIZE);
				}
			}
		}

		/**
		 * Draws a single tile at the provided screen coordinates.
		 * 
		 * @param g graphics context used for rendering
		 * @param type cell type identifier from the board
		 * @param x tile x coordinate in pixels
		 * @param y tile y coordinate in pixels
		 */
		private void drawTile(Graphics g, int type, int x, int y) {
			// Placeholder colors until you link the sprite loading logic
			switch (type) {
				case 0 -> g.setColor(Color.LIGHT_GRAY); // Floor
				case 1 -> g.setColor(Color.DARK_GRAY); // Wall
				case 2 -> g.setColor(Color.ORANGE); // Box
				case 3 -> g.setColor(Color.magenta); // Goal
				case 4 -> g.setColor(Color.BLUE); // Player
				case 5 -> g.setColor(Color.RED); // Box on Goal
				default -> g.setColor(Color.BLACK);
			}
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, TILE_SIZE, TILE_SIZE); // Grid lines
		}
	}

	/**
	 * Application entry point for launching the Sokoban GUI.
	 * 
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SokobanGUI().setVisible(true));
	}
}
