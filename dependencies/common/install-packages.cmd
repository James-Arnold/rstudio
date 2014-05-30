@echo off

setlocal

set PATH=%PATH%;%CD%\tools

call:install rmarkdown master
call:install packrat master
call:install shiny master

GOTO:EOF

:install

set PACKAGE=%1
set PACKAGE_VERSION=%2

REM git clone if necessary
set PACKAGE_DIR="%PACKAGE%"
if not exist "%PACKAGE_DIR%" (
   git clone "https://github.com/rstudio/%PACKAGE%.git"
)

REM clean and checkout target branch
pushd "%PACKAGE_DIR%"
git checkout .
git clean -df .
git pull
git checkout "%PACKAGE_VERSION%"


REM append GitHub fields to DESCRIPTION
git rev-parse "%PACKAGE_VERSION%" > PACKAGE_SHA1
set /p PACKAGE_SHA1= < PACKAGE_SHA1
del PACKAGE_SHA1
echo GithubRepo: %PACKAGE% >> DESCRIPTION
echo GithubUsername: rstudio >> DESCRIPTION
echo GithubRef: %PACKAGE_VERSION% >> DESCRIPTION
echo GithubSHA1: %PACKAGE_SHA1% >> DESCRIPTION
echo Repository: RStudioIDE >> DESCRIPTION

REM create source package
popd
set PACKAGE_ARCHIVE_PATTERN="%PACKAGE%*.tar.gz"
del /s /q %PACKAGE_ARCHIVE_PATTERN%
R CMD build "%PACKAGE%"

REM modify filename to include SHA1
for %%f in (%PACKAGE_ARCHIVE_PATTERN%) do set PACKAGE_ARCHIVE=%%f
set PACKAGE_ARCHIVE_STEM=%PACKAGE_ARCHIVE:~0,-7%
set PACKAGE_ARCHIVE_SHA1=%PACKAGE_ARCHIVE_STEM%_%PACKAGE_SHA1%.tar.gz
move %PACKAGE_ARCHIVE% %PACKAGE_ARCHIVE_SHA1%

GOTO:EOF


