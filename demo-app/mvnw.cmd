@IF "%DEBUG%" == "" @ECHO OFF
echo [INFO] Running demo-app Maven wrapper...
mvn %*
exit /b %ERRORLEVEL%
