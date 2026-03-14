/*
 * Copyright 2026 PicturePlayer;Nserly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.nserly.SoftwareCollections_API.Interaction.SoftwareInteraction.TCP.Server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import top.nserly.SoftwareCollections_API.Interaction.SoftwareInteraction.TCP.Interactions;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class stores data based on the thread of the instantiated class
 * <p>
 * It can also read the thread object when instantiated
 * </p>
 */
@Slf4j
public class TCP_ServerSocket {
    @Getter
    protected Set<String> blackList;// Blacklist
    @Setter
    protected CheckClient checkForClient;// Check if the client is valid
    @Getter
    @Setter
    protected int maxConnect;// Maximum number of server connections
    @Getter
    protected ExecutorService virtualThreadExecutor;// Virtual thread executor
    protected Class<? extends Interactions> interactions;
    private WaitForConnectClient waitForConnectClient;// Manage waiting process
    @Getter
    private final List<Socket> clientSockets;// Client socket collection (thread-safe)
    private ServerSocket serverSocket;
    @Getter
    private String ipv4;// Server ipv4 address
    @Getter
    private String ipv6;// Server ipv6 address
    @Getter
    private int port;// Server port
    private final AtomicBoolean isServerRunning = new AtomicBoolean(false);

    // Initialize thread-safe collections
    {
        blackList = Collections.synchronizedSet(new HashSet<>());
        clientSockets = Collections.synchronizedList(new ArrayList<>());
    }

