/* LINGI2241 - Computer Systems
 *      Valentin Lemaire - 1634 1700
 *      Augustin d'Oultremont - 2239 1700
 *
 *      This class is heavily based on this tutorial :
 *      https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
 *
 *      Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Clients {
    public static void main(String[] args) {
        // parse args
        if (args.length != 6) {
            System.err.println("Usage: java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);
        String inputFilename = args[3];
        float meanDelay = Float.parseFloat(args[4]);
        boolean verbose = Boolean.parseBoolean(args[5]);


        Thread[] threads = new Thread[numberOfClients];

        for (int i = 0; i<numberOfClients; i++){
            final int idx = i;

            // handle sending to server
            threads[i] = new Thread(() -> {
                try (
                        final Socket socket = new Socket(hostName, portNumber);
                        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final Scanner inputScanner = new Scanner(new File(inputFilename));
                ) {
                    List<String> requests = new ArrayList<>();
                    String line;
                    while (inputScanner.hasNext()) {
                        if ((line = inputScanner.nextLine()) != null)
                            requests.add(line);
                    }
                    int totalRequests = requests.size();
                    Thread sendingThread = new Thread(() -> {
                        for (String r : requests){
                            try {
                                Thread.sleep(poisson(meanDelay));
                                out.println(r);
                            } catch (InterruptedException e){
                                System.err.println(e.getMessage());
                            }
                        }
                    });

                    Thread receivingThread = new Thread(() -> {
                        int count = 0;
                        try {
                            String fromServer;
                            while ((count < totalRequests) && ((fromServer = in.readLine()) != null)) {
                                if (verbose) {
                                    System.out.println("Server: " + fromServer);
                                }
                                if (fromServer.equals(""))
                                    count++;
                            }
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            System.exit(1);
                        }
                    });
                    sendingThread.start();
                    receivingThread.start();
                    sendingThread.join();
                    receivingThread.join();
                    System.out.println("Client "+idx+" finished");

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            });

            threads[i].start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }


    public static int poisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }
}
