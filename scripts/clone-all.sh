#!/bin/bash

while read -r PROJ FULL HASH; do
  echo "${PROJ} -- ${HASH}"
  git clone -n https://github.com/apache/commons-${PROJ}
  pushd commons-${PROJ}
  git checkout ${HASH}
  popd
done < list.txt
