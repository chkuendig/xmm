cd MovieManager
echo Deleting files...
rm -r net
echo Compiling...
javac -server -d . -classpath $CLASSPATH:.:lib/skinlf.jar:lib/oalnf.jar:lib/TableLayout.jar:lib/useful.jar:lib/BrowserLauncher2-10rc4.jar:lib/jxl.jar:lib/JNative.jar:lib/retroweaver-all-1.2.2.jar:lib/commons-httpclient-3.0.1.jar:lib/log4j-1.2.13.jar:lib/xnap-commons-0.9.5.jar:lib/gettext-commons-0.9.jar:lib/castor-1.0.5.jar source/net/sf/xmm/moviemanager/*.java source/net/sf/xmm/moviemanager/database/*.java source/net/sf/xmm/moviemanager/extentions/*.java source/net/sf/xmm/moviemanager/fileproperties/*.java source/net/sf/xmm/moviemanager/util/*.java source/net/sf/xmm/moviemanager/http/*.java source/net/sf/xmm/moviemanager/models/*.java source/net/sf/xmm/moviemanager/commands/*.java source/net/sf/xmm/moviemanager/mediainfodll/*.java
cd ..