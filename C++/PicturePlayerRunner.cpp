#include <windows.h>
#include <iostream>
#include <tchar.h>
#include <vector>
#include <shellapi.h>
#include <shlwapi.h>
#include <string>
#include <io.h>
#include <fcntl.h>

#pragma comment(lib, "shlwapi.lib")

static std::vector<std::wstring> getRuntimeArgs();
std::wstring getExeDirectory();

void RedirectIOToConsole()
{
    // 获取标准输出句柄
    HANDLE hStdOut = GetStdHandle(STD_OUTPUT_HANDLE);
    if (hStdOut != INVALID_HANDLE_VALUE) {
        int fd = _open_osfhandle((intptr_t)hStdOut, _O_TEXT);
        if (fd != -1) {
            FILE* fp = _fdopen(fd, "w");
            if (fp != nullptr) {
                *stdout = *fp;
                setvbuf(stdout, nullptr, _IONBF, 0);
            }
        }
    }

    // 获取标准错误句柄
    HANDLE hStdErr = GetStdHandle(STD_ERROR_HANDLE);
    if (hStdErr != INVALID_HANDLE_VALUE) {
        int fd = _open_osfhandle((intptr_t)hStdErr, _O_TEXT);
        if (fd != -1) {
            FILE* fp = _fdopen(fd, "w");
            if (fp != nullptr) {
                *stderr = *fp;
                setvbuf(stderr, nullptr, _IONBF, 0);
            }
        }
    }
}

int main() {
    // 控制台处理：附加到父控制台或创建新控制台
    if (AttachConsole(ATTACH_PARENT_PROCESS)) {
        // 附加到父控制台成功
    }
    else {
        // 创建新控制台
        AllocConsole();
    }

    // 重定向标准输入输出
    RedirectIOToConsole();

    // 获取当前可执行文件所在的目录
    std::wstring exeDir = getExeDirectory();

    // 获取命令行参数
    std::vector<std::wstring> arguments = getRuntimeArgs();

    // 构建基础Java命令
    std::wstring baseCommand = L"java -cp \"" + exeDir + L"\\PicturePlayer.jar;" +
        exeDir + L"\\lib\\*\" Runner.Main -XX:+UseG1GC";

    // 添加额外参数（跳过第一个参数即程序自身路径）
    for (size_t i = 1; i < arguments.size(); ++i) {
        // 检查参数是否是文件路径
        if (PathFileExistsW(arguments[i].c_str())) {
            // 用引号包裹文件路径（处理空格问题）
            baseCommand += L" \"";
            baseCommand += arguments[i];
            baseCommand += L"\"";
        }
    }

    // 准备进程启动信息
    STARTUPINFO si = { sizeof(si) };
    PROCESS_INFORMATION pi;

    // 创建可修改的命令行缓冲区
    std::vector<wchar_t> commandLine(baseCommand.begin(), baseCommand.end());
    commandLine.push_back(L'\0'); // 添加字符串终止符

    // 关键修改：移除 CREATE_NO_WINDOW 标志
    BOOL success = CreateProcess(
        NULL,                   // 不使用模块名
        commandLine.data(),     // 动态构建的命令行
        NULL,                   // 进程句柄不可继承
        NULL,                   // 线程句柄不可继承
        FALSE,                  // 不继承句柄
        0,                      // 无特殊标志 - 允许创建控制台窗口
        NULL,                   // 使用父进程环境
        exeDir.c_str(),         // 设置工作目录为程序所在目录
        &si,                    // 启动信息
        &pi                     // 进程信息
    );

    // 检查进程是否创建成功
    if (!success) {
        DWORD error = GetLastError();
        std::cerr << "创建进程失败! 错误代码: " << error << std::endl;

        // 常见错误处理
        if (error == ERROR_FILE_NOT_FOUND) {
            std::cerr << "未找到java.exe。请确保Java运行时已安装并配置在PATH中。" << std::endl;
        }
        return 1;
    }

    std::cout << "Java程序已启动 (PID: " << pi.dwProcessId << ")" << std::endl;

    // 等待程序结束
    WaitForSingleObject(pi.hProcess, INFINITE);

    // 获取退出代码
    DWORD exitCode;
    GetExitCodeProcess(pi.hProcess, &exitCode);
    std::cout << "Java程序已退出，代码: " << exitCode << std::endl;

    // 清理资源
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);

    // 如果创建了新控制台，关闭它
    if (!AttachConsole(ATTACH_PARENT_PROCESS)) {
        FreeConsole();
    }

    return exitCode;
}

// 获取宽字符命令行参数
static std::vector<std::wstring> getRuntimeArgs() {
    LPWSTR* argv = nullptr;
    int argc = 0;

    // 获取命令行参数
    argv = CommandLineToArgvW(GetCommandLineW(), &argc);
    std::vector<std::wstring> arguments;

    if (argv == nullptr) {
        return arguments;
    }

    // 存储所有参数
    for (int i = 0; i < argc; i++) {
        arguments.push_back(argv[i]);
    }

    // 释放资源
    LocalFree(argv);

    return arguments;
}

// 获取当前可执行文件所在目录
std::wstring getExeDirectory() {
    wchar_t exePath[MAX_PATH];
    GetModuleFileNameW(NULL, exePath, MAX_PATH);

    // 提取目录部分（移除文件名）
    std::wstring path(exePath);
    size_t pos = path.find_last_of(L"\\/");
    if (pos != std::wstring::npos) {
        return path.substr(0, pos);
    }
    return L"."; // 如果无法提取，返回当前目录
}