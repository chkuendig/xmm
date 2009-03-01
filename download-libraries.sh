#!/bin/bash

echo "This script it outdated. Go download to the source package and copy the files in MovieManager/lib."
exit

echo "This script downloads the necessary libraries to run MeDs MovieManager."

# Get current version from MovieManager.java to compose filename for sources
VERFILE=`find . | grep /MovieManagerConfig.java`
CURRENTVERSION=`grep "String _version" $VERFILE | awk 'BEGIN { FS = "\"" } { print $2 }'`

echo "Current CVS version ${CURRENTVERSION}"

# Compose download URL
ZIPFILE="MovieManager.v.${CURRENTVERSION}.src.zip"
URL="http://downloads.sourceforge.net/xmm/${ZIPFILE}?download"

echo
echo "Starting download for $URL"
echo
wget -c $URL

# MovieManager/lib is in a subdirectory in the ZIP file, whose name may change
# -> get name and create link to copy to correct destination
LINKTARGET=`unzip -l $ZIPFILE '*/MovieManager/lib/' | grep /MovieManager/lib/ | awk 'BEGIN { FS = " " } { sub("/MovieManager/lib/", "", $4) ; print $4 }'`

ln -s . $LINKTARGET

# Unzip files in MovieManager/lib/
unzip -n $ZIPFILE '*/MovieManager/lib/*'

rm $LINKTARGET

echo "Done. Now compile and run."
