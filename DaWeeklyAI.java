import java.util.*;

/******************************************************************************
*
* Name: John McNelly and Jeff Barratt
* Block: F
* Date: 1/2/13
*
* Program: APCS Khet AI Challenge - Da Weekly AI
* Description:
*   Simple MiniMax program. Structure written by Jeff.
*   Heuristic written by John. Brainstormed together.
*   This is an Artificial Intelligence Engine written to play the board game
*   Khet 2.0 emulated in a Java environment.  This class makes decisions
*   about how to move Khet pieces in the best possible manner, then sends
*   these moves in real time to another class that runs a Khet game between 
*   two competing AI's.
******************************************************************************/

public class DaWeeklyAI extends Player
{

	final int MAX_DEPTH = 3;
	public final int INFINITY = 100000000;
	final int WIDTH = 10;
	final int HEIGHT = 8;
	private Set<int[]> possibleMoves;

	public DaWeeklyAI(int teamIn)
	{
		super("DaWeeklyAI", teamIn);
		//Create a list of all of the possible moves
		possibleMoves = new HashSet<int[]>();
		possibleMoves.add(new int[]{0, -1, -1, -90});
		possibleMoves.add(new int[]{0, -1, -1, 90});
		for (int i = 0; i < 360; i += 45)
		{
			possibleMoves.add(new int[]{1, -1, -1, i});
		}
	}

	/**
	 * What gets called by the game engine.
	 */
	public int[] makeMove(Piece[][] boardIn)
	{
		Piece[][] board = copy(boardIn);
		int[][] ourMoves = new int[140][4];
		int indexOfMove = 0;
		int index = 0;
		int average = -INFINITY;
		int max = -INFINITY;
		for (int x = 0; x < HEIGHT; x ++)
		{
			for (int y = 0; y < WIDTH; y ++)
			{
				if (boardIn[x][y] != null && boardIn[x][y].getTeam() == super.getTeam())
				{
					//For all of the possible moves...
					for(int[] a : possibleMoves)
					{
						//To make sure we don't actually change the super array.
						int[] move = a.clone();
						move[1] = x;
						move[2] = y;
						ourMoves[indexOfMove] = move;
						board = copy(boardIn);
						if (KhetGameEngine.executeMove(move, super.getTeam(), board))
						{
							shootLaser(board, super.getTeam());
							//If we won... Just fucking return the move. Jesus.... It's not that hard.
							if (checkWin(board))
							{
								return move;
							}
							if (!(board[move[1]][move[2]] instanceof Pharaoh) || !(move[0] == 0))
							{
								//Here's the recursion!
								int[] info = recDetermineMove(board, 1);
								if (info[0] > max)
								{
									max = info[0];
									average = info[1];
									index = indexOfMove;
								}
								else if (info[0] == max)
								{
									if (info[1] > average)
									{
										average = info[1];
									}
								}
							}
						}
						indexOfMove ++;
					}
				}
			}
		}
		return ourMoves[index];
	}

