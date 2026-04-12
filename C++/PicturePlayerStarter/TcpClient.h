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

#pragma once
#include <winsock2.h>
#include <ws2tcpip.h>
#include <regex>
#include <string>
#include <stdexcept>
#include <future>
#include <chrono>
#include <iostream>

#pragma comment(lib, "ws2_32.lib")

class TCPClient {
private:
    static inline const std::string IPv4_REGEX =
        "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    static inline const std::string IPv6_REGEX =
        "^(([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4})*)|::([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4})*)?)$";

    static inline const std::string DomainName_REGEX =
        "^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,6}$";

    SOCKET clientSocket;
    std::string serverIP;
    int serverPort;
    bool isConnectedOver;

    // 初始化Winsock
    bool initWinsock() {
        WSADATA wsaData;
        int result = WSAStartup(MAKEWORD(2, 2), &wsaData);
        return result == 0;
    }

    // 清理Winsock
    void cleanupWinsock() {
        WSACleanup();
    }

public:
    TCPClient(const std::string& ip, int port)
        : serverIP(ip), serverPort(port), clientSocket(INVALID_SOCKET), isConnectedOver(false) {
        if (!initWinsock()) {
            throw std::runtime_error("Winsock Initialization failed");
        }
    }

    ~TCPClient() {
        close();
        cleanupWinsock();
    }

    // 检查端口是否可用
    static bool isPortAvailable(int port) {
        WSADATA wsaData;
        if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
            return false;
        }

        SOCKET testSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (testSocket == INVALID_SOCKET) {
            WSACleanup();
            return false;
        }

        sockaddr_in addr;
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = INADDR_ANY;
        addr.sin_port = htons(port);

        bool available = bind(testSocket, (SOCKADDR*)&addr, sizeof(addr)) != SOCKET_ERROR;
        closesocket(testSocket);
        WSACleanup();
        return available;
    }

    // 验证IP地址
    static bool matchIP(const std::string& ip) {
        std::regex ipv4Regex(IPv4_REGEX);
        std::regex ipv6Regex(IPv6_REGEX);
        return std::regex_match(ip, ipv4Regex) ^ std::regex_match(ip, ipv6Regex);
    }

    // 验证域名
    static bool matchDomainName(const std::string& domain) {
        std::regex domainRegex(DomainName_REGEX);
        return std::regex_match(domain, domainRegex);
    }

    // 验证端口
    static bool matchPort(int port) {
        return port > 0 && port <= 65535;
    }

    // 综合验证
    static bool match(const std::string& ipOrDomain, int port) {
        return (matchIP(ipOrDomain) ^ matchDomainName(ipOrDomain)) && matchPort(port);
    }

    // 连接服务器
    void connect() {
        if (isConnected()) {
            throw std::runtime_error("Already connected");
        }

        if (!match(serverIP, serverPort)) {
            throw std::invalid_argument("The IP address or port is invalid");
        }

        // 创建socket
        clientSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (clientSocket == INVALID_SOCKET) {
            throw std::runtime_error("Failed to create socket: " + std::to_string(WSAGetLastError()));
        }

        // 设置服务器地址
        sockaddr_in serverAddr;
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_port = htons(serverPort);

        // 解析IP地址
        if (inet_pton(AF_INET, serverIP.c_str(), &serverAddr.sin_addr) <= 0) {
            closesocket(clientSocket);
            throw std::runtime_error("Failed to parse IP address");
        }

        // 连接服务器
        if (::connect(clientSocket, (SOCKADDR*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
            closesocket(clientSocket);
            clientSocket = INVALID_SOCKET;
            throw std::runtime_error("Failed to connect to the server: " + std::to_string(WSAGetLastError()));
        }

        isConnectedOver = true;
    }

    // 发送消息
    void send(const std::string& message) {
        if (!isConnected()) {
            throw std::runtime_error("Not connected to the server");
        }

        int bytesSent = ::send(clientSocket, message.c_str(), message.length(), 0);
        if (bytesSent == SOCKET_ERROR) {
            throw std::runtime_error("Failed to send data: " + std::to_string(WSAGetLastError()));
        }
    }

    void sendln(const std::string& message) {
		send(message + "\n");
    }

    // 接收消息
    std::string receive(int bufferSize = 1024) {
        if (!isConnected()) {
            throw std::runtime_error("Not connected to the server");
        }

        char* buffer = new char[bufferSize];
        int bytesRead = recv(clientSocket, buffer, bufferSize - 1, 0);

        if (bytesRead <= 0) {
            delete[] buffer;
            if (bytesRead == 0) {
                throw std::runtime_error("The connection has been closed");
            }
            else {
                throw std::runtime_error("Failed to receive data: " + std::to_string(WSAGetLastError()));
            }
        }

        buffer[bytesRead] = '\0';
        std::string result(buffer);
        delete[] buffer;
        return result;
    }

    std::string removeNewlines(std::string str) {
        str.erase(std::remove(str.begin(), str.end(), '\n'), str.end());
        return str;
    }

    // 获取服务器支持的软件名称
    std::string getServerSupportedSoftwareName() {
        if (!isConnected()) {
            throw std::runtime_error("Not connected to the server");
        }

        sendln("{getSoftwareName}");

        auto future = std::async(std::launch::async, &TCPClient::receive, this, 1024);
        if (future.wait_for(std::chrono::seconds(5)) == std::future_status::timeout) {
            throw std::runtime_error("Timeout while retrieving software name");
        }

        return removeNewlines(future.get());
    }

    

    // 检查服务器是否支持当前软件名称
    bool isServerSupportedSoftwareName(const std::string& currentSoftwareName) {
        return getServerSupportedSoftwareName() == currentSoftwareName;
    }

    // 关闭连接
    void close() {
        if (clientSocket != INVALID_SOCKET) {
            closesocket(clientSocket);
            clientSocket = INVALID_SOCKET;
        }
        isConnectedOver = false;
    }

    // 判断是否已连接
    bool isConnected() const {
        return isConnectedOver;
    }
};

