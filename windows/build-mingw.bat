@echo off
setlocal
echo Building LAN Remote Win32 portable...
i686-w64-mingw32-g++ -std=gnu++11 -O2 -s -mwindows -D_WIN32_WINNT=0x0601 -DWINVER=0x0601 lanremote.cpp -o LANRemote.exe -static -static-libgcc -static-libstdc++ -lgdiplus -lws2_32 -lole32 -luuid -lcomctl32 -lwinmm
if errorlevel 1 exit /b 1
echo LANRemote.exe Win32 portable build OK
