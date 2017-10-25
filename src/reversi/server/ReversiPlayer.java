package reversi.server;

import reversi.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.InetAddress;


/**
 * A middle-man class that holds each player.
 *
 * @author Kevin Becker
 */
class ReversiPlayer implements ReversiProtocol
{
    /** the socket of the player. */
    private Socket playerConn;
    /** the BufferedReader for player. */
    private BufferedReader playerIn;
    /** the PrintWriter for player. */
    private PrintWriter playerOut;

    /**
     * Constructs a player object.
     *
     * @param conn The connection Socket that player is connected to.
     * @param numRows The number of rows in the Reversi game (used for connect message).
     * @param numCols The number of cols in the Reversi game (used for connect message).
     */
    ReversiPlayer(Socket conn, int numRows, int numCols) throws IOException
    {
        this.playerConn = conn;
        this.playerIn = new BufferedReader(new InputStreamReader(playerConn.getInputStream()));
        this.playerOut = new PrintWriter(playerConn.getOutputStream(), true);

        successfulConnect(numRows, numCols);
    }

    /**
     * Tells the player it is their turn to move and returns their response, split by space.
     *
     * @return The player's move split by spaces
     *
     * @throws IOException If an IOException is encountered while reading in the response.
     */
    String [] makeMove() throws IOException
    {
        playerOut.println(MAKE_MOVE);
        return playerIn.readLine().split(" ");
    }

    /**
     * Tells the player that a move was made so they can update their game.
     *
     * @param moveMade The move that was made by the previous player.
     */
    void moveMade(String moveMade)
    {
        playerOut.println(moveMade);
    }

    /**
     * Sends the result of the game to the player.
     *
     * @param result The result of the game for the player (GAME_WON, GAME_LOST, or GAME_TIED).
     */
    void sendResult(String result)
    {
        playerOut.println(result);
    }

    /**
     * Sends to the client that an error was encountered so it may gracefully exit.
     */
    void sendError()
    {
        playerOut.println(ERROR);
    }

    /**
     * Closes all of the fields.
     *
     * @throws IOException If an IOException is encountered, it is thrown.
     */
    void close() throws IOException
    {
        if(this.playerOut != null) playerOut.close();
        if(this.playerIn != null) playerIn.close();
        if(this.playerConn != null) playerConn.close();
    }

    /**
     * Gets the InetAddress of the player (used for server output).
     *
     * @return The InetAddress of the player.
     */
    InetAddress getInetAddress()
    {
        return playerConn.getInetAddress();
    }

    /**
     * Gets the port that the player is connected to (not the external port).
     *
     * @return The port of the player.
     */
    int getPort()
    {
        return playerConn.getPort();
    }

    /**
     * Tells the player they have connected successfully, followed by the number of rows and number of columns.
     *
     * @param rows The number of rows in the Reversi board.
     * @param cols The number of columns in the Reversi board.
     */
    private void successfulConnect(int rows, int cols)
    {
        playerOut.println(CONNECT + " " + rows + " " + cols);
    }
}
