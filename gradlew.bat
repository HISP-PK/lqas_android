@echo off
setlocal

set DIR=%~dp0
if "%DIR%" == "" set DIR=.

set APP_BASE_NAME=%~n0
set APP_HOME=%DIR%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle

set LOCAL_GRADLE=
for /d %%D in ("%GRADLE_USER_HOME%\wrapper\dists\gradle-8.14-all\*") do (
    if exist "%%D\gradle-8.14\bin\gradle.bat" set LOCAL_GRADLE=%%D\gradle-8.14\bin\gradle.bat
)
if not defined LOCAL_GRADLE (
    for /d %%D in ("%GRADLE_USER_HOME%\wrapper\dists\gradle-8.14-stub\*") do (
        if exist "%%D\bin\gradle.bat" set LOCAL_GRADLE=%%D\bin\gradle.bat
    )
)
if defined LOCAL_GRADLE (
    "%LOCAL_GRADLE%" %*
    exit /b %ERRORLEVEL%
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper-main.jar;%APP_HOME%\gradle\wrapper\gradle-wrapper-shared.jar

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
exit /b 1

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
endlocal
