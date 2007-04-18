@cd MovieManager
@echo Deleting old jar file...
@del MovieManager.jar
jar cfm MovieManager.jar ../Manifest.txt codecs images queries net MovieManager.tmx
@cd ..