	private int[] recDetermineMove(Piece[][] boardIn, int depth)
	{
		int evaluate = evaluate(boardIn);
		if (depth < MAX_DEPTH)
		{
			int[] value = {0,0};
			if (depth % 2 == 0)
			{
				//If we just lost (They won on their turn), return negative infinity.
				if (evaluate == -INFINITY)
				{
					return new int[] {-INFINITY, -INFINITY};
				}
				//Get the best case of all of the nodes
				int max = -INFINITY;
				int average = -INFINITY;
				for (int x = 0; x < HEIGHT; x ++)
				{
					for (int y = 0; y < WIDTH; y ++)
					{
						if (boardIn[x][y] != null && boardIn[x][y].getTeam() == super.getTeam())
						{
							//For all of the possible moves...
							for(int[] a : possibleMoves)
							{
								int[] move = a.clone();
								move[1] = x;
								move[2] = y;
								Piece[][] board = copy(boardIn);
								if (KhetGameEngine.executeMove(move, super.getTeam(), board))
								{
									shootLaser(board, super.getTeam());
									//Here's the recursion!
									int[] info = recDetermineMove(board, depth + 1);
									if (info[0] > max)
									{
										max = info[0];
										average = info[1];
									}
									else if (info[0] == max)
									{
										if (info[1] > average)
										{
											average = info[1];
										}
									}
								}
							}
						}
					}
				}
				value[0] = max;
				value[1] = average;
			}
			else
			{
				//If we won in the move we just made, return down.
				if (evaluate == INFINITY)
				{
					return new int[] {INFINITY, INFINITY};
				}
				//Choose the worst of the 140 nodes
				int min = INFINITY;
				int sum = 0;
				int num = 0;
				//Search the board
				for (int x = 0; x < HEIGHT; x ++)
				{
					for (int y = 0; y < WIDTH; y ++)
					{
						if (boardIn[x][y] != null && boardIn[x][y].getTeam() == 1 - super.getTeam())
						{
							// For every possible move...
							for(int[] a : possibleMoves)
							{
								int[] move = a.clone();
								move[1] = x;
								move[2] = y;
								Piece[][] board = copy(boardIn);
								if (KhetGameEngine.executeMove(move, 1 - super.getTeam(), board))
								{
									shootLaser(board, 1 - super.getTeam());
									//Here's the recursion!
									int[] info = recDetermineMove(board.clone(), depth + 1);
									if (info[0] < min)
									{
										min = info[0];
									}
									sum += info[1];
									num ++;
								}
							}
						}
					}
				}
				value[0] = min;
				value[1] = (int) (1.0*sum/num);
			}
			return value;
		}
		else
		{
			return new int[] {evaluate, evaluate};
		}
	}

	/**
	 * Shoots the laser and kills any relevant piece. Stolen from main program.
	 */
	private void shootLaser(Piece[][] board, int playerTurn)
	{
		int[] space;
		if(playerTurn == 0){
			space = auxTraceLaser(board, 0, 0, 180, playerTurn);
		}
		else{
			space = auxTraceLaser(board, board.length - 1, board[0].length - 1, 0, playerTurn);
		}
		if (space != null)
		{
			board[space[0]][space[1]] = null;
		}
	}

