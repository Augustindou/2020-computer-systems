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

import utils.Buffer;
import utils.Request;
import utils.Cache;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimizedServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 3) {
            System.err.println("Usage: java OptimizedServer <port number> <database text file> <number of threads>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        String dbfile = args[1];
        final int N_THREADS = Integer.parseInt(args[2]);

        ServerSocket serverSocket = new ServerSocket(portNumber);
        Buffer<Request> buffer = new Buffer<>(100);
        OptimizedServerProtocol osp = new OptimizedServerProtocol(initArray(dbfile), 10, 5);

        System.out.println("Server is up");

        Socket clientSocket = serverSocket.accept();
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

        // Initializing worker threads
        Thread[] threads = new Thread[N_THREADS];
        for (int i=0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Request r;
                    while ((r = buffer.take()) != null) {
                        r.setFinishedQueuingTime(new Date());
                        r.setStartingToTreatRequestTime(new Date());
                        String outputLine = osp.processInput(r.getRequestValue());
                        r.setFinishedTreatingRequestTime(new Date());
                        r.setResponseValue(outputLine);
                        synchronized (out) {
                            out.writeObject(r);
                            out.flush();
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            // starting the threads right away
            threads[i].start();
        }

        // Loop to fill buffer
        try {
            Request received;
            while ((received = (Request) in.readObject()) != null) {
                received.setStartingQueuingTime(new Date());
                if (!buffer.add(received)) System.err.println("Full buffer, had to drop a request");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Wrong object format");
        } catch (EOFException e) {
            // When the final object is read, the buffer throws and EOF exception.
        }

        // Waiting for the threads to finish to return
        for (int i = 0; i < N_THREADS; i++) {
            threads[i].join();
        }

        in.close();
        out.close();
        serverSocket.close();
        clientSocket.close();

    }

    public static String[][] initArray(String filename) {
        try {
            File file = new File(filename);

            Scanner reader = new Scanner(file);
            Map<Integer, Set<String>> map = new HashMap<>();

            // Parsing file and adding it to hashmap with category as key
            String[] data;
            while (reader.hasNextLine()) {
                data = reader.nextLine().split("@@@");
                int idx = Integer.parseInt(data[0]);
                if (!map.containsKey(idx)) {
                    map.put(idx, new HashSet<>());
                }
                map.get(idx).add(data[1]);
            }
            reader.close();

            // Transforming the hashmap in a 2D array
            String[][] dataArray = new String[map.size()][];
            for (Map.Entry<Integer, Set<String>> e : map.entrySet()) {
                dataArray[e.getKey()] = e.getValue().toArray(new String[0]);
            }
            return dataArray;
        } catch (FileNotFoundException e) {
            System.err.println("No such file");
            return null;
        }
    }

    public static class OptimizedServerProtocol {
        private final String[][] dataArray;
        private final Cache cache;

        public OptimizedServerProtocol(String[][] dataArray, int N, float theta) {
            this.dataArray = dataArray;
            this.cache = new Cache(N, theta);
        }

        public String processInput(String command) throws InterruptedException {
            if (command == null)
                return null;

            String[] data = command.split(";");
            if (data.length != 2) {
                System.err.println("Wrong command format.");
                return null;
            }

            // Checking if item is in cache
            String cacheResponse = this.cache.get(command);
            if (cacheResponse != null) {
                return cacheResponse;
            }

            // If not in cache doing some processing
            String[] types = data[0].split(",");
            String regex = data[1];
            Pattern pattern = Pattern.compile(regex);

            int[] intTypes;
            if (types.length == 1 && types[0].equals("")) {
                // If no types we search them all
                intTypes = new int[]{0, 1, 2, 3, 4, 5};
            } else {
                intTypes = new int[types.length];
                for (int i = 0; i < types.length; i++) {
                    intTypes[i] = Integer.parseInt(types[i]);
                }
            }

            // Search in each type are independent, it can be done in concurrent threads
            Thread[] threads = new Thread[intTypes.length];
            StringBuilder builder = new StringBuilder();
            for (int idx : intTypes) {
                String[][] dataArray = OptimizedServerProtocol.this.dataArray;
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < dataArray[idx].length; j++) {
                    Matcher matcher = pattern.matcher(dataArray[idx][j]);
                    if (matcher.find()) {
                        builder.append(idx).append("@@@").append(dataArray[idx][j]).append("\n");
                    }
                }
            }

            String response;
            if (builder.length() > 0)
                response = builder.toString();
            else
                response = "";

            // The item was not in cache so it is added
            this.cache.add(command, response);
            return response;
        }
    }
}
