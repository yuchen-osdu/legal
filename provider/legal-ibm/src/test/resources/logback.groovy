import org.slf4j.bridge.SLF4JBridgeHandler
import ch.qos.logback.classic.jul.LevelChangePropagator

// see also: http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
// performance speedup for redirected JUL loggers
def lcp = new LevelChangePropagator()
lcp.context = context
lcp.resetJUL = true
context.addListener(lcp)

// needed only for the JUL bridge: http://stackoverflow.com/a/9117188/1915920
java.util.logging.LogManager.getLogManager().reset()
SLF4JBridgeHandler.removeHandlersForRootLogger()
SLF4JBridgeHandler.install()
java.util.logging.Logger.getLogger( "" ).setLevel( java.util.logging.Level.FINEST )

def logPattern = "|%.-1level| [%thread] %20.30logger{30}| %msg%n"
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = logPattern
    }
}
root(TRACE, ["STDOUT"])

def rootLvl = INFO
logger( "antlr", rootLvl )
logger( "de", rootLvl )
logger( "ch", rootLvl )
logger( "com", rootLvl )
logger( "java", rootLvl )
logger( "javassist", rootLvl )
logger( "javax", rootLvl )
logger( "junit", rootLvl )
logger( "groovy", rootLvl )
logger( "net", rootLvl )
logger( "org", rootLvl )
logger( "sun", rootLvl )
logger( "org.opengroup.osdu", DEBUG )

scan("30 seconds")  // reload/apply-on-change config every x sec