	private int [] auxTraceLaser(Piece board[][], int row, int col, int incDirect, int playerTurn)
	{
		try{
			Piece nextPiece = board[row][col];
			int reflectDirect;
			if(nextPiece == null)
			{
				reflectDirect = incDirect;
			}
			else
			{
				reflectDirect = nextPiece.reflectedDirection(incDirect);
			}

			if(reflectDirect == -1)
			{
				if (board[row][col] instanceof Anubis)
				{
					int facing = board[row] [col].getDirection();

					if(incDirect == Piece.keepInRange(facing + 180))
					{
						return null;
					}
				}

				return new int [] {row, col};
			}
			else if(reflectDirect == 0)
			{
				return auxTraceLaser(board, row - 1, col,reflectDirect, playerTurn);
			}
			else if(reflectDirect == 90)
			{
				return auxTraceLaser(board, row, col + 1, reflectDirect, playerTurn);
			}
			else if(reflectDirect == 180)
			{
				return auxTraceLaser(board, row + 1, col,reflectDirect, playerTurn);
			}
			else
			{
				return auxTraceLaser(board, row, col - 1, reflectDirect, playerTurn);
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	private Piece[][] copy(Piece[][] arg)
	{
		Piece[][] temp = new Piece[arg.length][arg[0].length];
		for (int i = 0; i < arg.length; i ++)
		{
			for (int j = 0; j < arg[0].length; j ++)
			{
				if (arg[i][j] == null)
				{
					temp[i][j] = null;
				}
				else
				{
					if (arg[i][j] instanceof Anubis)
					{
						temp[i][j] = new Anubis(arg[i][j].getDirection(), arg[i][j].getTeam());
					}
					else if (arg[i][j] instanceof Pyramid)
					{
						temp[i][j] = new Pyramid(arg[i][j].getDirection(), arg[i][j].getTeam());
					}
					else if (arg[i][j] instanceof Scarab)
					{
						temp[i][j] = new Scarab(arg[i][j].getDirection(), arg[i][j].getTeam());
					}
					else if (arg[i][j] instanceof Pharaoh)
					{
						temp[i][j] = new Pharaoh(arg[i][j].getDirection(), arg[i][j].getTeam());
					}
					else
					{
						System.out.println("Wait... What?");
					}
				}
			}
		}
		return temp;
	}

	//Because when our AI is about to win, it gets "special". We'll just leave it at that.
	private boolean checkWin(Piece[][] board)
	{
		for (int x = 0; x < HEIGHT; x ++)
		{
			for (int y = 0; y < WIDTH; y ++)
			{
				if (board[x][y] != null && board[x][y].getTeam() == 1 - getTeam() && board[x][y] instanceof Pharaoh)
				{
					return false;
				}
			}
		}
		return true;
	}

	/////////////////////////////////////////////End Jeff's Code -- Start John's Code/////////////////////////////////////////////
	
	//population rating finals
	private final double PYRAMID_POINTS = 2;
	private final double ANUBIS_POINTS = 4;
	private final int MAX_PYRAMIDS = 7;
	private final int MAX_ANUBII = 2;

	//edge rating finals
	private final int MAX_EDGE_PYRAMIDS = 2;
	private final int MIN_EDGE_PYRAMIDS = 1;

	//MYEH...ACTUALLY MULTIPLY RATINGS AFTER GETTING THEM
	private final int RANDOM_RATING_MULTIPLE = 50;
	private final int PHARAOH_RATING_MULTIPLE = 75;
	private final int SCARAB_RATING_MULTIPLE = 100;
	private final int EDGE_RATING_MULTIPLE = 1000;
	private final int POPULATION_RATING_MULTIPLE = 10000;
	private final int MAX_RATING = 256;
	private final int RATING_MULTIPLE = 10;

	public int evaluate(Piece[][] board)
	{
		int rating = 0;

		int populationRating = populationRating(board);
		if(populationRating == INFINITY)
		{
			return INFINITY;
		}
		else if(populationRating == -INFINITY)
		{
			return -INFINITY;
		}

		rating = populationRating * POPULATION_RATING_MULTIPLE;

		int edgeRating = EDGE_RATING_MULTIPLE * edgeRating(board);
		rating +=  edgeRating;

		int scarabRating = SCARAB_RATING_MULTIPLE * scarabRating(board);
		rating += scarabRating;

		int pharaohRating = PHARAOH_RATING_MULTIPLE * pharaohRating(board);
		rating += pharaohRating;

		int randomRating = RANDOM_RATING_MULTIPLE * (int)(Math.random() * MAX_RATING);
		rating += randomRating;

		return rating;
	}

	/**
	 * Rates position of the friendly pharaoh relative to its ideal position (original position)
	 * @param board board of pieces
	 * @return rating of friendly pharaoh's position
	 */
	private int pharaohRating(Piece [][] board)
	{
		Object[] pharaohList = piecesOfType(board, new Pharaoh(0, super.getTeam())).get(0);
		int row = (Integer)pharaohList[1];
		int col = (Integer)pharaohList[2];

		int idealRow;
		int idealCol;
		int distance;

		//on silver team, team 0, top of board
		if(super.getTeam() == 0)
		{
			//ideal position (0,4)
			idealRow = 0;
			idealCol = 4;

			int xDistance = col - idealCol;
			int yDistance = row - idealRow;

			distance = (int)Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
		}
		//on red team, team 1, bottom of board
		else
		{
			//ideal position (7,5)
			idealRow = 7;
			idealCol = 5;

			int xDistance = col - idealCol;
			int yDistance = row - idealRow;

			distance = (int)Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
		}

		int pharaohRating = MAX_RATING - (RATING_MULTIPLE * distance * distance);

		return pharaohRating;
	}

	/**
	 * Rates the positioning of friendly scarabs on the board
	 * @param board board of pieces
	 * @return rating of friendly scarab positions
	 */
	private int scarabRating(Piece [][] board)
	{
		ArrayList <Object[]> scarabInfoList = piecesOfType(board, new Scarab(0, super.getTeam()));

		Object[] friendlyPharaohInfo = piecesOfType(board, new Pharaoh(0, super.getTeam())).get(0);
		Object[] enemyPharaohInfo = piecesOfType(board, new Pharaoh(0, 1-super.getTeam())).get(0);

		int offenseScarabCount = 0;
		int defenseScarabCount = 0;

		int avgOffenseDistance = 0;
		int avgDefenseDistance = 0;

		Integer friendlyPharaohRow = (Integer)friendlyPharaohInfo[1];
		Integer friendlyPharaohCol = (Integer)friendlyPharaohInfo[2];

		Integer enemyPharaohRow = (Integer)enemyPharaohInfo[1];
		Integer enemyPharaohCol = (Integer)enemyPharaohInfo[2];

		//on silver team (team 0), top of board
		if(super.getTeam() == 0)
		{
			for(Object[] scarabInfo: scarabInfoList)
			{
				int scarabRow = (Integer)scarabInfo[1];
				int scarabCol = (Integer)scarabInfo[2];

				//scarab on top half of board
				if(scarabRow < board.length / 2)
				{
					defenseScarabCount ++;

					int yDistance = scarabRow - friendlyPharaohRow;
					int xDistance = scarabCol - friendlyPharaohCol;
					int distance = (int) Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

					if(avgDefenseDistance == 0)
						avgDefenseDistance += distance;
					else
						avgDefenseDistance = (avgDefenseDistance + distance) / 2;
				}
				//scarab on bottom half of board
				else
				{
					offenseScarabCount ++;

					int yDistance = scarabRow - enemyPharaohRow;
					int xDistance = scarabCol - enemyPharaohCol;
					int distance = (int) Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

					if(avgOffenseDistance == 0)
						avgOffenseDistance += distance;
					else
						avgOffenseDistance = (avgOffenseDistance + distance) / 2;
				}
			}
		}
		//on red team (team red), bottom of board
		else
		{
			for(Object[] scarabInfo: scarabInfoList)
			{
				int scarabRow = (Integer)scarabInfo[1];
				int scarabCol = (Integer)scarabInfo[2];

				//scarab on top half of board
				if((Integer)scarabInfo[1] < board.length / 2)
				{
					offenseScarabCount ++;

					int yDistance = scarabRow - enemyPharaohRow;
					int xDistance = scarabCol - enemyPharaohCol;
					int distance = (int) Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

					if(avgOffenseDistance == 0)
						avgOffenseDistance += distance;
					else
						avgOffenseDistance = (avgOffenseDistance + distance) / 2;
				}
				//scarab on bottom half of board
				else
				{
					defenseScarabCount ++;

					int yDistance = scarabRow - friendlyPharaohRow;
					int xDistance = scarabCol - friendlyPharaohCol;
					int distance = (int) Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

					if(avgDefenseDistance == 0)
						avgDefenseDistance += distance;
					else
						avgDefenseDistance = (avgDefenseDistance + distance) / 2;
				}
			}
		}

		//optimal state: scarabs evenly spread
		if(defenseScarabCount == 1 && offenseScarabCount == 1)
		{
			//want scarabs to be 1 block away from pharaohs
			int offenseRating = MAX_RATING - ((avgOffenseDistance - 1) * RATING_MULTIPLE);
			int defenseRating = MAX_RATING - ((avgDefenseDistance - 1) * RATING_MULTIPLE);
			return (offenseRating + defenseRating) / 2;
		}
		//sub-optimal state: scarabs both on defense
		else if(defenseScarabCount == 2)
		{
			//not good, but want scarabs as close to pharaoh as possible
			int offenseRating = -MAX_RATING;
			int defenseRating = MAX_RATING - ((avgDefenseDistance - 1) * RATING_MULTIPLE);
			return (offenseRating + defenseRating) / 2;
		}
		//worst state: scarabs both on offense
		else
		{
			//no.  just no.
			return -MAX_RATING;
		}

	}

	/**
	 * Returns arrayList of arrays with {piece, row, col} on board with same type and team as pieceType.
	 * @param board board of pieces
	 * @param pieceType sample piece that is instance of filter piece type
	 * @return ArrayList of arrays with {piece, row, col} of pieces of specified type
	 */
	private ArrayList<Object[]> piecesOfType(Piece [][] board, Piece pieceType)
	{
		int team = pieceType.getTeam();
		ArrayList <Object[]> pieceInfoList = new ArrayList<Object[]>();

		if(pieceType instanceof Pyramid)
		{
			for(int row = 0; row < board.length; row++)
			{
				Piece[] pieceRow = board[row];
				for(int col = 0; col < pieceRow.length; col++)
				{
					Piece piece = pieceRow[col];
					if(piece != null && piece instanceof Pyramid && piece.getTeam() == team)
					{
						Object[] pieceInfo = {piece, row, col};
						pieceInfoList.add(pieceInfo);
					}
				}
			}
		}
		else if(pieceType instanceof Scarab)
		{
			for(int row = 0; row < board.length; row++)
			{
				Piece[] pieceRow = board[row];
				for(int col = 0; col < pieceRow.length; col++)
				{
					Piece piece = pieceRow[col];
					if(piece != null && piece instanceof Scarab && piece.getTeam() == team)
					{
						Object[] pieceInfo = {piece, row, col};
						pieceInfoList.add(pieceInfo);
					}
				}
			}
		}
		else if(pieceType instanceof Anubis)
		{
			for(int row = 0; row < board.length; row++)
			{
				Piece[] pieceRow = board[row];
				for(int col = 0; col < pieceRow.length; col++)
				{
					Piece piece = pieceRow[col];
					if(piece != null && piece instanceof Anubis && piece.getTeam() == team)
					{
						Object[] pieceInfo = {piece, row, col};
						pieceInfoList.add(pieceInfo);
					}
				}
			}
		}
		else if(pieceType instanceof Pharaoh)
		{
			for(int row = 0; row < board.length; row++)
			{
				for(int col = 0; col < board[row].length; col++)
				{
					Piece piece = board[row][col];
					if(piece != null && piece instanceof Pharaoh && piece.getTeam() == team)
					{
						Object[] pieceInfo = {piece, row, col};
						pieceInfoList.add(pieceInfo);
					}
				}
			}
		}
		else
		{
			System.out.println("Wat.");
		}
		return pieceInfoList;
	}

	/**
	 * Rates arrangement of friendly pieces on edge of board
	 * @param board board of pieces
	 * @return rating number between -256 to 256
	 */
	private int edgeRating(Piece [][] board)
	{
		int team = super.getTeam();

		ArrayList <Piece> firstColPieces = new ArrayList<Piece>();
		ArrayList <Piece> lastColPieces = new ArrayList<Piece>();

		//step through rows
		for(Piece[] pieceRow: board)
		{
			Piece firstColPiece = pieceRow[0];
			Piece lastColPiece = pieceRow[board[0].length - 1];

			//add piece in first column
			if(firstColPiece != null)
			{
				firstColPieces.add(firstColPiece);
			}
			//add piece in last column
			if(lastColPiece != null)
			{
				lastColPieces.add(lastColPiece);
			}
		}

		//assign teams to column pieces
		ArrayList <Piece> friendlyPieces;
		if(team == 0)
		{
			friendlyPieces = firstColPieces;
		}
		else
		{
			friendlyPieces = lastColPieces;
		}

		//calculate rating
		int friendlyRating;

		//friendly rating
		//no friendly pieces on edge
		if(friendlyPieces.size() == 0)
		{
			friendlyRating = 0;
		}
		//friendly pieces on edge
		else
		{
			//count friendly pieces on edge
			int numPyramids = 0;
			int numScarabs = 0;
			int numPharaohs = 0;
			int numAnubii = 0;
			for(Piece currentPiece: friendlyPieces)
			{
				if(currentPiece instanceof Pyramid)
				{
					numPyramids ++;
				}
				else if(currentPiece instanceof Scarab)
				{
					numScarabs ++;
				}
				else if(currentPiece instanceof Pharaoh)
				{
					numPharaohs ++;
				}
				else if(currentPiece instanceof Anubis)
				{
					numAnubii ++;
				}
			}

			//no pyramids
			if(numPyramids == 0)
			{
				//worst state: no reflective pieces
				if(numScarabs == 0)
				{
					friendlyRating = -MAX_RATING;
				}
				//sub-optimal state: some reflective pieces
				else
				{
					friendlyRating = 0;
				}
			}
			//1-2 pyramids
			else if(numPyramids >= MIN_EDGE_PYRAMIDS && numPyramids <= MAX_EDGE_PYRAMIDS)
			{
				//optimum state: no other pieces
				if((numScarabs + numPharaohs + numAnubii) == 0)
				{
					friendlyRating = MAX_RATING;
				}
				//sub-optimal state: some other pieces
				else
				{
					friendlyRating = MAX_RATING - ((numScarabs + numPharaohs + numAnubii) * RATING_MULTIPLE);
				}
			}
			//3 or more pyramids
			else
			{
				//sub-optimum state: no other pieces
				if((numScarabs + numPharaohs + numAnubii) == 0)
				{
					friendlyRating = MAX_RATING - ((numPyramids - MAX_EDGE_PYRAMIDS) * RATING_MULTIPLE);
				}
				//sub-optimal state: some other pieces
				else
				{
					friendlyRating = MAX_RATING - ((numPyramids - MAX_EDGE_PYRAMIDS) * RATING_MULTIPLE) - ((numScarabs + numPharaohs + numAnubii) * RATING_MULTIPLE);
				}
			}
		}

		return friendlyRating;
	}

	/**
	 * Steps through all spots on the board and rates the game state based on the weighted 
	 * population of friendly and enemy pieces
	 * @param board 2D array pieces
	 * @return INFINITY if friendly team has won
	 * -INFINITY if enemy team has won
	 * otherwise, number between -MAX_RATING and MAX_RATING
	 */
	private int populationRating(Piece [][] board)
	{
		int friendlyPyramids = 0;
		int friendlyPharaohs = 0;
		int friendlyAnubii = 0;

		int enemyPyramids = 0;
		int enemyPharaohs = 0;
		int enemyAnubii = 0;

		//look through all spots on board, increment enemy and friendly populations
		for (int i = 0; i < board.length; i ++)
		{
			//step through spots on board
			for (int u = 0; u < board[i].length; u ++)
			{
				//is a piece
				if (board [i] [u] != null)
				{
					Piece currentPiece = board[i][u];
					//is a friendly piece
					if(currentPiece.getTeam() == getTeam())
					{
						//check for type
						if(currentPiece instanceof Pyramid)
						{
							friendlyPyramids ++;
						}
						else if(currentPiece instanceof Pharaoh)
						{
							friendlyPharaohs ++;
						}
						else if(currentPiece instanceof Anubis)
						{
							friendlyAnubii ++;
						}
					}
					//is an enemy piece
					else
					{
						//check for type
						if(currentPiece instanceof Pyramid)
						{
							enemyPyramids ++;
						}
						else if(currentPiece instanceof Pharaoh)
						{
							enemyPharaohs ++;
						}
						else if(currentPiece instanceof Anubis)
						{
							enemyAnubii ++;
						}
					}
				}
			}
		}

		//absolute states (won/lost)
		if(friendlyPharaohs < 1)
		{
			return -INFINITY;
		}
		if(enemyPharaohs < 1)
		{
			return INFINITY;
		}
		//turn population ratings into fractions of total possible, multiply by MAX_RATING and add together
		else
		{
			double friendlyPopulationRating = 0;
			friendlyPopulationRating += PYRAMID_POINTS * friendlyPyramids;
			friendlyPopulationRating += ANUBIS_POINTS * friendlyAnubii;
			friendlyPopulationRating = (friendlyPopulationRating / ((MAX_PYRAMIDS * PYRAMID_POINTS) + (MAX_ANUBII * ANUBIS_POINTS))) * MAX_RATING;

			double enemyPopulationRating = 0;
			enemyPopulationRating += PYRAMID_POINTS * enemyPyramids;
			enemyPopulationRating += ANUBIS_POINTS * enemyAnubii;
			enemyPopulationRating = (enemyPopulationRating / ((MAX_PYRAMIDS * PYRAMID_POINTS) + (MAX_ANUBII * ANUBIS_POINTS))) * MAX_RATING * -1;

			double totalPopulationRating = friendlyPopulationRating + enemyPopulationRating;

			return (int)totalPopulationRating;
		}
	}
}
