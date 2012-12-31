//
//

#include <sys/syscall.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <iomanip>
#include <log4cplus/logger.h>
#include <log4cplus/fileappender.h>
#include <log4cplus/loggingmacros.h>
#include <log4cplus/configurator.h>
#include <log4cplus/ndc.h>
using namespace log4cplus;

void InitLog (char *argv0, Logger& logger);
void LogHexDump (Logger& log, std::string& str);
