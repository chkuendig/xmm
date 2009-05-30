@cd MovieManager
@echo Deleting files...
@del /Q/S net > nul
@echo Compiling...
javac -d . -target 1.5 -source 1.5 -classpath "%CLASSPATH%";.;^
lib/LookAndFeels/skinlf.jar;^
lib/LookAndFeels/oalnf.jar;^
lib/TableLayout.jar;^
lib/useful.jar;^
lib/BrowserLauncher2-10rc4.jar;^
lib/jxl.jar;^
lib/jna-3.0.9.jar;^
lib/retroweaver-all-2.0.jar;^
lib/commons-httpclient-3.0.1.jar;^
lib/log4j-1.2.13.jar;^
lib/commons-beanutils.jar;^
lib/commons-collections-3.2.jar;^
lib/commons-digester-1.7.jar;^
lib/itext-1.4.5.jar;^
lib/jasperreports-3.0.0.jar;^
lib/poi-2.5.1-final-20040804.jar;^
lib/castor-1.2.jar;^
lib/ProportionLayout.jar;^
lib/driveinfo/sfc.jar;^
lib/ostermillerutils_1_05_00_for_java_1_4.jar;^
lib/cobra/cobra-0.98.2.jar;^
lib/cobra/js.jar^
 source/net/sf/xmm/moviemanager/*.java^
 source/net/sf/xmm/moviemanager/database/*.java^
 source/net/sf/xmm/moviemanager/swing/extentions/*.java^
 source/net/sf/xmm/moviemanager/swing/extentions/events/*.java^
 source/net/sf/xmm/moviemanager/swing/extentions/filetree/*.java^
 source/net/sf/xmm/moviemanager/swing/util/table/*.java^
 source/net/sf/xmm/moviemanager/fileproperties/*.java^
 source/net/sf/xmm/moviemanager/gui/*.java^
 source/net/sf/xmm/moviemanager/gui/menubar/*.java^
 source/net/sf/xmm/moviemanager/util/*.java^
 source/net/sf/xmm/moviemanager/util/plugins/*.java^
 source/net/sf/xmm/moviemanager/http/*.java^
 source/net/sf/xmm/moviemanager/models/*.java^
 source/net/sf/xmm/moviemanager/models/imdb/*.java^
 source/net/sf/xmm/moviemanager/commands/*.java^
 source/net/sf/xmm/moviemanager/commands/guistarters/*.java^
 source/net/sf/xmm/moviemanager/commands/importexport/*.java^
 source/net/sf/xmm/moviemanager/mediainfodll/*.java
@cd ..
