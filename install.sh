#!/bin/bash

# Installer script that works on Karl's machine.

# Basically, the steps of "installing" this are:
#  1. Rename the compiled JAR file to match the version number Maven expects.
#     It's just a minor version change -- Maven won't realize the difference.
#  2. Copy the compiled JAR file into the local Maven repository.
#     Maven uses the repository copy of the JAR file when building and packaging
#  3. Overwrite the sha1 hash file to contain the hash of the modified JAR
#     so that Maven doesn't realize we substituted it.

# I don't know where this is kept on Windows -- and Windows lacks some of the
# same Unix utilities -- so I can't write a batch version of this.

BASE_DIR=$(dirname $0)
PLUGIN_FILENAME=$(basename $(ls ${BASE_DIR}/hibernate-core/target/libs/*.jar))
DEST_FILENAME=hibernate-core-4.3.10.Final.jar
DESTINATION=~/.m2/repository/org/hibernate/hibernate-core/4.3.10.Final

if [[ ! -e ${DESTINATION}/${DEST_FILENAME}.bak ]]; then
    cp -v ${DESTINATION}/${DEST_FILENAME}{,.bak}
fi
if [[ ! -e ${DESTINATION}/${DEST_FILENAME}.bak ]]; then
    cp -v ${DESTINATION}/${DEST_FILENAME}{,.bak}
fi

cp -v ${BASE_DIR}/hibernate-core/target/libs/${PLUGIN_FILENAME} ${DESTINATION}/${DEST_FILENAME}
sha1sum ${DESTINATION}/${DEST_FILENAME} | awk '{print $1}' | tee ${DESTINATION}/${DEST_FILENAME}.sha1
