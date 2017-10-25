package reversi.server;

import reversi.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.InetAddress;


public class ReversiPlayer implements ReversiProtocol
{
    /** the socket of the player. */
    private Socket playerConn;
    /** the BufferedReader for player. */
    private BufferedReader playerIn;
    /** the PrintWriter for player. */
    private PrintWriter playerOut;

    /**
     * Constructs a player object
     * @param conn The connection Socket that player is connected to
     * @param numRows The number of rows in the Reversi game (used for connect message)
     * @param numCols The number of cols in the Reversi game (used for connect message)
     */
    public ReversiPlayer(Socket conn, int numRows, int numCols) throws IOException
    {
        this.playerConn = conn;
        this.playerIn = new BufferedReader(new InputStreamReader(playerConn.getInputStream()));
        this.playerOut = new PrintWriter(playerConn.getOutputStream(), true);

        successfulConnect(numRows, numCols);
    }

    public String [] makeMove() throws IOException
    {
        playerOut.println(MAKE_MOVE);
        return playerIn.readLine().split(" ");
    }

    public void moveMade(String moveMade)
    {
        playerOut.println(moveMade);
    }

    public void sendResult(String result)
    {
        playerOut.println(result);
    }

    public void sendError()
    {
        playerOut.println(ERROR);
    }

    public void close() throws IOException
    {
        if(this.playerOut != null) playerOut.close();
        if(this.playerIn != null) playerIn.close();
        if(this.playerConn != null) playerConn.close();
    }

    InetAddress getInetAddress()
    {
        return playerConn.getInetAddress();
    }

    int getPort()
    {
        return playerConn.getPort();
    }

    private void successfulConnect(int rows, int cols)
    {
        playerOut.println(CONNECT + " " + rows + " " + cols);
    }
}
