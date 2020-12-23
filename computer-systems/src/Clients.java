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

import utils.Request;

import java.io.*;
import java.net.*;
import java.util.*;

public class Clients {

    static Random rand;

    public static void main(String[] args) {
        // parse args
        if (args.length != 7) {
            System.err.println("Usage: java Clients <host name> <port number> <number of clients> <input file> <results file> <mean delay> <verbose>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);
        String inputFilename = args[3];
        String outputFilename = args[4];
        float meanDelay = Float.parseFloat(args[5]);
        boolean verbose = Boolean.parseBoolean(args[6]);

        final List<String> resultsList = new ArrayList<>();

        rand = new Random();

        Thread[] threads = new Thread[numberOfClients];

        List<String> requests = generateRequestList(inputFilename);
        int[] requestsPerClient = new int[numberOfClients];

        try (
            final Socket socket = new Socket(hostName, portNumber);
            final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            for (int i = 0; i<numberOfClients; i++){
                final int idx = i;

                // handle sending to server
                threads[i] = new Thread(() -> {
                    try {
                        Thread sendingThread = new Thread(() -> {
                            List<String> requestsForThread = new ArrayList<>(requests);
                            Collections.shuffle(requestsForThread);
                            //int toDrop = rand.nextInt(requestsForThread.size());
                            //requestsForThread.subList(toDrop, requestsForThread.size()).clear();
                            synchronized (requestsPerClient) {
                                requestsPerClient[idx] = requestsForThread.size();
                            }

                            for (String r : requestsForThread){
                                try {
                                    Thread.sleep((long) exponential(1/meanDelay));
                                    Request req = new Request(r, idx);
                                    synchronized (out) {
                                        req.setSentByClientTime(new Date());
                                        out.writeObject(req);
                                        out.flush();
                                    }
                                } catch (InterruptedException | IOException e){
                                    System.err.println(e.getMessage());
                                }
                            }
                        });

                        sendingThread.start();
                        sendingThread.join();

                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                });

                threads[i].start();
            }

            Thread receivingThread = new Thread(() -> {
                int[] counts = new int[numberOfClients];
                int doneCount = 0;
                try {
                    Request fromServer;
                    while ((doneCount < numberOfClients) && ((fromServer = (Request) in.readObject()) != null)) {
                        fromServer.setReceivedByClientTime(new Date());
                        logResponse(fromServer, resultsList);
                        if (verbose) {
                            System.out.println("Received Server Response of length : " + fromServer.getResponseValue().split("\n").length);
                            // System.out.println("Received Server Response : \n" + fromServer.getResponseValue());
                        }
                        counts[fromServer.getClientID()]++;
                        synchronized (requestsPerClient) {
                            if (counts[fromServer.getClientID()] == requestsPerClient[fromServer.getClientID()]) {
                                System.out.println("Client " + fromServer.getClientID() + " finished");
                                doneCount++;
                            }
                        }
                    }
                } catch (EOFException e) {
                    // EOF means that the server has stopped sending data
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error in request format");
                    System.exit(1);
                }
            });

            receivingThread.start();

            for (Thread thread : threads) {
                thread.join();
            }
            receivingThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        writeResultsFile(resultsList, outputFilename);

    }


    public static double exponential(float lambda) {
        return Math.log(1-rand.nextDouble())/(-lambda);
    }

    public static synchronized void logResponse(Request r, List<String> resultsList) {
        r.computeIntervals();
        String results = r.createTimeString();
        resultsList.add(results);
    }

    public static void writeResultsFile(List<String> resultsList, String outputFilename) {
        try {
            FileWriter outputWriter = new FileWriter(outputFilename);
            for (String line : resultsList) {
                outputWriter.write(line);
            }
            outputWriter.close();
            System.out.println("Saved results to "+outputFilename);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static List<String> generateRequestList(String inputFilename) {
        List<String> requests = new ArrayList<>();
        try (
            final Scanner inputScanner = new Scanner(new File(inputFilename));
        ) {
            String line;
            while (inputScanner.hasNext()) {
                if ((line = inputScanner.nextLine()) != null)
                    requests.add(line);
            }
            return requests;
        } catch (IOException e) {
            System.out.println("Empty file or file not found");
            return requests;
        }
    }
}
