/*
 * LINGI2241 - Computer Systems
 *      Augustin d'Oultremont - 2239 1700
 *      Valentin Lemaire - 1634 1700
 *
 *      This class is heavily based on this tutorial:
 *      https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
 *
 *      Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 *      Redistribution and use in source and binary forms, with or without
 *      modification, are permitted provided that the following conditions
 *      are met:
 *
 *          - Redistributions of source code must retain the above copyright
 *            notice, this list of conditions and the following disclaimer.
 *
 *          - Redistributions in binary form must reproduce the above copyright
 *            notice, this list of conditions and the following disclaimer in the
 *            documentation and/or other materials provided with the distribution.
 *
 *          - Neither the name of Oracle or the names of its
 *            contributors may be used to endorse or promote products derived
 *            from this software without specific prior written permission.
 *
 *      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *      IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *      THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *      PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *      CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *      EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *      PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *      PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *      LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *      NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *      SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleServer {

    private static final int N_THREADS = 2;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java SimpleServer <port number> <database text file>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        String dbfile = args[1];

        String[][] dataArray = initArray(dbfile);

        ServerSocket serverSocket = new ServerSocket(portNumber);
        for (int i=0; i < N_THREADS; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try (
                            Socket clientSocket = serverSocket.accept();
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        ) {
                            String inputLine, outputLine;

                            // Initiate conversation with client
                            SimpleServerProtocol ssp = new SimpleServerProtocol(dataArray);

                            while ((inputLine = in.readLine()) != null) {
                                outputLine = ssp.processInput(inputLine);
                                out.println(outputLine);
                                if (outputLine.equals("Bye."))
                                    break;
                            }
                        } catch (IOException e) {
                            System.out.println("Exception caught when trying to listen on port "
                                    + portNumber + " or listening for a connection");
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }).start();
        }
    }

    public static String[][] initArray(String filename) {
        try {
            ArrayList<String[]> list = new ArrayList<>();
            File file = new File(filename);

            Scanner reader = new Scanner(file);
            String[] data;
            while (reader.hasNextLine()) {
                data = reader.nextLine().split("@@@");
                list.add(data);
            }
            reader.close();

            return list.toArray(new String[list.size()][list.get(0).length]);

        } catch (FileNotFoundException e) {
            System.err.println("No such file");
            return null;
        }
    }

    public static class SimpleServerProtocol {
        private String[][] dataArray;

        public SimpleServerProtocol(String[][] dataArray) {
            this.dataArray = dataArray;
        }

        public String processInput(String command) {
            if (command != null) {
                String[] data = command.split(";");
                if (data.length != 2) {
                    System.err.println("Wrong command format.");
                    return null;
                }
                String[] types = data[0].split(",");
                String regex = data[1];
                Pattern pattern = Pattern.compile(regex);

                StringBuilder toSend = new StringBuilder();
                for (int i = 0; i < this.dataArray.length; i++) {
                    if (types.length == 0) {
                        Matcher matcher = pattern.matcher(this.dataArray[i][1]);
                        if (matcher.find()) {
                            toSend.append(this.dataArray[i][0]).append("@@@").append(this.dataArray[i][1]).append("\n");
                        }
                    } else {
                        for (String type : types) {
                            if (this.dataArray[i][0].equals(type)) {
                                Matcher matcher = pattern.matcher(this.dataArray[i][1]);
                                if (matcher.find()) {
                                    toSend.append(this.dataArray[i][0]).append("@@@").append(this.dataArray[i][1]).append("\n");
                                }
                                break;
                            }
                        }
                    }
                }
                return toSend.toString();
            } else {
                return null;
            }
        }
    }
}