    // Initialize IP addresses
    {
        // Get IPv6 address
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException("Failed to get network interfaces", e);
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress instanceof Inet6Address && !inetAddress.isLinkLocalAddress()) {
                    ipv6 = inetAddress.getHostAddress();
                }
            }
        }

        // Get IPv4 address
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get localhost address", e);
        }
        ipv4 = localhost.getHostAddress();
    }

    /**
     * Constructor
     *
     * @param port         Server port
     * @param maxConnect   Maximum number of server connections
     * @param interactions Interaction logic after successful verification
     */
    public TCP_ServerSocket(int port, int maxConnect, Class<? extends Interactions> interactions) {
        this.port = port;
        this.maxConnect = maxConnect;
        this.interactions = interactions;
        // Create virtual thread executor (JDK 21+ Virtual Threads)
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Check if port is available
     *
     * @param port Port number
     * @return true if port is available
     */
    public static boolean isPortAvailable(int port) {
        try (java.nio.channels.ServerSocketChannel channel = java.nio.channels.ServerSocketChannel.open()) {
            channel.socket().setReuseAddress(true);
            channel.socket().bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            log.warn("Port {} is not available", port, e);
            return false;
        }
    }

    /**
     * Send message to client
     *
     * @param socket  Client socket
     * @param message Message to send
     * @throws IOException If I/O error occurs
     */
    public static void send(Socket socket, String message) throws IOException {
        if (socket == null || !socket.isConnected()) {
            throw new IllegalArgumentException("Socket is null or not connected");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        socket.getOutputStream().write(message.getBytes());
        socket.getOutputStream().flush();
    }

    // ------------------------------ Blacklist Management ------------------------------
    public void changeBlackList(Set<String> blackIP) {
        this.blackList = Collections.synchronizedSet(new HashSet<>(blackIP));
    }

    public void changeBlackList(List<String> blackIP) {
        this.blackList = Collections.synchronizedSet(new HashSet<>(blackIP));
    }

    public void changeBlackList(String... blackIP) {
        this.blackList = Collections.synchronizedSet(new HashSet<>(Arrays.asList(blackIP)));
    }

    public void cleanBlacklist() {
        blackList.clear();
    }

    public void removeBlackIP(String blackIP) {
        blackList.remove(blackIP);
    }

    public boolean containsBlack(String blackIP) {
        return blackList.contains(blackIP);
    }

    public void addBlackList(String blackIP) {
        blackList.add(blackIP);
    }

    public void addBlackList(List<String> blackIP) {
        blackList.addAll(blackIP);
    }

    public void addBlackList(String... blackIP) {
        blackList.addAll(Arrays.asList(blackIP));
    }

    public void addBlackList(Set<String> blackIP) {
        blackList.addAll(blackIP);
    }

    // ------------------------------ Connection Management ------------------------------
    /**
     * Add client to blacklist and disconnect
     *
     * @param client Client socket
     */
    public void addBlackListByCurrentSocket(Socket client) {
        if (client == null) return;
        String ip = client.getInetAddress().getHostAddress();
        disconnect(ip);
        addBlackList(ip);
    }

    /**
     * Disconnect specific client by IP
     *
     * @param ip Client IP address
     */
    public void disconnect(String ip) {
        if (ip == null || ip.isEmpty()) return;

        // Use iterator to avoid ConcurrentModificationException
        Iterator<Socket> iterator = clientSockets.iterator();
        while (iterator.hasNext()) {
            Socket socket = iterator.next();
            if (socket.getInetAddress().getHostAddress().equals(ip)) {
                try {
                    socket.close();
                    iterator.remove(); // Safe removal
                    log.info("Disconnected client: {}", ip);
                } catch (IOException e) {
                    log.warn("Failed to close socket for client: {}", ip, e);
                }
                break;
            }
        }
    }

    /**
     * Get all connected client IPs
     *
     * @return Set of client IPs
     */
    public Set<String> getClientIP() {
        if (clientSockets.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> clientIps = new HashSet<>();
        for (Socket socket : clientSockets) {
            clientIps.add(socket.getInetAddress().getHostAddress());
        }
        return clientIps;
    }

    /**
     * Check and get active client sockets
     *
     * @return List of active client sockets
     */
    public List<Socket> checkAndGetClientSockets() {
        checkConnectState();
        return new ArrayList<>(clientSockets); // Return copy to avoid external modification
    }

    /**
     * Check client connection status (thread-safe)
     */
    public synchronized void checkConnectState() {
        if (clientSockets.isEmpty()) return;

        Iterator<Socket> iterator = clientSockets.iterator();
        while (iterator.hasNext()) {
            Socket socket = iterator.next();
            try {
                if (socket.isClosed() || !socket.isConnected()) {
                    iterator.remove();
                    log.info("Removed closed socket: {}", socket.getInetAddress().getHostAddress());
                    continue;
                }
                // Check connection liveness
                socket.sendUrgentData(1);
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                iterator.remove();
                log.warn("Removed inactive socket: {}", socket.getInetAddress().getHostAddress(), e);
            }
        }
    }

    // ------------------------------ Server Lifecycle ------------------------------
    /**
     * Start TCP server
     *
     * @throws IOException If I/O error occurs
     */
    public void start() throws IOException {
        if (!isServerRunning.compareAndSet(false, true)) {
            log.warn("Server is already running on port: {}", port);
            return;
        }

        // Check port availability
        if (!isPortAvailable(port)) {
            throw new IOException("Port " + port + " is not available");
        }

        // Create server socket with SO_REUSEADDR
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));

        log.info("TCP server started on port: {} (IPv4: {}, IPv6: {})", port, ipv4, ipv6);

        // Start accept client connections with virtual thread
        waitForConnectClient = new WaitForConnectClient(serverSocket, this);
        // Use virtual thread for accepting connections
        Thread acceptThread = Thread.ofVirtual().name("tcp-server-accept-" + port).start(waitForConnectClient);
    }

    /**
     * Close TCP server
     *
     * @throws IOException If I/O error occurs
     */
    public void close() throws IOException {
        if (!isServerRunning.compareAndSet(true, false)) {
            log.warn("Server is already closed");
            return;
        }

        log.info("Shutting down TCP server on port: {}", port);

        // Stop accepting new connections
        if (waitForConnectClient != null) {
            waitForConnectClient.stop();
        }

        // Close server socket
        if (serverSocket != null) {
            serverSocket.close();
        }

        // Close all client sockets
        synchronized (clientSockets) {
            for (Socket socket : clientSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("Failed to close client socket", e);
                }
            }
            clientSockets.clear();
        }

        // Shutdown virtual thread executor (graceful shutdown)
        if (virtualThreadExecutor != null) {
            virtualThreadExecutor.shutdown();
            try {
                if (!virtualThreadExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    virtualThreadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                virtualThreadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("TCP server closed on port: {}", port);
    }

    // ------------------------------ Inner Classes ------------------------------
    /**
     * Functional interface for client validation
     */
    @FunctionalInterface
    public interface CheckClient {
        boolean Check(Socket socket);
    }
}

/**
 * Client connection acceptor (runs in virtual thread)
 */
@Slf4j
class WaitForConnectClient implements Runnable {
    private final ServerSocket serverSocket;
    private final TCP_ServerSocket tcpServerSocket;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    public WaitForConnectClient(ServerSocket serverSocket, TCP_ServerSocket tcpServerSocket) {
        this.serverSocket = serverSocket;
        this.tcpServerSocket = tcpServerSocket;
    }

    public void stop() {
        isStopped.set(true);
        // Interrupt accept() method
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn("Failed to close server socket", e);
        }
    }

    @Override
    public void run() {
        log.info("Start accepting client connections on port: {}", tcpServerSocket.getPort());

        while (!isStopped.get()) {
            try {
                // Blocking accept (will be unblocked when serverSocket is closed)
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                log.info("New connection request from: {}", clientIp);

                // 1. Check maximum connection limit
                int currentConnections = tcpServerSocket.getClientSockets().size();
                int maxConnections = tcpServerSocket.getMaxConnect();
                if (maxConnections != -1 && currentConnections >= maxConnections) {
                    clientSocket.close();
                    log.info("Connection refused ({}): Max connections reached (current: {}, max: {})",
                            clientIp, currentConnections, maxConnections);
                    continue;
                }

                // 2. Check blacklist
                if (tcpServerSocket.containsBlack(clientIp)) {
                    clientSocket.close();
                    log.info("Connection refused ({}): IP is in blacklist", clientIp);
                    continue;
                }

                // 3. Check client validation (developer-defined)
                if (tcpServerSocket.checkForClient != null && !tcpServerSocket.checkForClient.Check(clientSocket)) {
                    clientSocket.close();
                    log.info("Connection refused ({}): Failed developer's validation", clientIp);
                    continue;
                }

                // 4. Add client to connection list
                tcpServerSocket.getClientSockets().add(clientSocket);
                log.info("Client connected: {} (total connections: {})", clientIp, tcpServerSocket.getClientSockets().size());

                // 5. Handle client interaction with virtual thread
                try {
                    Interactions interactionInstance = Interactions.getInstance(
                            tcpServerSocket.interactions, clientSocket, tcpServerSocket.getClientSockets()
                    );
                    if (interactionInstance != null) {
                        // Submit interaction task to virtual thread executor
                        tcpServerSocket.getVirtualThreadExecutor().execute(() -> {
                            try {
                                interactionInstance.call(); // Assume Interactions implements Runnable
                            } catch (Exception e) {
                                log.error("Error handling client interaction: {}", clientIp, e);
                                // Clean up on error
                                tcpServerSocket.disconnect(clientIp);
                            }
                        });
                    } else {
                        log.warn("Failed to create interaction instance for client: {}", clientIp);
                        clientSocket.close();
                        tcpServerSocket.getClientSockets().remove(clientSocket);
                    }
                } catch (Exception e) {
                    log.error("Failed to initialize client interaction: {}", clientIp, e);
                    clientSocket.close();
                    tcpServerSocket.getClientSockets().remove(clientSocket);
                }

            } catch (IOException e) {
                if (isStopped.get()) {
                    log.info("Stopped accepting client connections");
                    break;
                }
                log.error("Error accepting client connection", e);
            }
        }

        log.info("Connection acceptor thread stopped");
    }
}