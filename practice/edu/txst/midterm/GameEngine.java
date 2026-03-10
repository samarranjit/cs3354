package edu.txst.midterm;

/**
 * Core game logic for the Sokoban midterm. This class is responsible for
 * locating the player, handling movement and box pushing rules, and
 * determining when the game is solved or has no solution.
 */
public class GameEngine {
	private Board board;
	private int playerRow;
	private int playerCol;

	// Cell Type Constants
	private static final int FLOOR = 0;
	private static final int WALL = 1;
	private static final int BOX = 2;
	private static final int GOAL = 3;
	private static final int PLAYER = 4;
	private static final int BOX_ON_GOAL = 5;

	/**
	 * Creates a new game engine for the given board and locates the initial
	 * position of the player.
	 * 
	 * @param board the board that represents the current level
	 */
	public GameEngine(Board board) {
		this.board = board;
		findPlayer();
	}

	/**
	 * Determines if the game is over. The game is considered solved when
	 * there are no remaining goal cells on the board. All goals must be
	 * covered by boxes (BOX_ON_GOAL).
	 * 
	 * @return {@code true} if the level is solved, {@code false} otherwise
	 */
	public boolean isGameOver() {
		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 10; c++) {
				int cell = board.getCell(r, c);
				if (cell == GOAL) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determines if the game is stuck. A level is considered stuck when
	 * at least one goal remains and none of the unsolved boxes can be pushed
	 * to a valid destination cell.
	 * 
	 * @return {@code true} if no box can be moved and at least one goal
	 *         remains, {@code false} otherwise
	 */
	public boolean isGameStuck() {
		if (isGameOver()) {
			return false;
		}

		boolean goalRemaining = false;
		boolean foundUnsolvedBox = false;

		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 10; c++) {
				int cell = board.getCell(r, c);
				if (cell == GOAL) {
					goalRemaining = true;
				}
				// Only boxes that are not already on goals need to be moved.
				if (cell == BOX) {
					foundUnsolvedBox = true;
					if (canBoxMove(r, c)) {
						return false;
					}
				}
			}
		}

		return goalRemaining && foundUnsolvedBox;
	}

	/**
	 * Finds and stores the player's current board coordinates.
	 */
	private void findPlayer() {
		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 10; c++) {
				if (board.getCell(r, c) == PLAYER) {
					playerRow = r;
					playerCol = c;
					return;
				}
			}
		}
	}

	/**
	 * Attempts to move the player.
	 * 
	 * @param dRow Change in row (-1, 0, 1)
	 * @param dCol Change in column (-1, 0, 1)
	 * @return {@code true} if the player was moved, {@code false} if the
	 *         movement was blocked
	 */
	public boolean movePlayer(int dRow, int dCol) {
		int targetRow = playerRow + dRow;
		int targetCol = playerCol + dCol;
		int targetCell = board.getCell(targetRow, targetCol);

		// 1. Check for Walls, Goals, Boxes on Goals, or Out of Bounds
		if (targetCell == WALL || targetCell == GOAL || targetCell == BOX_ON_GOAL || targetCell == -1) {
			return false; // Movement blocked
		}

		// 2. Check for normal Boxes
		if (targetCell == BOX) {
			int nextRow = targetRow + dRow;
			int nextCol = targetCol + dCol;
			int nextCell = board.getCell(nextRow, nextCol);

			// Can only push if the space behind the box is Floor or Goal
			if (nextCell == FLOOR || nextCell == GOAL) {
				// Move the box
				int newBoxType = (nextCell == GOAL) ? BOX_ON_GOAL : BOX;
				board.setCell(nextRow, nextCol, newBoxType);

				// Clear the box's old position (it becomes a floor)
				board.setCell(targetRow, targetCol, FLOOR);
			} else {
				return false; // Box is blocked
			}
		}

		// 3. Move the Player (player always stands on floor)
		board.setCell(playerRow, playerCol, FLOOR);
		playerRow = targetRow;
		playerCol = targetCol;
		board.setCell(playerRow, playerCol, PLAYER);
		return true;
	}

	/**
	 * Determines whether a specific unsolved box can be pushed in at least one
	 * direction.
	 * 
	 * @param row the box row index
	 * @param col the box column index
	 * @return {@code true} if the box can be pushed in any direction,
	 *         {@code false} otherwise
	 */
	private boolean canBoxMove(int row, int col) {
		int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		for (int[] dir : directions) {
			if (canPushBoxInDirection(row, col, dir[0], dir[1])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether a box can be pushed in one specific direction based on a
	 * free destination cell and a valid player-side cell.
	 * 
	 * @param boxRow the current row of the box
	 * @param boxCol the current column of the box
	 * @param dRow the row delta representing push direction
	 * @param dCol the column delta representing push direction
	 * @return {@code true} if the push is valid, {@code false} otherwise
	 */
	private boolean canPushBoxInDirection(int boxRow, int boxCol, int dRow, int dCol) {
		int destinationRow = boxRow + dRow;
		int destinationCol = boxCol + dCol;
		int playerSideRow = boxRow - dRow;
		int playerSideCol = boxCol - dCol;

		if (!isInBounds(destinationRow, destinationCol) || !isInBounds(playerSideRow, playerSideCol)) {
			return false;
		}

		int destinationCell = board.getCell(destinationRow, destinationCol);
		int playerSideCell = board.getCell(playerSideRow, playerSideCol);

		boolean destinationOpen = (destinationCell == FLOOR || destinationCell == GOAL);
		boolean playerSideOpen = (playerSideCell == FLOOR || playerSideCell == PLAYER);

		return destinationOpen && playerSideOpen;
	}

	/**
	 * Checks whether the provided board coordinates are within bounds.
	 * 
	 * @param row the row index
	 * @param col the column index
	 * @return {@code true} if the coordinates are valid, {@code false} otherwise
	 */
	private boolean isInBounds(int row, int col) {
		return row >= 0 && row < 5 && col >= 0 && col < 10;
	}
}
